package es.uclm.esi.iso2.sescam.usuarios.dominio;

import es.uclm.esi.iso2.sescam.common.dto.PacienteDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.IAuditoriaService;
import es.uclm.esi.iso2.sescam.common.ports.ISisAdmisionClient;
import es.uclm.esi.iso2.sescam.usuarios.persistencia.IPacienteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de dominio para la gestión del ciclo de vida de los pacientes (CU-12 / RF-12).
 * Aplica de forma rigurosa las reglas de negocio del SESCAM:
 * - RN-08: Baja lógica obligatoria (activo = false) para preservar integridad médico-legal.
 * - RN-07: Auditoría síncrona de accesos y mutaciones si se dispone de servicio de auditoría.
 */
public class PacienteService {

    private final IPacienteRepository pacienteRepository;
    private final ISisAdmisionClient sisAdmisionClient;
    private final IAuditoriaService auditoriaService;

    public PacienteService(IPacienteRepository pacienteRepository) {
        this(pacienteRepository, null, null);
    }

    public PacienteService(IPacienteRepository pacienteRepository, ISisAdmisionClient sisAdmisionClient) {
        this(pacienteRepository, sisAdmisionClient, null);
    }

    public PacienteService(IPacienteRepository pacienteRepository,
                           ISisAdmisionClient sisAdmisionClient,
                           IAuditoriaService auditoriaService) {
        this.pacienteRepository = Objects.requireNonNull(pacienteRepository, "El repositorio de pacientes no puede ser nulo");
        this.sisAdmisionClient = sisAdmisionClient;
        this.auditoriaService = auditoriaService;
    }

