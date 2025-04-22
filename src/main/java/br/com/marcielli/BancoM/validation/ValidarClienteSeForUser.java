//package br.com.marcielli.BancoM.validation;
//
//import java.lang.annotation.Documented;
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
//import jakarta.validation.Constraint;
//import jakarta.validation.Payload;
//
//@Target({ ElementType.TYPE })
//@Retention(RetentionPolicy.RUNTIME)
//@Constraint(validatedBy = ClienteSeForUserValidator.class)
//@Documented
//public @interface ValidarClienteSeForUser {
//
//	// Anotação personalizada
//
//	String message() default "Campos de cliente são obrigatórios quando o papel for USER.";
//
//	Class<?>[] groups() default {};
//
//	Class<? extends Payload>[] payload() default {};
//}
