package es.uclm.esi.iso2.sescam.notificaciones.persistencia;

import es.uclm.esi.iso2.sescam.notificaciones.dominio.Notificacion;
import java.util.List;

public interface INotificacionRepository {

    Notificacion guardar(Notificacion notificacion);

    List<Notificacion> buscarPorDestinatario(String destinatarioId);

    void limpiar();
}
