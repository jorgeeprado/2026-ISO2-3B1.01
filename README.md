# Sistema Integral de Gestión de Radiodiagnóstico e Imagen Médica (SESCAM)

[![Build and Test](https://img.shields.io/badge/Build-Maven%203.8+-blue.svg)](pom.xml)
[![Java](https://img.shields.io/badge/Java-17%20LTS-orange.svg)](pom.xml)
[![Architecture](https://img.shields.io/badge/Architecture-Multimodule%20SOLID-green.svg)](CONTEXT.md)
[![Methodology](https://img.shields.io/badge/Methodology-PUD%20%7C%20SDD-purple.svg)](PLAN.md)

Proyecto de curso de **Ingeniería del Software II (Curso 2026/27)**  
Grado en Ingeniería Informática — **Escuela Superior de Informática (UCLM, Ciudad Real)**  
**Grupo de Prácticas:** 3B1.01  

---

## 👥 Equipo de Trabajo y Reparto de Módulos

| Integrante | Rol en Equipo | Módulo Asignado | Casos de Uso |
| :--- | :--- | :--- | :--- |
| **Jorge Prado Novillo** | Desarrollador | `modulo-realizacion-informes-dicom` (Módulo 3) | CU-03, CU-09, CU-10, CU-22 |
| **Javier Gómez-Lobo Ramírez** | Desarrollador | `modulo-analitica-estadisticas` (Módulo 7) | CU-11, CU-16, CU-21 |
| **Carlos de la Dueña Marques** | Desarrollador | `modulo-usuarios-seguridad` (Módulo 5) | CU-12, CU-18, CU-19, CU-20 |
| **Pablo Sánchez Orozco** | Desarrollador | `modulo-citas-agendas` (Módulo 1) & `modulo-catalogo-configuracion` (Módulo 6) | CU-01, CU-08, CU-13, CU-14, CU-15, CU-23, CU-17 |
| **Pedro de la Fuente Pintado** | Desarrollador | `modulo-citas-agendas` (Módulo 1) & `modulo-catalogo-configuracion` (Módulo 6) | CU-01, CU-08, CU-13, CU-14, CU-15, CU-23, CU-17 |
| **Inés Ballesteros Ontalba** | Desarrolladora | `modulo-prescripcion-seguimiento` (Módulo 2) | CU-05, CU-06, CU-07 |
| **Marcos Díaz-Pavón Romero** | Desarrollador | `modulo-notificaciones-calidad` (Módulo 4) | CU-02, CU-04, CU-24 |

---

## 🏛️ Arquitectura del Software (Spec-Driven Development)

El sistema está diseñado bajo el principio de **máxima cohesión y mínimo acoplamiento** exigido por la asignatura, estructurado en **8 submódulos Maven**:

```
2026-ISO2-3B1.01/
├── pom.xml (Parent POM con dependencias y gestión centralizada)
├── CONTEXT.md (Guardarraíl: dominio SESCAM, actores, reglas RN-01..08, RNF ISO 25010)
├── DECISIONS.md (Guardarraíl: Architecture Decision Records D-001..08)
├── PLAN.md (Guardarraíl: planificación PUD de 24 iteraciones, costes y releases SemVer)
│
├── modulo-common-interfaces/       <-- CONTRATOS COMPARTIDOS (DTOs, Enums, Interfaces de Servicios)
│
├── modulo-citas-agendas/           <-- Módulo 1 (Pedro y Pablo)
├── modulo-prescripcion-seguimiento/<-- Módulo 2 (Inés)
├── modulo-realizacion-informes-dicom/ <-- Módulo 3 (Jorge)
├── modulo-notificaciones-calidad/  <-- Módulo 4 (Marcos)
├── modulo-usuarios-seguridad/      <-- Módulo 5 (Carlos)
├── modulo-catalogo-configuracion/  <-- Módulo 6 (Pedro y Pablo)
└── modulo-analitica-estadisticas/  <-- Módulo 7 (Javier)
```

> ⚠️ **Regla de oro arquitectónica (RNF-09):**  
> Ningún módulo de negocio puede depender de clases concretas de otro módulo. Los 7 módulos de negocio dependen **únicamente** de `modulo-common-interfaces`. Toda llamada entre módulos se realiza mediante inyección de interfaces, permitiendo tests unitarios aislados con **Mocks/Stubs de Mockito (RNF-10)**.

Cada módulo de negocio implementa la arquitectura multicapa (Patrón MVC):
- `...modulo.presentacion`: Controladores de vista, endpoints REST o consola.
- `...modulo.dominio`: Lógica de negocio, entidades del módulo e implementación de interfaces.
- `...modulo.persistencia`: DAOs y repositorios de datos.

---

## 🛠️ Compilación y Ejecución

El proyecto utiliza **Java 17 LTS** y **Apache Maven**:

```bash
# Compilar todo el proyecto multimódulo
mvn clean compile

# Ejecutar todas las pruebas unitarias
mvn test

# Empaquetar artefactos JAR
mvn clean package
```

---

## 🌿 Flujo de Trabajo en Git (Git Flow)

1. **Ramas principales:**
   - `main`: Código de producción estable (restringido, solo merges vía PR validados).
   - `develop`: Rama principal de integración y desarrollo continuo.
2. **Ramas de funcionalidad (Feature branches):**
   - Se crean siempre a partir de `develop`:
     ```bash
     git checkout develop
     git pull origin develop
     git checkout -b feature/CUXX-descripcion-corta
     ```
3. **Commits trazables:**
   - Cada commit debe referenciar el Issue correspondiente en GitHub:
     ```bash
     git commit -m "feat(modulo-3): especificacion de interfaces para CU-09 (#14)"
     ```
4. **Pull Requests (PR):**
   - Una vez finalizada la tarea y verificados los tests (`mvn test`), se sube la rama y se abre un PR contra `develop` para revisión por pares (*Code Review*).

---

## 📚 Documentación y Enlaces Clave
- [CONTEXT.md](CONTEXT.md): Contexto y reglas del negocio hospitalario.
- [DECISIONS.md](DECISIONS.md): Justificación técnica de decisiones arquitectónicas.
- [PLAN.md](PLAN.md): Planificación temporal, costes, agenda y matriz RADIT.
- [Wiki del Proyecto](https://github.com/KevinRuedas183/ISO24-ISO2-C01/wiki): Actas de reuniones, seguimiento y guías.
