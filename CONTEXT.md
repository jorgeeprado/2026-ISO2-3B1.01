# CONTEXT.md · Contexto del Dominio y Arquitectura del Sistema (SESCAM)

> **Documento Guardarraíl para Agentes de IA y Desarrolladores Humanos (Spec-Driven Development)**  
> **Proyecto:** Sistema Integral de Gestión de Radiodiagnóstico e Imagen Médica del SESCAM  
> **Asignatura:** Ingeniería del Software II (Curso 2026/27) — Grupo 3B1.01  
> **Metodología:** Proceso Unificado de Desarrollo (PUD) — 1 RF : 1 CU : 1 Iteración  

---

## 1. Descripción General del Dominio
El Servicio de Salud de Castilla-La Mancha (SESCAM) requiere un sistema integral para administrar de manera eficiente las pruebas diagnósticas radiológicas, agendas de trabajo, recursos técnicos (salas y equipos), y el flujo asistencial completo:
1. **Prescripción médica** (ordinaria, preferente, urgente o seguimiento periódico).
2. **Citación y optimización de agendas** (gestión de listas de espera y reasignación de cancelaciones).
3. **Realización técnica de pruebas** (control de contraste, radiación, checklist de seguridad y consentimiento).
4. **Informe radiológico e imagen DICOM** (visor WADO/HTML5, firma electrónica y alertas de hallazgos críticos).
5. **Notificaciones multicanal y calidad percibida** (recordatorios, preparación, encuestas).
6. **Seguridad y auditoría estricta** (RBAC, trazabilidad síncrona inmutable).
7. **Analítica clínica y operativa** (tiempos de espera, ocupación de salas, cuadros de mando globales).

---

## 2. Catálogo de Actores

### 2.1. Actores Principales (Usuarios Humanos)
1. **Paciente (`PACIENTE`):**
   - Consulta y autogestiona sus citas programadas (confirmación, solicitud de cambio o cancelación).
   - Firma digitalmente consentimientos informados y cuestionario previo de seguridad (alergias, marcapasos, función renal).
   - Recibe recordatorios e indicaciones de preparación previa.
   - Consulta su historial longitudinal de estudios radiológicos e informes validados.
   - Comunica incidencias y cumplimenta encuestas de satisfacción.
2. **Médico (`MEDICO` — Hereda de `PACIENTE` según RN-05):**
   - Facultativo prescriptor o médico radiólogo.
   - Prescribe pruebas diagnósticas puntuales y pautas de seguimiento periódico recurrente (cribados, oncología).
   - Consulta el estado de sus prescripciones y agendas disponibles.
   - Registra la realización técnica de la prueba, dosis de radiación y StudyInstanceUID DICOM.
   - Visualiza series en visor DICOM, redacta y firma digitalmente informes diagnósticos.
   - Emite alertas inmediatas de hallazgo clínico crítico inesperado al prescriptor.
   - Consulta estadísticas clínicas sobre sus peticiones y tiempos de respuesta.
3. **Personal Administrativo / Admisión (`ADMINISTRATIVO_ADMISION`):**
   - Ostenta la **potestad exclusiva** de asignar formalmente citas, reprogramarlas o consolidar su cancelación definitiva (RN-02).
   - Administra las agendas diarias de facultativos y salas técnicas.
   - Gestiona listas de espera priorizadas por prioridad clínica y antigüedad FIFO (RN-01).
   - Reasigna de forma inteligente los huecos liberados por cancelaciones de última hora (<48h).
   - Gestiona el alta, modificación y baja lógica de pacientes (RN-08).
   - Genera reportes operativos de ocupación y demoras medias.
4. **Administrador del Sistema (`ADMIN_SISTEMA`):**
   - Configura el catálogo de tipos de prueba (Rx, TAC, RM, US, Mamografía) y parámetros globales (horarios, duraciones, protocolos).
   - Administra roles de usuario y permisos granulares bajo RBAC.
   - Inspecciona y audita el registro síncrono e inmutable de accesos a datos clínicos (RN-07).
   - Supervisa conectores de integración externa.
   - Exporta estadísticas y cuadros de mando globales.

