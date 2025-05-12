package br.com.marcielli.bancom.dao;

import br.com.marcielli.bancom.entity.Cartao;
import br.com.marcielli.bancom.entity.CartaoCredito;
import br.com.marcielli.bancom.entity.CartaoDebito;
import br.com.marcielli.bancom.entity.Cliente;
import br.com.marcielli.bancom.entity.Conta;
import br.com.marcielli.bancom.entity.ContaCorrente;
import br.com.marcielli.bancom.entity.ContaPoupanca;
import br.com.marcielli.bancom.entity.Transferencia;
import br.com.marcielli.bancom.entity.User;
import br.com.marcielli.bancom.enuns.CategoriaConta;
import br.com.marcielli.bancom.enuns.TipoCartao;
import br.com.marcielli.bancom.enuns.TipoConta;
import br.com.marcielli.bancom.enuns.TipoTransferencia;
import br.com.marcielli.bancom.exception.ClienteEncontradoException;
import br.com.marcielli.bancom.mappers.CartaoRowMapper;
import br.com.marcielli.bancom.mappers.ClienteRowMapper;
import br.com.marcielli.bancom.mappers.ContaWithTransferenciasRowMapper;
import br.com.marcielli.bancom.mappers.ContasRowMapper;
import br.com.marcielli.bancom.mappers.TransferenciaRowMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.RowMapper;

@Component
public class ClienteDao {

