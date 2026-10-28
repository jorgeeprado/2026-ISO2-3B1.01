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