### 2.2. Actores Secundarios (Sistemas Hospitalarios Externos)
5. **SSO Corporativo:** Proveedor de identidad federado del SESCAM (OIDC / SAML 2.0 / JWT).
6. **Historia Clínica Electrónica (HCE):** Repositorio clínico central (mensajería HL7 v2/v3 y FHIR R4: `DiagnosticReport`, `ServiceRequest`).
7. **Sistema de Gestión de Pacientes y Admisión (SisAdmision):** Censo maestro de pacientes sincronizado mediante HL7 ADT / FHIR.
8. **Plataforma RIS/PACS:** Repositorio y archivo central de imágenes médicas bajo estándar DICOM 3.0 (WADO-RS / QIDO-RS) y HL7 ORM/ORU.

---

## 3. Reglas de Negocio Oficiales (RN-01 a RN-08)

- **RN-01 (Priorización de Listas de Espera):** Las solicitudes en lista de espera se ordenan estrictamente por nivel de prioridad clínica (`URGENTE` > `PREFERENTE` > `ORDINARIO`). En caso de igualdad de prioridad, rige criterio FIFO por fecha de prescripción.
- **RN-02 (Potestad Administrativa Exclusiva de Citación):** Los pacientes pueden solicitar cambios o cancelaciones, pero la asignación formal de hueco, reprogramación o cancelación definitiva es potestad exclusiva del Personal Administrativo.
- **RN-03 (Autogeneración de Citas en Seguimiento Periódico):** Al completarse un ciclo o validarse el informe de una prueba periódica pautada (cribados, oncología), el sistema autogenera de forma automática la siguiente solicitud de cita según el intervalo en meses establecido.
- **RN-04 (Bloqueo de Realización por Consentimiento y Seguridad):** El sistema bloquea e impide documentar la realización de pruebas con contraste y Resonancia Magnética (RM) si no consta registrado y validado el consentimiento informado firmado y el cuestionario de seguridad (alergias a yodo, implantes metálicos, filtrado glomerular).
- **RN-05 (Herencia Funcional del Rol Médico):** Todo usuario con rol `MEDICO` hereda automáticamente todos los casos de uso y permisos del rol `PACIENTE` para su propia gestión asistencial a título personal.
- **RN-06 (Alerta Inmediata por Hallazgo Crítico):** Si durante la redacción del informe el radiólogo marca un hallazgo crítico inesperado, el sistema emite de manera síncrona una alerta prioritaria multicanal directa al facultativo prescriptor.
- **RN-07 (Auditoría Síncrona e Inmutable):** El 100% de los accesos de lectura, creación, modificación o firma de datos clínicos, historias e imágenes DICOM genera un registro síncrono e inmutable en el log de auditoría. Quedan expresamente prohibidas las sentencias `UPDATE` y `DELETE` en dicha tabla.
- **RN-08 (Baja Lógica de Pacientes):** Los pacientes nunca son eliminados físicamente de la base de datos para preservar la integridad referencial y médico-legal; se gestionan mediante baja lógica (`activo = false`).

---

## 4. Descomposición Modular y Reparto de Casos de Uso (24 CDUs)

