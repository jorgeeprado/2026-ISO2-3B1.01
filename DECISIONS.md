# DECISIONS.md · Registro de Decisiones de Arquitectura y Diseño (ADR)

> **Asignatura:** Ingeniería del Software II (Curso 2026/27) — UCLM  
> **Equipo 3B1.01:** Jorge Prado, Javier Gómez-Lobo, Carlos de la Dueña, Pablo Sánchez, Marcos Díaz-Pavón, Inés Ballesteros, Pedro de la Fuente  
> **Enfoque Metodológico:** Spec-Driven Development (SDD) & Architecture-Centric Unified Process  

---

### D-001 · Herramientas y Agentes de IA para Soporte al Desarrollo
- **Fecha:** 13/10/2026 · **Iteración:** 0 (Fase de Inicio) · **Acta asociada:** Acta nº 1
- **Contexto:** Necesidad de seleccionar el entorno y agentes de IA para el desarrollo asistido (AI4SE) sin caer en el antipatrón del *vibe coding*, garantizando el rol del alumno como ingeniero auditor (*man-in-the-loop*).
- **Decisión:** VS Code con GitHub Copilot Chat (licencia académica UCLM) y Antigravity / Gemini CLI para modelado y revisión guiada por guardarraíles Markdown.
- **Alternativas consideradas:** Eclipse clásico sin IA (mayor coste de codificación manual); OpenCode (menor integración inicial con repositorios institucionales).
- **Motivo / Justificación:** Maximizar la productividad en tareas repetitivas y generación de tests conservando el control determinista de la arquitectura.
- **Consecuencias:** Coste de licencias 0 € para el proyecto. Obligación de mantener `CONTEXT.md` y `DECISIONS.md` actualizados como contexto persistente de los agentes.

---

### D-002 · Descomposición Arquitectónica Multimódulo Maven y Módulo de Interfaces
- **Fecha:** 18/10/2026 · **Iteración:** 0 · **Acta asociada:** Acta nº 1
- **Contexto:** Se requiere estructurar el sistema para 7 integrantes sin acoplamiento cruzado y cumpliendo el RNF-09. La propuesta P2 proponía 21 micro-módulos y P3 proponía 4 paquetes monolith.
- **Decisión:** Arquitectura de **8 submódulos Maven**: 1 módulo transversal de contratos (`modulo-common-interfaces`) y 7 módulos cohesivos de negocio.
- **Regla estricta:** Ningún módulo de negocio puede incluir en su `pom.xml` dependencias hacia otro módulo de negocio. Todos dependen únicamente de `modulo-common-interfaces`.
- **Alternativas consideradas:** Monolito en un único módulo Maven (alto riesgo de colisiones Git y dependencias circulares); 21 micro-módulos (excesiva sobreingeniería y fragmentación).
- **Motivo / Justificación:** Principio de Inversión de Dependencias (DIP) de SOLID. Permite desarrollo y pruebas unitarias 100% aisladas con Mocks/Stubs.
- **Consecuencias:** Modificaciones de contratos intermodulares deben pactarse y versionarse en `modulo-common-interfaces` antes de su uso.

---

### D-003 · Stack Tecnológico Base: Java 17 LTS, Maven y JUnit 5
- **Fecha:** 18/10/2026 · **Iteración:** 0 · **Acta asociada:** Acta nº 1
- **Contexto:** Selección de la versión del lenguaje, motor de construcción y framework de pruebas.
- **Decisión:** Java 17 LTS, Apache Maven 3.8+, JUnit Jupiter 5.10.2 y Mockito 5.11.0.
- **Alternativas consideradas:** Java 21 (posibles incompatibilidades con entornos de laboratorio antiguos); Java 8 (obsoleto).
- **Motivo / Justificación:** Cumplimiento de las directrices del claustro de profesores y compatibilidad completa con Visual Paradigm y herramientas de testing automatizado.
- **Consecuencias:** Código compatible con cualquier IDE (Eclipse, VS Code, IntelliJ IDEA). Cobertura obligatoria $\ge 80\%$ en lógica de negocio (RNF-10).

---

