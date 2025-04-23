//package br.com.marcielli.BancoM.service;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import br.com.marcielli.BancoM.entity.Cartao;
//import br.com.marcielli.BancoM.entity.Conta;
//import br.com.marcielli.BancoM.entity.Seguro;
//import br.com.marcielli.BancoM.enuns.CategoriaConta;
//import br.com.marcielli.BancoM.enuns.TipoSeguro;
//import br.com.marcielli.BancoM.exception.SeguroNaoEncontradoException;
//import br.com.marcielli.BancoM.repository.CartaoRepository;
//import br.com.marcielli.BancoM.repository.SeguroRepository;
//
//@Service
//public class SeguroService {
//	
//	@Autowired
//    private SeguroRepository seguroRepository;
//
//    @Autowired
//    private CartaoRepository cartaoRepository;
//
////    @Autowired
////    private ContaRepositoy contaRepository;
////
////    @Autowired
////    private ClienteRepository clienteRepository;
//    
//    public Seguro contratarSeguro(Long idCartao, TipoSeguro tipo) {
//        Cartao cartao = cartaoRepository.findById(idCartao)
//            .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));
//
//        Conta conta = cartao.getConta();
//        if (conta == null) {
//            throw new RuntimeException("Cartão não está vinculado a uma conta.");
//        }
//
//        CategoriaConta categoria = conta.getCategoriaConta();
//        if (categoria == null) {
//            throw new RuntimeException("Conta não possui categoria definida.");
//        }
//
//        BigDecimal valorMensal = BigDecimal.ZERO;
//        BigDecimal valorApolice = BigDecimal.ZERO;
//
//        switch (tipo) {
//            case SEGURO_VIAGEM:
//                if (categoria == CategoriaConta.PREMIUM) {
//                    valorMensal = BigDecimal.ZERO;
//                } else {
//                    valorMensal = new BigDecimal("50.00");
//                }
//                break;
//
//            case SEGURO_FRAUDE:
//                valorApolice = new BigDecimal("5000.00");
//                break;
//
//            default:
//                throw new IllegalArgumentException("Tipo de seguro inválido");
//        }
//
//        Seguro seguro = new Seguro();
//        seguro.setTipo(tipo);
//        seguro.setValorMensal(valorMensal);
//        seguro.setValorApolice(valorApolice);
//        seguro.setAtivo(true);
//        seguro.setCartao(cartao);
//
//        return seguroRepository.save(seguro);
//    }
//    
//    public Optional<Seguro> buscarPorId(Long id) {
//        return seguroRepository.findById(id);
//    }
//
//    public List<Seguro> listarTodos() {
//        return seguroRepository.findAll();
//    }
//
//    public Seguro cancelarSeguro(Long id) {
//        Seguro seguro = buscarPorId(id)
//            .orElseThrow(() -> new SeguroNaoEncontradoException("Seguro não encontrado."));
//
//        // Verifica se o seguro já está cancelado
//        if (!seguro.getAtivo()) {
//            throw new IllegalStateException("Seguro já está cancelado.");
//        }
//
//        // Cancela o seguro
//        seguro.setAtivo(false);
//        return seguroRepository.save(seguro);
//    }
//
//	   
//}
