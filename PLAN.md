# PLAN.md · Planificación del Proyecto con el Proceso Unificado de Desarrollo (PUD)

> **Asignatura:** Ingeniería del Software II (Curso 2026/27) — UCLM  
> **Equipo 3B1.01:** 7 Ingenieros Consultores  
> **Directriz Metodológica:** 1 Requisito Funcional = 1 Caso de Uso = 1 Iteración  
> **Fecha de Entrega Bloque 1:** 13 de Noviembre de 2026  
> **Fecha de Entrega Bloque 2 (Final):** 15 de Diciembre de 2026  

---

## 1. Estimación Económica y Parámetros del PUD

### 1.1. Suposiciones Base del Enunciado
- **Jornada laboral humana:** 1 semana = 40 horas laborables por consultor humano.
- **Coste Iteración 0 (Inicio):** 1.000 € (Costes fijos de arranque, diseño de arquitectura base y entorno).
- **Coste Iteración N (Transición / Cierre):** 2.000 € (Integración global, empaquetado final, pruebas de carga y manual de usuario).
- **Tarifa media consultor junior de ingeniería software:** 25 €/hora de trabajo humano.
- **Equipo de trabajo:** 7 integrantes humanos + agentes IA asistentes (GitHub Copilot / Gemini / Antigravity).

### 1.2. Análisis Comparativo: Esfuerzo con Agente IA vs. Desarrollo Humano Tradicional
- **Especialización y generación de código:** El agente de IA reduce el tiempo de escritura de código repetitivo (esqueletos MVC, DTOs, interfaces, DAOs básicos) en aproximadamente un **60% - 70%**.
- **Sesgo de optimismo y auditoría humana (*Man-in-the-loop*):** Aunque la IA genere código casi instantáneo, el ingeniero humano debe dedicar un 30% a 40% del tiempo a:
  1. Diseñar y fijar los contratos e interfaces en `modulo-common-interfaces`.
  2. Revisar e inspeccionar el código generado (verificar que no haya alucinaciones ni dependencias ilegales).
  3. Diseñar los casos de prueba unitarios con Mockito.
  4. Realizar revisiones por pares mediante Pull Requests en GitHub.
- **Coste de Licencias IA:** 0 € adicionales (acceso mediante el convenio de campus UCLM para GitHub Copilot y licencias académicas de Google AI / Gemini).

### 1.3. Presupuesto Económico Global del Proyecto

| Fase PUD | Iteraciones | Horas Estimadas (con IA) | Horas Estimadas (sin IA) | Coste Económico (Recursos Humanos) |
| :--- | :--- | :---: | :---: | :---: |
| **Fase 1: Inicio** | Iteración 0 | 40 h | 80 h | 1.000,00 € (Fijo según enunciado) |
| **Fase 2: Elaboración** | Iteraciones 1 a 6 (Oleada 1) | 120 h | 280 h | 3.000,00 € |
| **Fase 3: Construcción** | Iteraciones 7 a 20 (Oleadas 2 y 3) | 280 h | 700 h | 7.000,00 € |
| **Fase 3: Construcción** | Iteraciones 21 a 24 (Oleada 4) | 80 h | 200 h | 2.000,00 € |
| **Fase 4: Transición** | Iteración N (Integración final) | 80 h | 160 h | 2.000,00 € (Fijo según enunciado) |
| **TOTALES** | **26 Iteraciones** | **600 h** | **1.420 h** | **15.000,00 €** |

---

## 2. Calendario de Liberaciones (Releases SemVer)

- **Release v0.1.0 (28/10/2026) · Hito 0 (Inicio):**  
  Esqueleto multimódulo Maven con 8 submódulos, contratos compartidos, DTOs y guardarraíles (`CONTEXT.md`, `DECISIONS.md`, `PLAN.md`).