### D-004 · Correspondencia 1 RF : 1 Caso de Uso : 1 Iteración PUD
- **Fecha:** 18/10/2026 · **Iteración:** 0 · **Acta asociada:** Acta nº 1
- **Contexto:** Definición de la granularidad de la planificación del Proceso Unificado de Desarrollo para el Bloque 1.
- **Decisión:** Mapeo estricto y unívoco **1 Requisito Funcional = 1 Caso de Uso = 1 Iteración** (24 Casos de Uso en total: 21 del enunciado + 3 de valor añadido).
- **Alternativas consideradas:** Agrupar varios RFs en casos de uso compuestos (dificulta la trazabilidad y la imputación de costes por iteración).
- **Motivo / Justificación:** Facilita la trazabilidad directa entre la memoria teórica, los Issues en GitHub, el tablero Kanban y los commits.
- **Consecuencias:** 24 iteraciones intermedias entre la Iteración 0 (Infraestructura base) y la Iteración N (Integración y transición final).

---

### D-005 · Separación de la Citación Administrativa y Autogestión del Paciente (RN-02)
- **Fecha:** 18/10/2026 · **Iteración:** 0 · **Acta asociada:** Acta nº 1
- **Contexto:** Definir los límites de responsabilidad en la asignación de agendas para evitar confusiones de rol.
- **Decisión:** Mantener estrictamente separados `CU-01` (Autogestionar citas por el paciente, con carácter de solicitud) y `CU-13` (Gestionar citas definitivamente por el personal administrativo).
- **Motivo / Justificación:** Regla de negocio RN-02: solo el personal de Admisión tiene potestad legal y organizativa para consolidar huecos, reprogramaciones y cancelaciones.
- **Consecuencias:** El paciente solicita modificaciones que quedan en estado pendiente hasta la resolución del administrativo.

---

### D-006 · Herencia Funcional del Rol Médico respecto al Paciente (RN-05)
- **Fecha:** 18/10/2026 · **Iteración:** 0 · **Acta asociada:** Acta nº 1
- **Contexto:** Un facultativo también es usuario del servicio de salud y puede requerir atención médica radiológica personal.
- **Decisión:** En el modelo de seguridad RBAC (`IUsuariosSeguridadService`), el rol `MEDICO` hereda jerárquicamente las capacidades del rol `PACIENTE`.
- **Motivo / Justificación:** Simplifica el modelo de casos de uso y evita duplicación de cuentas o interfaces independientes para facultativos que son pacientes.
- **Consecuencias:** Todo caso de uso accesible por `PACIENTE` (CU-01 a CU-05, CU-22) es accesible de facto por `MEDICO` cuando actúa en calidad de usuario final.

---

### D-007 · Inmutabilidad y Sincronía del Log de Auditoría Clínica (RN-07 / RNF-06)
- **Fecha:** 18/10/2026 · **Iteración:** 0 · **Acta asociada:** Acta nº 1
- **Contexto:** Requisitos legales de protección de datos sanitarios (RGPD, LOPDGDD y ENS).
- **Decisión:** El registro de accesos en `IAuditoriaService` se realiza de forma **síncrona** en cada operación clínica (lectura de DICOM, redacción de informes) y la tabla de auditoría carece de operaciones de modificación (`UPDATE`) o borrado (`DELETE`).
- **Motivo / Justificación:** Garantizar el no repudio absoluto y la trazabilidad médico-legal de inspecciones a historias clínicas.
- **Consecuencias:** Casos de uso como CU-09 (Informes/DICOM) ejecutan un `<<include>>` mandatorio a CU-19 (Auditar acceso).

---

### D-008 · Bloqueo de Realización por Consentimiento y Seguridad Validado (RN-04 / CU-22)
- **Fecha:** 18/10/2026 · **Iteración:** 0 · **Acta asociada:** Acta nº 1
- **Contexto:** Riesgos clínicos graves derivados del uso de contrastes yodados (anafilaxia, daño renal) y campos magnéticos de RM (marcapasos o implantes férricos).
- **Decisión:** La ejecución técnica de pruebas en `IRealizacionInformesService` (`CU-10`) incluye obligatoriamente (`<<include>>`) la verificación del cuestionario de seguridad y consentimiento firmado (`CU-22`).
- **Motivo / Justificación:** Bloqueo sistemático a nivel de capa de dominio si `consentimientoPrevioValidado == false`.
- **Consecuencias:** Evita negligencias médicas y cumple los estándares de seguridad asistencial hospitalaria.

---

