package es.uclm.esi.iso2.sescam.notificaciones.dominio;

import es.uclm.esi.iso2.sescam.common.dto.NotificacionDTO;

public interface ICanalNotificacion {

    void enviar(NotificacionDTO notificacion) throws NotificacionDeliveryException;
}