- **Release v0.2.0 (04/11/2026) · Hito 1 (Cimientos Maestros - Oleada 1):**  
  Catálogo de pruebas (`CU-17`), Gestión de usuarios/seguridad RBAC (`CU-18`, `CU-12`) y Agendas diarias de salas/facultativos (`CU-14`).
- **Release v0.3.0 (13/11/2026) · Hito 2 (Core Clínico Prescripción/Citas - Oleada 2 - Entrega Bloque 1):**  
  Prescripciones médicas (`CU-06`, `CU-07`), Listas de espera (`CU-15`), Citación administrativa definitiva (`CU-13`) y autogestión básica (`CU-01`, `CU-08`).
- **Release v0.4.0 (27/11/2026) · Hito 3 (Ejecución, Visor DICOM e Informes - Oleada 3):**  
  Checklist/Consentimiento (`CU-22`), Realización de prueba (`CU-10`), Auditoría inmutable (`CU-19`), Notificaciones (`CU-02`), Visor/Informes DICOM (`CU-09`) e Historial (`CU-03`).
- **Release v1.0.0 (15/12/2026) · Hito Final (Valor Añadido, Analítica y Cierre - Oleada 4 + It. N):**  
  Reasignación <48h (`CU-23`), Alerta de hallazgos críticos (`CU-24`), Analítica y cuadros de mando (`CU-11`, `CU-16`, `CU-21`), Integración externa (`CU-20`), encuestas (`CU-04`) y pruebas de integración globales.

---

## 3. Desglose Detallado de las Iteraciones por Oleadas Funcionales

### Oleada 1: Configuración, Usuarios y Catálogo (Habilitadores de Datos)
*Objetivo: Disponer de tipos de prueba, salas, turnos y usuarios para poder prescribir y citar.*

| Iteración | Caso de Uso | Módulo | Responsable | Versión Módulo | RADIT Principal |
| :---: | :--- | :--- | :--- | :---: | :---: |
| **It. 1** | **CU-17:** Configurar catálogo de pruebas y salas | `modulo-catalogo-configuracion` | Pedro / Pablo | v0.2.0 | R, A, D, I, T |
| **It. 2** | **CU-18:** Gestionar roles y permisos de usuarios (RBAC) | `modulo-usuarios-seguridad` | Carlos | v0.2.0 | R, A, D, I, T |
| **It. 3** | **CU-12:** Gestionar alta, modificación y baja de pacientes | `modulo-usuarios-seguridad` | Carlos | v0.2.0 | R, A, D, I, T |
| **It. 4** | **CU-14:** Gestionar agendas diarias de médicos y salas | `modulo-citas-agendas` | Pedro / Pablo | v0.2.0 | R, A, D, I, T |

### Oleada 2: Flujo Core de Prescripción y Citación
*Objetivo: Ciclo de vida completo desde la orden médica hasta la asignación de cita.*

| Iteración | Caso de Uso | Módulo | Responsable | Versión Módulo | RADIT Principal |
| :---: | :--- | :--- | :--- | :---: | :---: |
| **It. 5** | **CU-06:** Prescribir prueba diagnóstica puntual | `modulo-prescripcion-seguimiento` | Inés | v0.3.0 | R, A, D, I, T |
| **It. 6** | **CU-07:** Prescribir seguimiento periódico automatizado | `modulo-prescripcion-seguimiento` | Inés | v0.3.0 | R, A, D, I, T |
| **It. 7** | **CU-15:** Gestionar listas de espera, prioridades y huecos | `modulo-citas-agendas` | Pedro / Pablo | v0.3.0 | R, A, D, I, T |
| **It. 8** | **CU-13:** Asignar, reprogramar y cancelar citas definitivamente | `modulo-citas-agendas` | Pedro / Pablo | v0.3.0 | R, A, D, I, T |
| **It. 9** | **CU-01:** Consultar y autogestionar citas por paciente | `modulo-citas-agendas` | Pedro / Pablo | v0.3.0 | R, A, D, I, T |
| **It. 10** | **CU-08:** Consultar estado de solicitudes y agendas disponibles | `modulo-citas-agendas` | Pedro / Pablo | v0.3.0 | R, A, D, I, T |