### D-009 · Despacho desacoplado y estados de notificación (CU-02.1)
- **Fecha:** 07/11/2026 · **Iteración:** 14 · **Responsable:** Marcos Díaz-Pavón Romero
- **Contexto:** Los recordatorios e informes deben despacharse por más de un canal sin dependencias directas de los módulos de citación o informes.
- **Decisión:** `NotificacionesCalidadService` recibe canales mediante `ICanalNotificacion`, trabaja únicamente con IDs y DTOs comunes y registra los estados `PENDIENTE`, `ENVIADA`, `PARCIAL` o `FALLIDA`. Se intentan todos los canales configurados; cualquier fallo se persiste y se propaga como `NotificacionDispatchException`.
- **Motivo / Justificación:** Inversión de dependencias y trazabilidad explícita de entregas parciales o fallidas.
- **Consecuencias:** El repositorio en memoria sirve como adaptador inicial; los canales reales y una persistencia duradera requieren adaptadores de infraestructura posteriores.

### D-010 · Consulta y persistencia de notificaciones (CU-02.2)
- **Fecha:** 07/11/2026 · **Iteración:** 15
- **Contexto:** CU-02.2 requiere consulta REST filtrada por usuario, fecha y estado de lectura, además de persistir el marcado como leído.
- **Decisión:** El módulo expone la API con Spring Web y usa Spring Data JPA como adaptador de persistencia. H2 se limita a las pruebas; la aplicación integradora configura su datasource. `INotificacionRepository` conserva la abstracción de dominio y el adaptador en memoria se mantiene para pruebas unitarias. El identificador numérico de persistencia se expone aparte del identificador público `String` ya existente.
- **Motivo / Justificación:** Cumplir el contrato de CU-02.2 sin romper la compatibilidad del DTO utilizado por CU-02.1.
- **Consecuencias:** La aplicación integradora debe configurar el datasource de producción y al menos un canal de notificación para habilitar el envío; sin canales, las consultas siguen disponibles y los envíos fallan explícitamente.

### D-011 · Reutilización de `AgendaMedicaDTO` como franja y tipo de recurso separado (CU-14)
- **Fecha:** 10/11/2026 · **Iteración:** 4 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** CU-14 gestiona agendas de médicos y salas. `AgendaMedicaDTO` ya representa una franja (recurso, fecha, horas, intervalo) y lo consume CU-16 (`modulo-analitica-estadisticas`); añadirle un campo cambiaría su constructor canónico y rompería otro módulo.
- **Decisión:** La franja es `AgendaMedicaDTO` sin modificar; su campo `idMedico` se interpreta como identificador de recurso. El tipo viaja aparte en el nuevo enum `TipoRecurso { MEDICO, SALA }` y se persiste junto a la franja (`FranjaRegistro`). Los métodos heredados de `AgendaService` (`crearAgenda`, `listarAgendasPorMedico`) asumen `MEDICO`.
- **Motivo / Justificación:** Reutilizar el DTO existente sin duplicar conceptos (un `FranjaAgendaDTO` nuevo) ni romper CU-16 (añadir `tipoRecurso` al record).
- **Consecuencias:** Un mismo identificador puede usarse como médico y como sala sin colisión. Si se versiona `AgendaMedicaDTO`, el tipo podría integrarse en él.

### D-012 · Contratos de CU-14 separados por cliente (ISP) y generación de turnos
- **Fecha:** 10/11/2026 · **Iteración:** 4 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** La administración de agendas (Admisión) y la reserva de huecos que hará la citación (CU-13) tienen clientes distintos.
- **Decisión:** Se añaden a `modulo-common-interfaces` `GestionAgendaService extends AgendaService` (franjas, consulta de agenda, bloqueos), `DisponibilidadAgendaService` (huecos libres, `obtenerTurno`, `ocuparTurno`, `liberarTurno`), `TurnoAgendaDTO` y `EstadoTurno { LIBRE, OCUPADO, BLOQUEADO }`, sin prefijo `I` como `AgendaService`/`CitaService`. `AgendaServiceImpl` implementa ambas. Cada franja genera ⌊(fin − inicio) / intervalo⌋ turnos (mismo cálculo de capacidad que CU-16); los minutos sobrantes no generan turno y una franja que no admite al menos un turno se rechaza. El identificador del turno es determinista: `idAgenda + "#" + HH:mm`.
- **Motivo / Justificación:** Principio de segregación de interfaces y coherencia con las métricas de ocupación de CU-16.
- **Consecuencias:** CU-13 dependerá solo de `DisponibilidadAgendaService`.