	private final JdbcTemplate jdbcTemplate;
	private final ContasRowMapper contasRowMapper;
	private static final Logger logger = LoggerFactory.getLogger(ClienteDao.class);

	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public ClienteDao(JdbcTemplate jdbcTemplate, ContasRowMapper contasRowMapper,
			NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.contasRowMapper = contasRowMapper;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public Optional<Cliente> findByCpf(Long cpf) {
		String sql = "SELECT c.id AS cliente_id, c.nome AS cliente_nome, c.cpf AS cliente_cpf, c.cliente_ativo, c.user_id, "
				+ "u.id AS user_id, u.username AS user_username, u.password AS user_password, u.user_ativo "
				+ "FROM clientes c " + "JOIN users u ON u.id = c.user_id " + "WHERE c.cpf = ?";

		List<Cliente> clientes = jdbcTemplate.query(sql, new ClienteRowMapper(), cpf);
		return clientes.stream().findFirst();
	}

	public boolean cpfExists(Long cpf) {
		String sql = "SELECT 1 FROM clientes WHERE cpf = ? LIMIT 1";
		List<Integer> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), cpf);
		return !result.isEmpty();
	}

	public List<Cliente> findByNomeContainingIgnoreCase(String nome) {
		String sql = "SELECT id, nome, cpf, cliente_ativo FROM clientes WHERE nome ILIKE ?";
		return jdbcTemplate.query(sql, new ClienteRowMapper(), "%" + nome + "%");
	}

	public void save(Cliente cliente) {
		if (cliente.getCpf() != null && cpfExists(cliente.getCpf())) {
			throw new ClienteEncontradoException("Já existe um cliente com esse CPF: " + cliente.getCpf());
		}

		String sql = "INSERT INTO clientes (nome, cpf, cliente_ativo, user_id) VALUES (?, ?, ?, ?)";
		try {
			jdbcTemplate.update(sql, cliente.getNome(), cliente.getCpf(), cliente.isClienteAtivo(),
					cliente.getUser().getId());
		} catch (DuplicateKeyException e) {
			throw new ClienteEncontradoException(
					"Já existe um cliente com esse CPF: " + cliente.getCpf() + " - Exception:" + e);
		} catch (DataAccessException e) {
			throw new ClienteEncontradoException("Erro ao salvar cliente no banco de dados - Exception:" + e);
		}
	}

	public Optional<Cliente> findById(Long id) {
		String sql = "SELECT " + "c.id AS cliente_id, " + "c.nome AS cliente_nome, " + "c.cpf AS cliente_cpf, "
				+ "c.cliente_ativo, " + "c.user_id, " + "u.username AS user_username, "
				+ "u.password AS user_password, " + "u.user_ativo AS user_ativo, " + "e.id AS endereco_id, " + "e.cep, "
				+ "e.cidade, " + "e.estado, " + "e.rua, " + "e.numero, " + "e.bairro, " + "e.complemento "
				+ "FROM clientes c " + "JOIN users u ON c.user_id = u.id "
				+ "LEFT JOIN enderecos e ON e.cliente_id = c.id " + "WHERE c.id = ?";

		List<Cliente> clientes = jdbcTemplate.query(sql, new ClienteRowMapper(), id);
		return clientes.isEmpty() ? Optional.empty() : Optional.of(clientes.get(0));
	}

	public Cliente findByIdWithContas(Long id) {
		String clienteSql = "SELECT * FROM clientes WHERE id = ?";
		Cliente cliente = jdbcTemplate.queryForObject(clienteSql, new BeanPropertyRowMapper<>(Cliente.class), id);

		if (cliente != null) {
			String contasSql = "SELECT * FROM contas WHERE cliente_id = ?";
			List<Conta> contas = jdbcTemplate.query(contasSql, new BeanPropertyRowMapper<>(Conta.class), id);
			cliente.setContas(contas);
		}

		return cliente;
	}

	public Cliente findByIdWithUser(Long id) {
		String sql = "SELECT c.*, u.id as user_id, u.username FROM clientes c "
				+ "LEFT JOIN users u ON c.user_id = u.id WHERE c.id = ?";

		return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
			Cliente cliente = new Cliente();
			try {
				Integer userId = rs.getObject("user_id", Integer.class);
				if (userId != null) {
					User user = new User();
					user.setId(userId);
					user.setUsername(rs.getString("username"));
					cliente.setUser(user);
				}
			} catch (SQLException e) {
				throw new DataAccessException("Erro ao ler user_id", e) {
					private static final long serialVersionUID = 1L;
				};
			}
			return cliente;
		}, id);
	}

	public Cliente findByIdWithContasAndTransferencias(Long clienteId) { //metodo usado por cliente para mostrar todos os clientes
		String sql = """
				           SELECT
				               c.id AS cliente_id,
				               c.nome,
				               c.cpf,
				               c.cliente_ativo,
				               co.id AS conta_id,
				               co.numero_conta,
				               co.tipo_conta,
				               co.saldo_conta,
				               co.pix_aleatorio,
				               co.categoria_conta,
				               co.status AS status_conta,
							   co.taxa_manutencao_mensal,
							   co.taxa_acresc_rend,
							   co.taxa_mensal,
				               t.id AS transferencia_id,
				               t.valor,
				               t.data,
				               t.id_cliente_origem,
				               t.id_cliente_destino,
				               t.id_conta_origem,
				               t.id_conta_destino,
				               t.id_cartao,
				               t.fatura_id,
				               t.tipo_transferencia,
				               t.codigo_operacao,
				               t.tipo_cartao,
				               ca.id AS cartao_id,
				               ca.senha,
				               ca.total_gasto_mes,
				               ca.conta_id,
				               ca.fatura_id AS id_fatura,
				               ca.numero_cartao,
				               ca.tipo_cartao AS cartao_tipo,
				               ca.categoria_conta,
				               ca.limite_credito_pre_aprovado,
				               ca.status AS status_cartao,
				               ca.total_gasto_mes_credito,
				               ca.limite_diario_transacao
				           FROM clientes c
				           LEFT JOIN contas co ON co.cliente_id = c.id
				           LEFT JOIN transferencias t ON t.id_conta_origem = co.id OR t.id_conta_destino = co.id
				           LEFT JOIN cartoes ca ON ca.conta_id = co.id
				           WHERE c.id = :clienteId
				   """;

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("clienteId", clienteId);

		Map<Long, Cliente> clienteMap = new HashMap<>();
		Map<Long, Conta> contaMap = new HashMap<>();

		namedParameterJdbcTemplate.query(sql, params, rs -> {
			try {
				// Cliente
				Long clienteIdRs = rs.getLong("cliente_id");
				logger.info("Iniciando processamento do cliente: Cliente ID: {}", clienteIdRs);
				Cliente cliente = clienteMap.computeIfAbsent(clienteIdRs, id -> {
					Cliente c = new Cliente();
					c.setId(id);
					try {
						c.setNome(rs.getString("nome"));
						String cpfStr = rs.getString("cpf");
						if (cpfStr != null) {
							c.setCpf(Long.valueOf(cpfStr));
						}
						c.setClienteAtivo(rs.getBoolean("cliente_ativo"));
					} catch (SQLException e) {
						logger.error("Erro ao processar os dados do cliente: {}", e.getMessage());
						throw new RuntimeException(e);
					}
					return c;
				});

				// Conta
				Long contaId = rs.getLong("conta_id");
				if (rs.wasNull()) {
					contaId = null;
				}

				if (contaId != null) {
					logger.info("Iniciando processamento da conta: Conta ID: {}", contaId);
					Conta conta = contaMap.computeIfAbsent(contaId, id -> {
						Conta co = new Conta();
						co.setId(id);
						co.setCartoes(new ArrayList<>());
						co.setTransferencias(new ArrayList<>());
						try {
							co.setNumeroConta(rs.getString("numero_conta"));
							String tipoContaStr = rs.getString("tipo_conta");
							co.setPixAleatorio(rs.getString("pix_aleatorio"));
							String categoriaContaStr1 = rs.getString("categoria_conta");
							co.setStatus(rs.getBoolean("status_conta"));
							try {
								co.setCategoriaConta(
										categoriaContaStr1 != null ? CategoriaConta.valueOf(categoriaContaStr1) : null);
							} catch (IllegalArgumentException e) {
								logger.warn("CategoriaConta inválida: {}. Definindo como null.", categoriaContaStr1);
								co.setCategoriaConta(null);
							}
							if (co instanceof ContaCorrente) {
								((ContaCorrente) co)
										.setTaxaManutencaoMensal(rs.getBigDecimal("taxa_manutencao_mensal"));
							}
							if (co instanceof ContaPoupanca) {
								((ContaPoupanca) co).setTaxaAcrescRend(rs.getBigDecimal("taxa_acresc_rend"));
								((ContaPoupanca) co).setTaxaMensal(rs.getBigDecimal("taxa_mensal"));
							}
							try {
								co.setTipoConta(tipoContaStr != null ? TipoConta.valueOf(tipoContaStr) : null);
							} catch (IllegalArgumentException e) {
								logger.warn("TipoConta inválido: {}. Definindo como null.", tipoContaStr);
								co.setTipoConta(null);
							}
							co.setSaldoConta(rs.getBigDecimal("saldo_conta"));
						} catch (SQLException e) {
							logger.error("Erro ao processar os dados da conta: {}", e.getMessage());
							throw new RuntimeException(e);
						}
						cliente.getContas().add(co);
						return co;
					});

					// Cartão
					Long cartaoId = rs.getLong("cartao_id");
					if (!rs.wasNull()) {
						// Verifica se o cartão já existe na conta antes de adicionar porque estava
						// duplicando
						boolean cartaoJaExiste = conta.getCartoes().stream().anyMatch(c -> c.getId().equals(cartaoId));

						if (!cartaoJaExiste) {
							logger.info("Iniciando processamento do cartão: Cartão ID: {}", cartaoId);
							String tipoCartaoStr = rs.getString("cartao_tipo");
							logger.debug("Tipo do cartão lido do banco: {}", tipoCartaoStr);
							TipoCartao tipoCartao = null;
							try {
								tipoCartao = tipoCartaoStr != null ? TipoCartao.valueOf(tipoCartaoStr) : null;
							} catch (IllegalArgumentException e) {
								logger.warn("TipoCartao inválido: {}. Definindo como null.", tipoCartaoStr);
							}

							String statusCartaoStr = rs.getString("status_cartao");
							logger.debug("Status do cartão lido do banco: {}", statusCartaoStr);

							boolean cartaoStatus = false; // Para log

							Cartao cartao = null;
							try {
								if (tipoCartao == TipoCartao.CREDITO) {
									CartaoCredito cartaoCredito = new CartaoCredito();
									cartaoCredito.setId(cartaoId);
									cartaoCredito.setNumeroCartao(rs.getString("numero_cartao"));
									cartaoCredito.setTipoCartao(tipoCartao);
									String categoriaContaStr = rs.getString("categoria_conta");
									try {
										cartaoCredito.setCategoriaConta(
												categoriaContaStr != null ? CategoriaConta.valueOf(categoriaContaStr)
														: null);
									} catch (IllegalArgumentException e) {
										logger.warn("CategoriaConta inválida: {}. Definindo como null.",
												categoriaContaStr);
										cartaoCredito.setCategoriaConta(null);
									}

									cartaoCredito.setStatus(rs.getBoolean("status_cartao"));
									cartaoCredito.setLimiteCreditoPreAprovado(
											rs.getBigDecimal("limite_credito_pre_aprovado"));
									cartaoCredito.setTotalGastoMesCredito(rs.getBigDecimal("total_gasto_mes_credito"));
									cartaoCredito.setSenha(rs.getString("senha"));
									cartaoCredito.setTotalGastoMes(rs.getBigDecimal("total_gasto_mes"));
									cartaoCredito.setContaId(rs.getLong("conta_id"));
									cartaoCredito.setFaturaId(rs.getLong("id_fatura"));

									cartaoStatus = rs.getBoolean("status_cartao"); // Para log

									cartao = cartaoCredito;
								} else if (tipoCartao == TipoCartao.DEBITO) {
									CartaoDebito cartaoDebito = new CartaoDebito();
									cartaoDebito.setId(cartaoId);
									cartaoDebito.setNumeroCartao(rs.getString("numero_cartao"));
									cartaoDebito.setTipoCartao(tipoCartao);
									String categoriaContaStr = rs.getString("categoria_conta");
									try {
										cartaoDebito.setCategoriaConta(
												categoriaContaStr != null ? CategoriaConta.valueOf(categoriaContaStr)
														: null);
									} catch (IllegalArgumentException e) {
										logger.warn("CategoriaConta inválida: {}. Definindo como null.",
												categoriaContaStr);
										cartaoDebito.setCategoriaConta(null);
									}
									cartaoDebito.setLimiteDiarioTransacao(rs.getBigDecimal("limite_diario_transacao"));
									cartaoDebito.setStatus(rs.getBoolean("status_cartao"));
									cartaoDebito.setTotalGastoMes(rs.getBigDecimal("total_gasto_mes"));
									cartaoDebito.setSenha(rs.getString("senha"));
									cartaoDebito.setContaId(rs.getLong("conta_id"));
									cartaoDebito.setFaturaId(rs.getLong("id_fatura"));

									cartaoStatus = rs.getBoolean("status_cartao"); // Para log
									cartao = cartaoDebito;
								} else {
									// criando um cartão basico caso o erro persista
									logger.warn("Tipo de cartão desconhecido ou nulo: {}. Criando cartão básico.",
											tipoCartaoStr);
									Cartao basico = new Cartao();
									basico.setId(cartaoId);
									basico.setNumeroCartao(rs.getString("numero_cartao"));
									basico.setTipoCartao(null);
									String categoriaContaStr = rs.getString("categoria_conta");
									try {
										basico.setCategoriaConta(
												categoriaContaStr != null ? CategoriaConta.valueOf(categoriaContaStr)
														: null);
									} catch (IllegalArgumentException e) {
										logger.warn("CategoriaConta inválida: {}. Definindo como null.",
												categoriaContaStr);
										basico.setCategoriaConta(null);
									}
									basico.setStatus(rs.getBoolean("status_cartao"));
									basico.setTotalGastoMes(rs.getBigDecimal("total_gasto_mes"));
									basico.setSenha(rs.getString("senha"));
									basico.setContaId(rs.getLong("conta_id"));
									basico.setFaturaId(rs.getLong("id_fatura"));
									cartao = basico;
								}

							} catch (SQLException e) {
								logger.error("Erro ao processar os dados do cartão: {}", e.getMessage());
								throw new RuntimeException(e);
							}

							if (cartao != null) {
								conta.getCartoes().add(cartao);
								logger.debug("Cartão adicionado à conta {}: Cartão ID {}, Status: {}", conta.getId(),
										cartao.getId(), cartaoStatus);
							} else {
								logger.warn("Nenhum cartão criado para cartao_id: {}", cartaoId);
							}
						} else {
							logger.debug("Cartão ID {} já existe na conta {}. Pulando duplicação.", cartaoId,
									conta.getId());
						}
					}
					
					// Transferência
					Long transferenciaId = rs.getLong("transferencia_id");
					if (rs.wasNull()) {
					    transferenciaId = null;
					}

					if (transferenciaId != null) {
					    try {
					        Long idClienteOrigem = rs.getLong("id_cliente_origem");
					        Long idCartaoTransferencia = rs.getLong("id_cartao");
					        String tipoTransferenciaStr = rs.getString("tipo_transferencia");
					        
					        final Long transferenciaIdFinal = transferenciaId;
					        
					        if (clienteId.equals(idClienteOrigem)) {
					           
					            boolean transferenciaJaExiste = conta.getTransferencias().stream()
					                .anyMatch(t -> t.getId().equals(transferenciaIdFinal));
					            
					            if (!transferenciaJaExiste) {
					                logger.info("Iniciando processamento da transferência enviada: Transferência ID: {}", transferenciaIdFinal);
					                Transferencia transferencia = new Transferencia();
					                transferencia.setId(transferenciaIdFinal);
					                transferencia.setValor(rs.getBigDecimal("valor"));
					                Timestamp timestamp = rs.getTimestamp("data");
					                if (timestamp != null) {
					                    transferencia.setData(timestamp.toLocalDateTime());
					                }
					                transferencia.setIdClienteOrigem(idClienteOrigem);
					                transferencia.setIdClienteDestino(rs.getLong("id_cliente_destino"));
					                transferencia.setIdContaOrigem(rs.getLong("id_conta_origem"));
					                transferencia.setIdContaDestino(rs.getLong("id_conta_destino"));
					                transferencia.setIdCartao(idCartaoTransferencia);
					                transferencia.setFaturaId(rs.getLong("fatura_id"));
					                
					                try {
					                    transferencia.setTipoTransferencia(tipoTransferenciaStr != null ? 
					                        TipoTransferencia.valueOf(tipoTransferenciaStr) : null);
					                } catch (IllegalArgumentException e) {
					                    logger.warn("TipoTransferencia inválido: {}. Definindo como null.", tipoTransferenciaStr);
					                    transferencia.setTipoTransferencia(null);
					                }
					                
					                transferencia.setCodigoOperacao(rs.getString("codigo_operacao"));
					                
					                String tipoCartaoStr = rs.getString("tipo_cartao");
					                try {
					                    transferencia.setTipoCartao(tipoCartaoStr != null ? 
					                        TipoCartao.valueOf(tipoCartaoStr) : null);
					                } catch (IllegalArgumentException e) {
					                    logger.warn("TipoCartao inválido: {}. Definindo como null.", tipoCartaoStr);
					                    transferencia.setTipoCartao(null);
					                }
					                
					                conta.getTransferencias().add(transferencia);
					                logger.debug("Transferência enviada adicionada à conta {}: Transferência ID {}", 
					                    conta.getId(), transferencia.getId());
					                
					                if (idCartaoTransferencia != null && idCartaoTransferencia > 0 && 
					                    "CARTAO_CREDITO".equals(tipoTransferenciaStr)) {
					                    
					                    final Transferencia transferenciaFinal = transferencia;
					                    
					                    for (Cartao cartao : conta.getCartoes()) {
					                        if (cartao.getId().equals(idCartaoTransferencia) && cartao instanceof CartaoCredito) {
					                        
					                            boolean creditoJaExiste = ((CartaoCredito) cartao).getTransferenciasCredito()
					                                .stream()
					                                .anyMatch(t -> t.getId().equals(transferenciaIdFinal));
					                            
					                            if (!creditoJaExiste) {
					                                ((CartaoCredito) cartao).getTransferenciasCredito().add(transferenciaFinal);
					                                logger.debug("Transferência de crédito adicionada ao cartão {}: ID {}", 
					                                    cartao.getId(), transferenciaFinal.getId());
					                            }
					                            break;
					                        }
					                    }
					                }
					            }
					        }
					    } catch (SQLException e) {
					        logger.error("Erro ao processar os dados da transferência: {}", e.getMessage());
					        throw new RuntimeException(e);
					    }
					}
			

				}
			} catch (SQLException e) {
				logger.error("Erro ao processar os dados do cliente, conta, cartão ou transferência: {}",
						e.getMessage());
				throw new RuntimeException(e);
			}
		});

		Cliente result = clienteMap.values().stream().findFirst().orElse(null);

		if (result != null) {
			logger.debug("Cliente retornado: ID {}, Contas: {}, Cartões: {}, Transferências: {}", result.getId(),
					result.getContas().size(),
					result.getContas().stream().mapToInt(conta -> conta.getCartoes().size()).sum(),
					result.getContas().stream().mapToInt(conta -> conta.getTransferencias().size()).sum());
		}
		return result;
	}

	public List<Transferencia> findByContaId(Long contaId) {
		String sql = "SELECT * FROM transferencias " + "WHERE id_conta_origem = ? " + "ORDER BY data DESC";

		return jdbcTemplate.query(sql, new TransferenciaRowMapper(), contaId);
	}

	public List<Transferencia> findTransferenciasCreditoByCartaoId(Long cartaoId) {
		String sql = """
				SELECT t.* FROM transferencias t
				JOIN fatura_transferencias ft ON t.id = ft.transferencia_id
				JOIN faturas f ON ft.fatura_id = f.id
				WHERE f.cartao_id = ? AND t.tipo_transferencia = 'CARTAO_CREDITO'
				ORDER BY t.data DESC""";

		return jdbcTemplate.query(sql, new TransferenciaRowMapper(), cartaoId);
	}

	public List<Cartao> findCartoesByContaId(Long contaId) {
		String cartaoSql = """
				    SELECT
				        c.id, c.tipo_conta, c.categoria_conta, c.tipo_cartao, c.numero_cartao,
				        c.status, c.senha, c.conta_id, c.fatura_id, c.total_gasto_mes,
				        c.limite_credito_pre_aprovado, c.taxa_utilizacao, c.taxa_seguro_viagem,
				        c.total_gasto_mes_credito, c.limite_diario_transacao,
				        co.cliente_id, cl.nome AS cliente_nome, co.saldo_conta, co.status AS conta_status,
				        co.tipo_conta AS conta_tipo_conta, co.categoria_conta AS conta_categoria_conta,
				        f.id AS fatura_id, f.cartao_id AS fatura_cartao_id, f.valor_total AS fatura_valor_total,
				        f.data_vencimento AS fatura_data_vencimento
				    FROM cartoes c
				    JOIN contas co ON c.conta_id = co.id
				    JOIN clientes cl ON co.cliente_id = cl.id
				    LEFT JOIN faturas f ON c.fatura_id = f.id
				    WHERE c.conta_id = ?
				""";
		return jdbcTemplate.query(cartaoSql, new CartaoRowMapper(), contaId);
	}

}