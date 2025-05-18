package br.com.marcielli.bancom.configuracao;

import br.com.marcielli.bancom.service.UserClienteService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AdminInitializerTest {
	
	@Autowired
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserClienteService userClienteService;

    @Test
    void shouldCallInitAdminUserWhenApplicationReady() {
        eventPublisher.publishEvent(new ApplicationReadyEvent(
                new org.springframework.boot.SpringApplication(),
                new String[]{},
                null, null));

        verify(userClienteService, timeout(1000)).initAdminUser();
    }

}