### D-013 · Prohibición de solapes por recurso y fecha
- **Fecha:** 10/11/2026 · **Iteración:** 4 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** CU-14 exige que no existan franjas solapadas de un mismo recurso en una misma fecha.
- **Decisión:** Dos franjas del mismo `TipoRecurso`, recurso y fecha no pueden solaparse. Los intervalos son semiabiertos `[inicio, fin)`, por lo que franjas contiguas (09:00–10:00 y 10:00–11:00) son válidas. Se valida en alta, modificación y `crearAgenda`. Se lanza `IllegalArgumentException` para datos inválidos o identificadores inexistentes e `IllegalStateException` para conflictos de negocio (solape, estado de turno incompatible).
- **Motivo / Justificación:** Usar excepciones estándar evita crear excepciones compartidas nuevas en `modulo-common-interfaces`.
- **Consecuencias:** Los clientes distinguen errores de entrada y conflictos de negocio por el tipo de excepción.

### D-014 · Integridad entre agenda y citas al modificar, eliminar o bloquear
- **Fecha:** 10/11/2026 · **Iteración:** 4 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** Las franjas y turnos de CU-14 serán referenciados por las citas de CU-13.
- **Decisión:** Una franja con algún turno `OCUPADO` no puede modificarse ni eliminarse, y un turno `OCUPADO` no puede bloquearse: antes debe reprogramarse o cancelarse la cita (CU-13). Al modificar una franja se regeneran sus turnos conservando los bloqueos (y su motivo) de los turnos que empiezan a la misma hora. Por compatibilidad con el contrato previo, `crearAgenda` con un identificador existente se trata como modificación; `crearFranja` lo rechaza.
- **Motivo / Justificación:** Evitar citas huérfanas y no perder bloqueos al reajustar horarios.
- **Consecuencias:** Ninguna operación de CU-14 deja citas apuntando a turnos inexistentes.

### D-015 · Bloqueos y desbloqueos con motivo obligatorio
- **Fecha:** 10/11/2026 · **Iteración:** 4 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** CU-14 requiere registrar el motivo de cada bloqueo o desbloqueo de turnos.
- **Decisión:** `bloquearTurno`, `desbloquearTurno` y `bloquearFranja` exigen motivo no vacío, que se guarda en `TurnoAgendaDTO.motivo` (solo el último). Solo se bloquean turnos `LIBRE` y solo se desbloquean turnos `BLOQUEADO`; `bloquearFranja` bloquea únicamente los turnos libres y devuelve los afectados.
- **Motivo / Justificación:** Trazabilidad mínima del estado del turno sin introducir un registro de eventos.
- **Consecuencias:** No se mantiene historial de bloqueos; si se exige trazabilidad completa, deberá añadirse un registro de eventos de agenda.

### D-016 · Persistencia en memoria y cobertura con JaCoCo en `modulo-citas-agendas`
- **Fecha:** 10/11/2026 · **Iteración:** 4 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** El módulo debe seguir MVC con persistencia simulada y cumplir RNF-10 (cobertura > 80 %).
- **Decisión:** Persistencia mediante DAOs en memoria (`IFranjaRepository`/`FranjaRepositoryInMemory`, `ITurnoRepository`/`TurnoRepositoryInMemory`) inyectados por constructor en `AgendaServiceImpl`. JaCoCo 0.8.12 en el pom del módulo con umbral ≥ 80 % de instrucciones, igual que `modulo-notificaciones-calidad`.
- **Motivo / Justificación:** Aislar el dominio con mocks en las pruebas y verificar la cobertura en cada `mvn test`.
- **Consecuencias:** La validación oficial se realiza con JDK 17 (D-003); no se añaden ajustes de Mockito para versiones posteriores del JDK.

### D-017 · Entrada de lista de espera como copia de la prescripción (CU-15)
- **Fecha:** 10/11/2026 · **Iteración:** 7 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** CU-15 necesita la prioridad clínica y la fecha de prescripción de cada solicitud. `SolicitudPruebaDTO` es un POJO mutable que pertenece al módulo de prescripción, y `EstadoSolicitud` describe el ciclo de vida de la prescripción, no el de la lista de espera.
- **Decisión:** `inscribir` recibe el `SolicitudPruebaDTO` completo, que debe estar `PENDIENTE_CITACION`, y copia sus datos en el nuevo record `EntradaListaEsperaDTO`, identificado por `idSolicitud`. `SolicitudPruebaDTO.fechaSolicitud` se toma como fecha de prescripción. La entrada tiene su propio estado, el nuevo enum `EstadoEntradaEspera { EN_ESPERA, CITADA, RETIRADA }`. Hay una lista por `tipoPruebaId`.
- **Motivo / Justificación:** Que un cambio externo en el DTO no altere el orden sin pasar por el servicio, cumplir RNF-09 sin depender de `IPrescripcionService` (aún sin implementar) y no confundir la retirada de la lista con la cancelación de la prescripción.
- **Consecuencias:** Los cambios de prioridad se hacen con `cambiarPrioridad`. Si el módulo de prescripción modifica la solicitud, la lista no se actualiza por sí sola.

