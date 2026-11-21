package es.uclm.esi.iso2.sescam.notificaciones;

import es.uclm.esi.iso2.sescam.notificaciones.persistencia.NotificacionEntity;
import es.uclm.esi.iso2.sescam.notificaciones.persistencia.NotificacionJpaRepository;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackageClasses = NotificacionEntity.class)
@EnableJpaRepositories(basePackageClasses = NotificacionJpaRepository.class)
public class NotificacionesConfiguration {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock clockNotificaciones() {
        return Clock.systemUTC();
    }
}
