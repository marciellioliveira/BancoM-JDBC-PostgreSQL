//package br.com.marcielli.BancoM.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import br.com.marcielli.BancoM.service.AgendarAplicacaoTaxasContasService;
//
//@RestController
//public class TesteController {
//	
//    @Autowired
//    private AgendarAplicacaoTaxasContasService agendador;
//
//    @GetMapping("/teste-agendador")
//    public String teste() {
//        agendador.aplicarTaxaManutencaoMensal();
//        return "Método executado manualmente!";
//    }
//}