### D-018 · Orden de la lista de espera según RN-01
- **Fecha:** 10/11/2026 · **Iteración:** 7 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** RN-01 ordena por prioridad clínica (`URGENTE` > `PREFERENTE` > `ORDINARIO`) y, a igual prioridad, FIFO por fecha de prescripción.
- **Decisión:** El comparador `OrdenListaEspera` ordena por rango explícito de prioridad, después por fecha de prescripción (de más antigua a más reciente), después por fecha de inscripción y, por último, por `idSolicitud`. Un cambio de prioridad o una reincorporación conservan la fecha de prescripción y la de inscripción.
- **Motivo / Justificación:** Un orden siempre igual y comprobable en los tests, sin depender del `ordinal()` del enum `Prioridad`.
- **Consecuencias:** Una solicitud que cambia de prioridad se coloca en el nuevo nivel según su antigüedad real, y una cita cancelada no hace perder la posición.

### D-019 · Contratos de CU-15 separados por cliente (ISP)
- **Fecha:** 10/11/2026 · **Iteración:** 7 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** Admisión mantiene las listas, mientras que la citación (CU-13) y la reasignación de huecos (CU-23) solo necesitan candidatos y cambios de estado.
- **Decisión:** Se añaden a `modulo-common-interfaces` `GestionListaEsperaService` (inscribir, cambiar prioridad, retirar, consultar lista y posición, obtener entrada) y `CandidatosListaEsperaService` (proponer candidatos para un turno, marcar como citada, reincorporar), sin prefijo `I`. `ListaEsperaServiceImpl` implementa ambas.
- **Motivo / Justificación:** Principio de segregación de interfaces, igual que en D-012.
- **Consecuencias:** CU-13 y CU-23 dependerán solo de `CandidatosListaEsperaService`.

### D-020 · Los huecos de CU-15 se obtienen de las agendas de CU-14
- **Fecha:** 10/11/2026 · **Iteración:** 7 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** CU-15 gestiona huecos, pero los turnos ya los modela CU-14, y la asignación formal del hueco corresponde a CU-13 (RN-02).
- **Decisión:** Los huecos son los turnos `LIBRE` de `DisponibilidadAgendaService`; no hay un modelo de huecos propio. Una entrada es compatible con un turno si su `tipoPruebaId` coincide con el `idPruebaAsociada` de la franja del turno (`AgendaService.obtenerAgendaPorId`); si la franja no tiene prueba asociada (nula o vacía), admite cualquier prueba. `proponerCandidatos` no reserva ni ocupa el turno. La ventana de 48 h y la reasignación automática quedan para CU-23.
- **Motivo / Justificación:** Evitar duplicar el modelo de turnos y no invadir CU-13 ni CU-23.
- **Consecuencias:** CU-15 depende de los contratos de CU-14, por lo que su rama parte de la de CU-14. CU-13 debe llamar a `marcarCitada` al consolidar la cita y a `reincorporar` al cancelarla.

### D-021 · Persistencia en memoria, errores y trazabilidad de la lista de espera
- **Fecha:** 10/11/2026 · **Iteración:** 7 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** El módulo sigue MVC con persistencia simulada y debe cumplir RNF-10.
- **Decisión:** Persistencia mediante `IListaEsperaRepository`/`ListaEsperaRepositoryInMemory`, inyectado por constructor junto con un `Clock` para la fecha de inscripción. Errores como en D-013: `IllegalArgumentException` para datos inválidos, duplicados o identificadores inexistentes, e `IllegalStateException` para estados incompatibles. Las entradas no se borran: retirar las deja en `RETIRADA` y una solicitud ya inscrita no puede volver a inscribirse. Cambio de prioridad, retirada y reincorporación exigen motivo; solo se guarda el último, como en D-015.
- **Motivo / Justificación:** Coherencia con CU-14 y pruebas deterministas.
- **Consecuencias:** No hay historial de cambios de prioridad; si se exige trazabilidad completa, deberá añadirse un registro de eventos de la lista de espera.