### Oleada 3: Realización Clínica, DICOM, Seguridad e Informes (Módulo 3 de Jorge)
*Objetivo: Ejecución material de la prueba, consentimiento, visualización de imagen y redacción de informes.*

| Iteración | Caso de Uso | Módulo | Responsable | Versión Módulo | RADIT Principal |
| :---: | :--- | :--- | :--- | :---: | :---: |
| **It. 11** | **CU-22:** Firmar consentimiento informado y checklist seguridad | `modulo-realizacion-informes-dicom` | **Jorge** | v0.4.0 | R, A, D, I, T |
| **It. 12** | **CU-10:** Registrar realización de prueba e incidencias | `modulo-realizacion-informes-dicom` | **Jorge** | v0.4.0 | R, A, D, I, T |
| **It. 13** | **CU-19:** Auditar actividades y accesos a datos clínicos | `modulo-usuarios-seguridad` | Carlos | v0.4.0 | R, A, D, I, T |
| **It. 14** | **CU-02:** Emitir y consultar notificaciones y recordatorios | `modulo-notificaciones-calidad` | Marcos | v0.4.0 | R, A, D, I, T |
| **It. 15** | **CU-09:** Crear y consultar informes clínicos y visor DICOM | `modulo-realizacion-informes-dicom` | **Jorge** | v0.4.0 | R, A, D, I, T |
| **It. 16** | **CU-03:** Consultar historial de pruebas realizadas | `modulo-realizacion-informes-dicom` | **Jorge** | v0.4.0 | R, A, D, I, T |

### Oleada 4: Valor Añadido, Analítica e Interoperabilidad Externa
*Objetivo: Optimización de agendas, alertas de emergencia, cuadros de mando y conectores externos.*

| Iteración | Caso de Uso | Módulo | Responsable | Versión Módulo | RADIT Principal |
| :---: | :--- | :--- | :--- | :---: | :---: |
| **It. 17** | **CU-23:** Reasignación inteligente de huecos liberados (<48h) | `modulo-citas-agendas` | Pedro / Pablo | v1.0.0 | R, A, D, I, T |
| **It. 18** | **CU-24:** Emitir alerta clínica por hallazgo radiológico crítico | `modulo-notificaciones-calidad` | Marcos | v1.0.0 | R, A, D, I, T |
| **It. 19** | **CU-05:** Consultar información de seguimiento periódico | `modulo-prescripcion-seguimiento` | Inés | v1.0.0 | R, A, D, I, T |
| **It. 20** | **CU-04:** Comunicar incidencias y encuestas de satisfacción | `modulo-notificaciones-calidad` | Marcos | v1.0.0 | R, A, D, I, T |
| **It. 21** | **CU-20:** Gestionar integración con sistemas externos (SSO/HCE/PACS) | `modulo-usuarios-seguridad` | Carlos | v1.0.0 | R, A, D, I, T |
| **It. 22** | **CU-11:** Consultar estadísticas de prescripción médica | `modulo-analitica-estadisticas` | Javier | v1.0.0 | R, A, D, I, T |
| **It. 23** | **CU-16:** Generar informes operativos de ocupación y demoras | `modulo-analitica-estadisticas` | Javier | v1.0.0 | R, A, D, I, T |
| **It. 24** | **CU-21:** Generar estadísticas globales y reportes de uso | `modulo-analitica-estadisticas` | Javier | v1.0.0 | R, A, D, I, T |

### Cierre del Proyecto
| Iteración | Alcance | Responsable | Versión Módulo | Coste Asignado |
| :---: | :--- | :--- | :---: | :---: |
| **Iteración N** | Integración global, pruebas E2E con Mockito, manual de usuario y despliegue | Todos (Coordina Jorge / Kevin) | v1.0.0 | 2.000,00 € |

