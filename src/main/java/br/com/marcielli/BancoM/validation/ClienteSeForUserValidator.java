//package br.com.marcielli.BancoM.validation;
//
//import br.com.marcielli.BancoM.dto.UserRegisterDTO;
//import br.com.marcielli.BancoM.enuns.Role;
//import jakarta.validation.ConstraintValidator;
//import jakarta.validation.ConstraintValidatorContext;
//
//public class ClienteSeForUserValidator implements ConstraintValidator<ValidarClienteSeForUser, UserRegisterDTO> {
//
//	// Lógica que valida o DTO
//	// Essa classe implementa a lógica: "se role == USER, os campos do cliente têm
//	// que estar preenchidos."
//
//	@Override
//	public boolean isValid(UserRegisterDTO dto, ConstraintValidatorContext context) {
//		if (dto.getRole() != Role.USER) {
//			return true; // Não precisa validar campos de cliente
//		}
//
//		boolean valido = true;
//
//		if (dto.getNome() == null || dto.getNome().isBlank()) {
//			adicionarMensagem("Nome é obrigatório para usuários do tipo USER", context);
//			valido = false;
//		}
//
//		if (dto.getCpf() == null) {
//			adicionarMensagem("CPF é obrigatório para usuários do tipo USER", context);
//			valido = false;
//		}
//
//		if (dto.getCep() == null || dto.getCep().isBlank()) {
//			adicionarMensagem("CEP é obrigatório para usuários do tipo USER", context);
//			valido = false;
//		}
//
//		// Adicione outras verificações conforme necessário (cidade, rua, etc)
//
//		return valido;
//	}
//
//	private void adicionarMensagem(String mensagem, ConstraintValidatorContext context) {
//		context.disableDefaultConstraintViolation();
//		context.buildConstraintViolationWithTemplate(mensagem).addConstraintViolation();
//	}
//}
