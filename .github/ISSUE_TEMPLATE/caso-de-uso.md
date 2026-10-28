---
name: Caso de Uso (Iteración PUD)
about: Plantilla oficial obligatoria para cada Caso de Uso / Iteración del PUD (1 RF = 1 CU = 1 Iteración)
title: "CU-XX: [Nombre del Caso de Uso] (modulo-[nombre] vX.Y.Z)"
labels: ["caso-de-uso", "pud"]
assignees: ""
---

## 📌 Identificación del Caso de Uso e Iteración
- **Identificador:** CU-XX
- **Requisito Funcional asociado:** RF-XX
- **Módulo Maven:** `modulo-[nombre]`
- **Versión SemVer objetivo:** `vX.Y.Z`
- **Fase PUD:** [Inicio | Elaboración | Construcción | Transición]
- **Iteración:** Iteración XX
- **Acta de reunión asociada:** [Acta nº X](enlace-a-wiki)
- **Responsable:** @usuario

---

## 🎯 Objetivos y Disciplinas RADIT Involucradas
*Marca las disciplinas de la Ingeniería del Software abordadas en esta iteración:*
- [ ] **R - Requisitos:** Especificación del caso de uso, pre/postcondiciones y flujo de eventos.
- [ ] **A - Análisis:** Modelo conceptual, diagramas de interacción/secuencia del caso de uso.
- [ ] **D - Diseño:** Definición de clases MVC, patrones de diseño y refinamiento de interfaces.
- [ ] **I - Implementación:** Codificación en Java 17 respetando contratos y arquitectura multicapa.
- [ ] **T - Testing (Pruebas):** Casos de prueba unitarios JUnit 5 y Mocks aislados con Mockito ($\ge 80\%$ cobertura).

---

## 🔌 Contratos e Interfaces Involucrados
*Especifica qué interfaces de `modulo-common-interfaces` se implementan o se consumen:*
- **Interfaces implementadas:** `es.uclm.esi.iso2.sescam.common.interfaces.I...`
- **Interfaces consumidas (inyección con Mocks):** `es.uclm.esi.iso2.sescam.common.interfaces.I...`
- **DTOs utilizados:** `...DTO`
- **Reglas de negocio aplicadas:** [RN-01 .. RN-08]

---

## ✅ Criterios de Aceptación (Definition of Done - DoD)
- [ ] Especificación del caso de uso documentada en Visual Paradigm (con campos *Project Management* cumplimentados: Fase, Iteración, Disciplina, etc.).
- [ ] Clases implementadas en su respectiva capa (`presentacion`, `dominio`, `persistencia`) sin dependencias directas a otros módulos de negocio.
- [ ] Pruebas unitarias JUnit 5 implementadas con Mockito ejecutándose exitosamente (`mvn test`).
- [ ] Cobertura de pruebas unitarias $\ge 80\%$ sobre la lógica de negocio.
- [ ] Código compilando limpiamente sin warnings (`mvn clean compile`).
- [ ] Pull Request abierta contra la rama `develop` referenciando este issue en el commit.
- [ ] Revisión de código por pares aprobada y mergeada sin conflictos.

---

## ⏱️ Estimación de Esfuerzo (Horas)
*Comparativa obligatoria de esfuerzo según directrices de la asignatura:*
- **Estimación SIN usar agente de IA:** [X] horas
- **Estimación CON agente de IA (AI4SE):** [Y] horas
- **Horas reales dedicadas (al cerrar el issue):** [Z] horas
- **Desviación / Reflexión crítica:** [Explicación de diferencias encontradas]