| Módulo Maven | Responsable(s) | Casos de Uso Asignados |
| :--- | :--- | :--- |
| **`modulo-common-interfaces`** | Todo el equipo | DTOs, Enums y Contratos de Servicio Java transversales |
| **`modulo-citas-agendas`** (Módulo 1) | Pedro de la Fuente, Pablo Sánchez | **CU-01:** Consultar/autogestionar citas<br>**CU-08:** Consultar solicitudes/agendas<br>**CU-13:** Asignar/reprogramar citas definitivamente<br>**CU-14:** Administrar agendas diarias de médicos y salas<br>**CU-15:** Gestionar listas de espera y huecos<br>**CU-23:** Reasignación inteligente de huecos liberados (<48h) |
| **`modulo-prescripcion-seguimiento`** (Módulo 2) | Inés Ballesteros | **CU-05:** Consultar información de seguimiento periódico<br>**CU-06:** Prescribir prueba diagnóstica puntual<br>**CU-07:** Prescribir seguimiento periódico automatizado |
| **`modulo-realizacion-informes-dicom`** (Módulo 3) | Jorge Prado Novillo | **CU-03:** Consultar historial de pruebas realizadas<br>**CU-09:** Crear/consultar informes clínicos y visor DICOM<br>**CU-10:** Registrar realización de prueba e incidencias<br>**CU-22:** Firmar consentimiento informado y checklist seguridad |
| **`modulo-notificaciones-calidad`** (Módulo 4) | Marcos Díaz-Pavón Romero | **CU-02:** Emitir/consultar notificaciones y recordatorios<br>**CU-04:** Comunicar incidencias y encuestas de satisfacción<br>**CU-24:** Emitir alerta clínica por hallazgo radiológico crítico |
| **`modulo-usuarios-seguridad`** (Módulo 5) | Carlos de la Dueña Marques | **CU-12:** Alta, modificación y baja lógica de pacientes<br>**CU-18:** Administrar roles y permisos de usuarios (RBAC)<br>**CU-19:** Auditar actividades y accesos a datos clínicos<br>**CU-20:** Integración técnica con sistemas externos (SSO/HCE) |
| **`modulo-catalogo-configuracion`** (Módulo 6) | Pedro de la Fuente, Pablo Sánchez | **CU-17:** Configurar catálogo de pruebas y parámetros del sistema |
| **`modulo-analitica-estadisticas`** (Módulo 7) | Javier Gómez-Lobo Ramírez | **CU-11:** Consultar estadísticas de prescripción médica<br>**CU-16:** Generar informes operativos de ocupación y demoras<br>**CU-21:** Generar estadísticas globales y cuadros de mando |

---

## 5. Requisitos No Funcionales (ISO/IEC 25010)

- **RNF-01 (Desempeño Transaccional):** El 95% de las transacciones (consultas de agenda, prescripción, listas de espera) responde en $\le 1.5$ s bajo concurrencia de 200 usuarios.
- **RNF-02 (Desempeño Visor DICOM):** Carga y primer renderizado de series de imágenes DICOM ($\le 50$ MB) en $\le 3.0$ s en red local hospitalaria.
- **RNF-03 (Interoperabilidad Sanitaria):** 100% de integraciones externas implementadas mediante puertos y adaptadores conformes a HL7 v2.x, HL7 FHIR R4 y DICOM 3.0 (WADO-RS/QIDO-RS).
- **RNF-04 (Autenticación Federada):** Validación de tokens SSO (OIDC/SAML/JWT) en $\le 300$ ms.
- **RNF-05 (Seguridad y Confidencialidad):** Cifrado TLS 1.3 en tránsito y AES-256 en reposo; matriz RBAC estricta (0 accesos indebidos).
- **RNF-06 (No Repudio y Trazabilidad):** Registro síncrono e inmutable de toda lectura/mutación sobre datos clínicos o DICOM.
- **RNF-07 (Disponibilidad y Tolerancia a Fallos):** Disponibilidad $\ge 99.9\%$ (24/7), RTO $\le 1$ h, RPO $\le 15$ min. Desacoplamiento ante caídas de HCE/PACS mediante colas de reintento.
- **RNF-08 (Accesibilidad y Usabilidad):** Cumplimiento WCAG 2.1 nivel AA; flujos de cita en $\le 4$ pasos/clics.
- **RNF-09 (Modularidad SOLID y Maven):** Arquitectura Maven multimódulo (8 submódulos); 0 dependencias entre módulos de negocio; comunicación 100% vía interfaces versionadas con SemVer.
- **RNF-10 (Testabilidad y Gestión de Configuración):** Cobertura de pruebas unitarias/integración $\ge 80\%$, ejecución aislada con Mocks/Stubs; Git Flow estricto con 0 commits directos a `main` e integración vía Pull Request trazada a Issues.
- **RNF-11 (Portabilidad Multiplataforma):** Backend desacoplado en Java 17 que da soporte a clientes de escritorio, web responsiva y móvil.
