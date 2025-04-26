💳  Banco Digital - API REST com Java SE 21 e Spring Boot 

Este é um projeto desenvolvido como parte do <b>bootcamp Código de Base da Educ360º</b>, simulando um sistema bancário completo com funcionalidades essenciais como cadastro de clientes, gerenciamento de contas, emissão de cartões e contrato de seguros.
A API foi completamente em regras reais de negócio para bancos digitais.

✅ A aplicação permite operações bancárias como:
- Cadastro de clientes;
- Abertura de contas (Corrente/Poupança);
- Emissão de cartões (Crédito/Débito);
- Realização de transações (PIX, transferências);
- Realização de transações (Depósito/Saque);
- Realização de pagamento(Cartão de Crédito/Débito);
- Gerenciamento de seguros de cartão;
- Aplicação de Taxas (Manutenção Mensal e Rendimento);
- Ver fatura, pagar fatura, alterar limite e etc;
- Validações e aplicação de regras de negócio.

🚀 Tecnologias Utilizadas
- Java 21 – Linguagem principal;
- Spring Boot – Framework backend;
- Spring Data JPA - Persistencia de Dados;
- Spring Security + JWT - Segurança do Projeto com token JWT;
- Hibernate - Framework de mapeamento objeto-relacional (ORM) para Java simplificando a persistência de dados no banco;
- H2 Database - Base de Dados em Memória;
- Maven – Gerenciador de dependências e build;
- Lombok – Geração automática de getters, setters, constructors, etc;
- Postman - Testes das Rotas da API;
- Maven – Gerenciador de dependências e build;
- Bean Validation (Jakarta Validation) – Validação de dados via anotações;
- API – Integração com API externa para dados e validações;
- RestExceptionHandler com anotação como @ControllerAdvice para excessões personalizadas.

💱 Suporte a Múltiplas Moedas (Multiwallet)
- Com a integração da API de câmbio em tempo real, como a  ExchangeRate-API foi possível simplificar a conversão do saldo do usuário em tempo real e em diversas moedas como (BRL, USD, EUR).

🚧 Status do Projeto

🛠️ Em Desenvolvimento
- Implementando logs com a biblioteca do Spring chamada SLF4J (Simple Logging Facade for Java);
- Implementando @EnableScheduling/@EnableAsync e CRON para API aplicar as taxas automaticamente em lote de acordo com as datas agendadas;
- Melhoria na configuração do que o ADMIN pode ou não fazer. Implementei regra de negócio adicional onde o ADMIN não pode fazer movimentação da conta de outros clientes para conta própria;
- Estou em processo de fazer o merge entre a branch newBank (A CERTA) e a main. Por isso, ao clonar o projeto, no momento rode a branch newBank. ;)


🔗 Endpoints da API (usados no Postman)

🧑‍💼 Clientes
- POST /users (Cadastrar novos clientes);
- POST /login (Login no sistema);
- GET /users (Mostrar todos os clientes - Apenas ADMIN);
- GET /users/id (Mostrar cliente por id);
- PUT /users/id (Atualizar cliente por id);
- DELETE /users/id (Deletar cliente por id - Apenas ADMIN).
  
💼 Contas
- GET /contas (Mostrar todas as contas - Apenas ADMIN);
- GET /contas/id (Mostrar conta por id);
- POST /contas (Criar contas - Corrente ou Poupança);
- DELETE /contas/id (Deletar conta por id - Apenas ADMIN);
- PUT /contas/id (Atualizar conta por id);
- POST /contas/id/transferencia (Transferência TED);
- POST /contas/id/pix (Transferência PIX);
- POST /contas/id/deposito (Depósito);
- POST /contas/id/saque (Saque);
- PUT /contas/id/manutencao (Aplica taxa de manutenção da Conta Corrente);
- PUT /contas/id/rendimentos (Aplica taxa de rendimento da Conta Poupança);
- GET /contas/id/saldo (Ver saldo da conta em BRL, USD, EUR).

💳 Cartões
- GET /cartoes (Mostrar todos os cartões - Apenas ADMIN);
- GET /cartoes/id (Mostrar cartão por id);
- POST /cartoes (Criar cartão Débito/Crédito);
- PUT /cartoes/id (Atualizar cartão);
- DELETE /cartoes/id (Deletar cartão);
- POST /cartoes/id/pagamento (Pagamento com cartão);
- PUT /cartoes/id/status (Alterar status do cartão);
- PUT /cartoes/id/senha (Alterar senha do cartão);
- PUT /cartoes/id/limite (Alterar limite do cartão de Crédito);
- PUT /cartoes/id/limite-diario (Alterar limite do Cartão de Débito);
- GET /cartoes/id/fatura (Mostrar fatura);
- POST /cartoes/id/fatura/pagamento (Pagar fatura).

🧰 Seguros
- GET /seguros (Mostrar todos os seguros - Apenas ADMIN);
- GET /seguros/id (Mostrar seguro por id);
- POST /seguros (Contratar seguro);
- PUT /seguros/id (Atualizar seguro);
- DELETE /seguros/id (Deletar seguro - Apenas ADMIN).

📂 Rota de Teste para agendador de Aplicação de Taxas
- PUT /teste-agendador.
  
📈 Futuras Implementações
- Interface Web (Frontend).

▶️ Como Executar
🔍Clone o repositório (Utilize a Branch newBank)
- git clone https://github.com/marciellioliveira/BancoM.git
-  Acesse a pasta cd nome-do-repo
-  Rode com sua IDE favorita (IntelliJ, Eclipse, VSCode)...