    /**
     * Da de alta a un nuevo paciente en el censo del sistema (CU-12).
     *
     * @param paciente DTO con los datos del paciente.
     * @return PacienteDTO dado de alta y persistido.
     * @throws PacienteInvalidoException si los datos requeridos son nulos o inválidos.
     * @throws PacienteYaExisteException si ya existe un paciente con el mismo CIP o DNI.
     */
    public PacienteDTO darAltaPaciente(PacienteDTO paciente) {
        validarDatosAlta(paciente);

        String dniLimpio = paciente.getDni().trim().toUpperCase();
        if (pacienteRepository.existePorDni(dniLimpio)) {
            throw new PacienteYaExisteException("Ya existe un paciente censado con el DNI: " + dniLimpio);
        }

        if (paciente.getCip() != null && !paciente.getCip().trim().isEmpty()) {
            String cipLimpio = paciente.getCip().trim().toUpperCase();
            if (pacienteRepository.existePorCip(cipLimpio)) {
                throw new PacienteYaExisteException("Ya existe un paciente censado con el CIP: " + cipLimpio);
            }
            paciente.setCip(cipLimpio);
        } else {
            paciente.setCip(generarCipSescam(dniLimpio));
        }

        if (paciente.getId() == null || paciente.getId().trim().isEmpty()) {
            paciente.setId("PAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        paciente.setDni(dniLimpio);
        paciente.setNombre(paciente.getNombre().trim());
        paciente.setApellidos(paciente.getApellidos().trim());
        paciente.setActivo(true);

        PacienteDTO guardado = pacienteRepository.guardar(paciente);

        registrarAuditoriaSegura("CREACION", guardado.getId(), "Alta de nuevo paciente en el censo");
        return guardado;
    }

    /**
     * Modifica los datos demográficos o de contacto de un paciente existente (CU-12).
     *
     * @param paciente DTO con los datos modificados.
     * @return PacienteDTO actualizado.
     * @throws PacienteInvalidoException  si los datos o el identificador son inválidos.
     * @throws PacienteNoEncontradoException si el paciente no existe.
     * @throws PacienteYaExisteException  si el nuevo DNI o CIP entra en colisión con otro paciente.
     */
    public PacienteDTO modificarPaciente(PacienteDTO paciente) {
        if (paciente == null) {
            throw new PacienteInvalidoException("El paciente a modificar no puede ser nulo");
        }

        String idOCip = paciente.getId() != null && !paciente.getId().trim().isEmpty()
                ? paciente.getId()
                : paciente.getCip();

        if (idOCip == null || idOCip.trim().isEmpty()) {
            throw new PacienteInvalidoException("Se requiere el ID o CIP del paciente para poder modificarlo");
        }

        PacienteDTO existente = pacienteRepository.buscarPorIdOCip(idOCip)
                .orElseThrow(() -> new PacienteNoEncontradoException(
                        "No se encontró ningún paciente con el identificador o CIP: " + idOCip));

        if (paciente.getNombre() != null) {
            if (paciente.getNombre().trim().isEmpty()) {
                throw new PacienteInvalidoException("El nombre del paciente no puede estar vacío");
            }
            existente.setNombre(paciente.getNombre().trim());
        }

        if (paciente.getApellidos() != null) {
            if (paciente.getApellidos().trim().isEmpty()) {
                throw new PacienteInvalidoException("Los apellidos del paciente no pueden estar vacíos");
            }
            existente.setApellidos(paciente.getApellidos().trim());
        }

        if (paciente.getFechaNacimiento() != null) {
            if (paciente.getFechaNacimiento().isAfter(LocalDate.now())) {
                throw new PacienteInvalidoException("La fecha de nacimiento no puede ser futura");
            }
            existente.setFechaNacimiento(paciente.getFechaNacimiento());
        }

        if (paciente.getDni() != null && !paciente.getDni().trim().isEmpty()) {
            String nuevoDni = paciente.getDni().trim().toUpperCase();
            Optional<PacienteDTO> conflictoDni = pacienteRepository.buscarPorDni(nuevoDni);
            if (conflictoDni.isPresent() && !conflictoDni.get().getId().equals(existente.getId())) {
                throw new PacienteYaExisteException("El DNI " + nuevoDni + " ya pertenece a otro paciente censado");
            }
            existente.setDni(nuevoDni);
        }

        if (paciente.getCip() != null && !paciente.getCip().trim().isEmpty()) {
            String nuevoCip = paciente.getCip().trim().toUpperCase();
            Optional<PacienteDTO> conflictoCip = pacienteRepository.buscarPorCip(nuevoCip);
            if (conflictoCip.isPresent() && !conflictoCip.get().getId().equals(existente.getId())) {
                throw new PacienteYaExisteException("El CIP " + nuevoCip + " ya pertenece a otro paciente censado");
            }
            existente.setCip(nuevoCip);
        }

        if (paciente.getTelefono() != null) {
            existente.setTelefono(paciente.getTelefono().trim());
        }

        if (paciente.getEmail() != null) {
            existente.setEmail(paciente.getEmail().trim());
        }

        PacienteDTO actualizado = pacienteRepository.guardar(existente);
        registrarAuditoriaSegura("MODIFICACION", actualizado.getId(), "Modificación de datos del paciente");
        return actualizado;
    }

    /**
     * Ejecuta la baja lógica de un paciente (RN-08 / CU-12).
     * Los pacientes nunca se eliminan físicamente de la base de datos para preservar
     * la integridad médico-legal de citas, prescripciones e informes radiológicos.
     *
     * @param pacienteId Identificador o CIP del paciente a dar de baja.
     * @return true si el paciente existía y se marcó como inactivo (activo = false); false si no existe.
     * @throws IllegalArgumentException si el identificador es nulo o vacío.
     */
    public boolean darBajaLogicaPaciente(String pacienteId) {
        if (pacienteId == null || pacienteId.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del paciente no puede ser nulo o vacío");
        }

        Optional<PacienteDTO> opt = pacienteRepository.buscarPorIdOCip(pacienteId.trim());
        if (opt.isEmpty()) {
            return false;
        }

        PacienteDTO paciente = opt.get();
        paciente.setActivo(false); // Cumplimiento estricto de RN-08
        pacienteRepository.guardar(paciente);

        registrarAuditoriaSegura("MODIFICACION", paciente.getId(), "Baja lógica de paciente (RN-08)");
        return true;
    }

    /**
     * Obtiene el expediente demográfico de un paciente por su ID o CIP (CU-12).
     * En caso de no existir localmente, si se encuentra disponible el cliente del maestro de censo (SisAdmision),
     * realiza la sincronización automática HL7 ADT / FHIR.
     *
     * @param pacienteId Identificador único o CIP del paciente.
     * @return PacienteDTO encontrado o sincronizado; null si no existe en ningún sistema.
     * @throws IllegalArgumentException si el identificador es nulo o vacío.
     */
    public PacienteDTO obtenerPaciente(String pacienteId) {
        if (pacienteId == null || pacienteId.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del paciente no puede ser nulo o vacío");
        }

        Optional<PacienteDTO> opt = pacienteRepository.buscarPorIdOCip(pacienteId.trim());
        if (opt.isPresent()) {
            PacienteDTO encontrado = opt.get();
            registrarAuditoriaSegura("LECTURA", encontrado.getId(), "Consulta de expediente de paciente");
            return encontrado;
        }

        // Intento de sincronización con el maestro hospitalario SisAdmision
        if (sisAdmisionClient != null) {
            PacienteDTO sincronizado = sisAdmisionClient.sincronizarCensoPaciente(pacienteId.trim());
            if (sincronizado != null) {
                if (sincronizado.getId() == null || sincronizado.getId().trim().isEmpty()) {
                    sincronizado.setId("PAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                }
                sincronizado.setActivo(true);
                PacienteDTO guardado = pacienteRepository.guardar(sincronizado);
                registrarAuditoriaSegura("CREACION", guardado.getId(), "Alta por sincronización con censo SisAdmision");
                return guardado;
            }
        }

        return null;
    }

    /**
     * Consulta el listado de pacientes censados.
     *
     * @param soloActivos Si true, devuelve únicamente los pacientes activos (activo == true).
     * @return Lista de pacientes.
     */
    public List<PacienteDTO> listarPacientes(boolean soloActivos) {
        if (soloActivos) {
            return pacienteRepository.listarActivos();
        }
        return pacienteRepository.listarTodos();
    }

    private void validarDatosAlta(PacienteDTO paciente) {
        if (paciente == null) {
            throw new PacienteInvalidoException("El paciente no puede ser nulo");
        }
        if (paciente.getNombre() == null || paciente.getNombre().trim().isEmpty()) {
            throw new PacienteInvalidoException("El nombre del paciente es obligatorio");
        }
        if (paciente.getApellidos() == null || paciente.getApellidos().trim().isEmpty()) {
            throw new PacienteInvalidoException("Los apellidos del paciente son obligatorios");
        }
        if (paciente.getDni() == null || paciente.getDni().trim().isEmpty()) {
            throw new PacienteInvalidoException("El DNI del paciente es obligatorio");
        }
        if (paciente.getFechaNacimiento() != null && paciente.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new PacienteInvalidoException("La fecha de nacimiento no puede ser posterior a la fecha actual");
        }
    }

    private String generarCipSescam(String dni) {
        String baseDni = dni.replaceAll("[^a-zA-Z0-9]", "");
        return "CIP-CLM-" + baseDni;
    }

    private void registrarAuditoriaSegura(String accion, String pacienteId, String detalle) {
        if (auditoriaService != null) {
            try {
                auditoriaService.registrarAccesoClinico(
                        "ADMIN_ADMISION",
                        pacienteId,
                        accion,
                        "HISTORIA_CLINICA",
                        "127.0.0.1"
                );
            } catch (Exception e) {
                // Registro seguro: no bloquea el flujo de negocio pero preserva la resiliencia
            }
        }
    }
}

