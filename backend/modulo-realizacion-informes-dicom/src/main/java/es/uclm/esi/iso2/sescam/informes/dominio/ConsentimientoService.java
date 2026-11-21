package es.uclm.esi.iso2.sescam.informes.dominio;

import es.uclm.esi.iso2.sescam.common.dto.ConsentimientoDTO;
import es.uclm.esi.iso2.sescam.informes.persistencia.IConsentimientoRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de dominio para la gestión y validación de consentimientos informados y checklist de seguridad.
 * Implementa las reglas de negocio de seguridad del paciente del SESCAM (CU-22 / RF-22 / RN-04).
 */
public class ConsentimientoService {

    /**
     * Umbral mínimo de tasa de filtrado glomerular (TFG en ml/min/1.73m²) para administración segura de contraste.
     * Valores inferiores a 30 representan insuficiencia renal severa (estadio 4-5) y contraindican el contraste.
     */
    public static final double UMBRAL_MINIMO_FILTRADO_GLOMERULAR = 30.0;

    private final IConsentimientoRepository consentimientoRepository;

    public ConsentimientoService(IConsentimientoRepository consentimientoRepository) {
        this.consentimientoRepository = Objects.requireNonNull(consentimientoRepository, "El repositorio no puede ser nulo");
    }

    /**
     * Procesa la firma digital del consentimiento informado y el checklist de seguridad (CU-22 / RF-22).
     *
     * @param consentimiento DTO con las respuestas del checklist de seguridad y firma.
     * @return El consentimiento validado, firmado y persistido.
     * @throws IllegalArgumentException si los datos obligatorios del paciente o prueba son inválidos.
     * @throws IllegalStateException    si el checklist de seguridad previo no ha sido completado.
     */
    public ConsentimientoDTO firmarConsentimiento(ConsentimientoDTO consentimiento) {
        if (consentimiento == null) {
            throw new IllegalArgumentException("El consentimiento a firmar no puede ser nulo");
        }
        if (consentimiento.getPacienteId() == null || consentimiento.getPacienteId().trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del paciente es obligatorio para firmar el consentimiento");
        }
        if (consentimiento.getTipoPruebaId() == null || consentimiento.getTipoPruebaId().trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del tipo de prueba es obligatorio");
        }
        if (!consentimiento.isChecklistSeguridadCompletado()) {
            throw new IllegalStateException("No es posible firmar el consentimiento sin haber completado el cuestionario de seguridad previo");
        }

        if (consentimiento.getId() == null || consentimiento.getId().trim().isEmpty()) {
            consentimiento.setId("CONS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        if (consentimiento.getFechaFirma() == null) {
            consentimiento.setFechaFirma(LocalDateTime.now());
        }

        consentimiento.setFirmado(true);

        if (consentimiento.getFirmaDigitalHash() == null || consentimiento.getFirmaDigitalHash().trim().isEmpty()) {
            consentimiento.setFirmaDigitalHash(generarFirmaDigitalHash(consentimiento));
        }

        return consentimientoRepository.guardar(consentimiento);
    }

    /**
     * Verifica si un paciente cuenta con consentimiento informado y cuestionario de seguridad válidos (RN-04).
     * Aplica el bloqueo clínico en caso de detectar contraindicaciones médicas de seguridad (marcapasos en RM
     * o insuficiencia renal severa con TFG < 30 ml/min para pruebas con contraste).
     *
     * @param pacienteId   Identificador único del paciente.
     * @param tipoPruebaId Identificador del tipo de prueba solicitada.
     * @return true si el consentimiento es válido y seguro para la prueba; false si está bloqueado o ausente.
     */
    public boolean validarConsentimientoPrevio(String pacienteId, String tipoPruebaId) {
        if (pacienteId == null || tipoPruebaId == null) {
            return false;
        }

        Optional<ConsentimientoDTO> opt = consentimientoRepository.buscarPorPacienteYPrueba(pacienteId, tipoPruebaId);
        if (opt.isEmpty()) {
            return false;
        }

        ConsentimientoDTO c = opt.get();

        // Verificaciones formales básicas
        if (!c.isFirmado() || !c.isChecklistSeguridadCompletado() || c.getFirmaDigitalHash() == null) {
            return false;
        }

        // Regla de Negocio RN-04: Bloqueo de seguridad clínica
        // Contraindicación absoluta para campos magnéticos (Resonancia Magnética)
        if (c.isPortaMarcapasosOImplanteMetalico()) {
            return false;
        }

        // Contraindicación severa para administración de contrastes yodados / gadolinio
        if (c.getValorFiltradoGlomerular() > 0 && c.getValorFiltradoGlomerular() < UMBRAL_MINIMO_FILTRADO_GLOMERULAR) {
            return false;
        }

        return true;
    }

    /**
     * Consulta el consentimiento registrado para un paciente y tipo de prueba.
     *
     * @param pacienteId   Identificador del paciente.
     * @param tipoPruebaId Identificador de la prueba.
     * @return Optional con el consentimiento si fue registrado previamente.
     */
    public Optional<ConsentimientoDTO> consultarConsentimiento(String pacienteId, String tipoPruebaId) {
        return consentimientoRepository.buscarPorPacienteYPrueba(pacienteId, tipoPruebaId);
    }

    private String generarFirmaDigitalHash(ConsentimientoDTO c) {
        String data = c.getPacienteId() + ":" + c.getTipoPruebaId() + ":" + c.getFechaFirma();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(data.hashCode());
        }
    }
}

