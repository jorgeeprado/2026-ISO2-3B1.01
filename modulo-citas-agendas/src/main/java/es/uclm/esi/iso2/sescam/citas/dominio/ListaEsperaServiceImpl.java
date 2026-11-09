package es.uclm.esi.iso2.sescam.citas.dominio;

import es.uclm.esi.iso2.sescam.common.dto.ListaEsperaDTO;
import es.uclm.esi.iso2.sescam.common.interfaces.ListaEsperaService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListaEsperaServiceImpl implements ListaEsperaService {

    private final Map<String, ListaEsperaDTO> entradasPorId = new HashMap<>();

    @Override
    public ListaEsperaDTO registrarEnListaEspera(ListaEsperaDTO entrada) {
        validarEntrada(entrada);
        if (entradasPorId.containsKey(entrada.getId())) {
            throw new IllegalArgumentException("Ya existe una entrada con ese identificador");
        }

        ListaEsperaDTO almacenada = copiarEntrada(entrada);
        entradasPorId.put(almacenada.getId(), almacenada);
        return copiarEntrada(almacenada);
    }

    @Override
    public boolean eliminarDeListaEspera(String id) {
        validarTextoObligatorio(id, "El identificador de la entrada es obligatorio");
        return entradasPorId.remove(id) != null;
    }

    @Override
    public ListaEsperaDTO obtenerEntradaPorId(String id) {
        validarTextoObligatorio(id, "El identificador de la entrada es obligatorio");
        ListaEsperaDTO entrada = entradasPorId.get(id);
        return entrada == null ? null : copiarEntrada(entrada);
    }

    @Override
    public List<ListaEsperaDTO> listarPorTipoPrueba(String tipoPruebaId) {
        validarTextoObligatorio(tipoPruebaId, "El identificador del tipo de prueba es obligatorio");
        List<ListaEsperaDTO> resultado = new ArrayList<>();
        for (ListaEsperaDTO entrada : entradasPorId.values()) {
            if (entrada.getTipoPruebaId().equals(tipoPruebaId)) {
                resultado.add(copiarEntrada(entrada));
            }
        }
        return resultado;
    }

    @Override
    public List<ListaEsperaDTO> listarPorPaciente(String pacienteId) {
        validarTextoObligatorio(pacienteId, "El identificador del paciente es obligatorio");
        List<ListaEsperaDTO> resultado = new ArrayList<>();
        for (ListaEsperaDTO entrada : entradasPorId.values()) {
            if (entrada.getPacienteId().equals(pacienteId)) {
                resultado.add(copiarEntrada(entrada));
            }
        }
        return resultado;
    }

    private void validarEntrada(ListaEsperaDTO entrada) {
        if (entrada == null) {
            throw new IllegalArgumentException("La entrada de lista de espera no puede ser nula");
        }
        validarTextoObligatorio(entrada.getId(), "El identificador de la entrada es obligatorio");
        validarTextoObligatorio(entrada.getPacienteId(), "El identificador del paciente es obligatorio");
        validarTextoObligatorio(entrada.getTipoPruebaId(),
                "El identificador del tipo de prueba es obligatorio");
        if (entrada.getFechaInscripcion() == null) {
            throw new IllegalArgumentException("La fecha de inscripción es obligatoria");
        }
        validarTextoObligatorio(entrada.getEstado(), "El estado de la entrada es obligatorio");
    }

    private void validarTextoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private ListaEsperaDTO copiarEntrada(ListaEsperaDTO entrada) {
        return new ListaEsperaDTO(
                entrada.getId(),
                entrada.getPacienteId(),
                entrada.getTipoPruebaId(),
                entrada.getFechaInscripcion(),
                entrada.getEstado()
        );
    }
}