### D-022 · Contratos de CU-13 y contratos de citas heredados
- **Fecha:** 10/11/2026 · **Iteración:** 8 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** `ICitasAgendasService` mezcla CU-01, CU-08, CU-13 y CU-23, nadie lo implementa y asigna citas con fecha, hora y sala libres, sin pasar por los turnos de agenda. `CitaService`/`CitaServiceImpl` es un CRUD que permite crear citas sin turno ni lista de espera.
- **Decisión:** Se añaden a `modulo-common-interfaces` `CitacionAdministrativaService` (asignar, reprogramar y cancelar citas) y `ConsultaCitasService` (consulta de solo lectura), sin prefijo `I` y separados por cliente. `CitacionServiceImpl` implementa ambas. `ICitasAgendasService`, `CitaService` y `CitaServiceImpl` se consideran **heredados**: no se modifican ni se usan en CU-13.
- **Motivo / Justificación:** Principio de segregación de interfaces y garantizar RN-02 obligando a que toda cita ocupe un turno de CU-14 y salga de la lista de espera de CU-15.
- **Consecuencias:** Mientras convivan, el almacén oficial de citas es el de `CitacionServiceImpl`. Retirar los contratos heredados queda pendiente de acuerdo del equipo.

### D-023 · Enlace entre cita y turno en la persistencia
- **Fecha:** 10/11/2026 · **Iteración:** 8 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** `CitaDTO` no tiene identificador de turno, y modificarlo afectaría a CU-16 (`modulo-analitica-estadisticas`).
- **Decisión:** `CitaRegistro(cita, idTurno, motivoAsignacion, motivoReprogramacion)` se guarda en `ICitaRepository`/`CitaRepositoryInMemory` con copias defensivas, porque `CitaDTO` es mutable. Cada cita ocupa un único turno: `fechaHora` y `duracionMinutos` se toman del turno; si el turno es de sala se informa `salaId`, y si es de médico `medicoRadiologoId`; el otro campo queda a `null`. Los identificadores son legibles (`CITA-000001`) y los genera un `Supplier<String>` inyectado.
- **Motivo / Justificación:** Mismo enfoque que D-011 para no modificar contratos existentes.
- **Consecuencias:** Una cita no reserva a la vez sala y radiólogo; si se exige, habrá que ampliar el modelo.

### D-024 · Orquestación de CU-13 con compensación
- **Fecha:** 10/11/2026 · **Iteración:** 8 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** Asignar, reprogramar y cancelar modifican el turno (CU-14), la lista de espera (CU-15) y la cita, sin transacciones entre servicios.
- **Decisión:** Las validaciones se hacen antes de modificar nada. Después:
  - **Asignar:** `ocuparTurno` → `marcarCitada` → guardar la cita.
  - **Reprogramar:** `ocuparTurno` (nuevo) → `liberarTurno` (antiguo) → guardar la cita.
  - **Cancelar:** `liberarTurno` → `reincorporar` → guardar la cita.
  
  Si falla un paso, se deshacen los anteriores en orden inverso y se relanza la excepción original. Si una compensación falla, su excepción se añade como suprimida y se continúa con las demás.
- **Motivo / Justificación:** Primero se asegura el recurso escaso (el turno) y lo último es la persistencia propia.
- **Consecuencias:** Si falla también una compensación puede quedar un estado inconsistente, detectable por la excepción suprimida.

### D-025 · Estados de la cita, de la lista de espera y de la solicitud
- **Fecha:** 10/11/2026 · **Iteración:** 8 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Decisión:**
  - Asignar crea la cita `PROGRAMADA` y la entrada de la lista pasa a `CITADA`.
  - Reprogramar mantiene la cita `PROGRAMADA` (también si estaba `CONFIRMADA`) con la nueva fecha y hora; la lista no cambia.
  - Cancelar deja la cita `CANCELADA` con motivo y siempre reincorpora la solicitud a la lista conservando su antigüedad (RN-01).
  - Solo las citas `PROGRAMADA` o `CONFIRMADA` se pueden reprogramar o cancelar.
  - CU-13 **no modifica `EstadoSolicitud`**: pertenece al módulo de prescripción y no hay contrato para cambiarlo (RNF-09).
- **Motivo / Justificación:** CU-16 cuenta como ocupadas las citas `PROGRAMADA`, `CONFIRMADA` y `REALIZADA`, pero no las `REPROGRAMADA`.
- **Consecuencias:** La solicitud sigue `PENDIENTE_CITACION` en el módulo de prescripción y en CU-16 aunque tenga cita. Queda pendiente acordar con Inés y Pablo un contrato para actualizarla.

### D-026 · RN-01 y RN-02 en la asignación
- **Fecha:** 10/11/2026 · **Iteración:** 8 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Decisión:**
  - Al asignar se consultan los candidatos del turno sin límite (`Integer.MAX_VALUE`), para que ninguna solicitud quede fuera aunque esté al final de la lista.
  - Se puede asignar cualquier candidato compatible; si no es el primero según RN-01, el motivo es obligatorio y se guarda en `CitaRegistro.motivoAsignacion`.
  - No se asignan ni reprograman citas en turnos que ya han empezado según el `Clock` inyectado; cancelar se permite en cualquier cita activa.
  - RN-02 se garantiza porque solo `CitacionAdministrativaService` ocupa o libera turnos por citas y marca entradas como citadas. La comprobación de roles corresponde a CU-18.
- **Consecuencias:** Los saltos de orden quedan trazados, pero solo se guarda el motivo de la asignación y el de la última reprogramación.

### D-027 · Consulta de solicitudes y disponibilidad de agendas (CU-08)
- **Fecha:** 11/11/2026 · **Iteración:** 10 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** CU-08 debe mostrar al médico sus solicitudes y los huecos disponibles sin duplicar la regla de disponibilidad de CU-14 ni acoplar módulos de negocio.
- **Decisión:** `ConsultaSolicitudesAgendasServiceImpl` consulta las solicitudes mediante `IPrescripcionService` y delega los huecos libres en `DisponibilidadAgendaService.consultarHuecosLibres`. CU-08 no mantiene un repositorio propio de agendas y devuelve copias defensivas de los DTO mutables de solicitud.
- **Motivo / Justificación:** Reutilizar el contrato común de CU-14 mantiene una única fuente de verdad para el estado y filtrado de turnos, y cumple RNF-09.
- **Consecuencias:** Por D-025, CU-13 no modifica `EstadoSolicitud`; por tanto, una solicitud ya citada puede seguir apareciendo como `PENDIENTE_CITACION` en CU-08. CU-08 muestra el estado recibido de prescripción y no lo infiere ni lo modifica. Cualquier sincronización futura requiere un contrato acordado con el módulo de prescripción.

### D-031 · Reasignación de huecos liberados (CU-23)
- **Fecha:** 12/11/2026 · **Iteración:** 17 · **Responsables:** Pedro de la Fuente, Pablo Sánchez
- **Contexto:** Al cancelar una cita, CU-13 libera el turno y reincorpora la solicitud a la lista de espera. CU-23 permite ofrecer explícitamente ese hueco a otra solicitud compatible sin alterar RNF-09 ni duplicar la persistencia de CU-13.
- **Decisión:**
  - Solo se consideran turnos cuyo inicio sea estrictamente posterior al instante actual y estrictamente anterior a las 48 horas siguientes, calculados con el `Clock` inyectado. Se excluyen el instante exacto de 48 horas y los turnos ya empezados.
  - Se excluye de la selección la solicitud cancelada, aunque `cancelarCita` ya la haya reincorporado a la lista de espera.
  - Se conserva el orden de candidatos de RN-01. Si se selecciona una solicitud distinta del primer candidato, se proporciona el motivo obligatorio exigido por RN-01/RN-02.
  - La persistencia se realiza únicamente mediante `CitacionAdministrativaService.asignarCita` (CU-13). Si falla, se propaga el error y no se intenta asignar a otro candidato.
  - La invocación de CU-23 es explícita y no está enlazada automáticamente a `cancelarCita`.
- **Motivo / Justificación:** Mantener una ventana temporal determinista, respetar el orden clínico, y asegurar que la ocupación del turno y la actualización de la lista sigan bajo la única vía administrativa de CU-13.
- **Consecuencias:** Si no hay candidatos elegibles, el hueco permanece libre. La cancelación por sí sola no inicia la reasignación; el cliente debe invocar CU-23 expresamente.
