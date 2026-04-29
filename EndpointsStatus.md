---
title: Estado de Endpoints API
date: 2026-04-25
tags:
  - api
  - endpoints
  - testing
aliases:
  - Estado Endpoints
cssclasses:
  - docs
---

> [!info]
> Registro rápido del estado de los endpoints para control de pruebas manuales / integración.

# Resumen por entidad

## Usuario
- [ ] GET /usuarios
  - [ ] GET /usuarios/{id}
  - [ ] POST /usuarios
  - [ ] PUT /usuarios/{id}
  - [ ] DELETE /usuarios/{id}

## Estudiante
- [ ] GET /estudiantes
  - [x] GET /estudiantes/{id} — Probado OK
  - [ ] POST /estudiantes
  - [x] PUT /estudiantes/{id} — Probado OK
  - [ ] DELETE /estudiantes/{id}

## Docente
- [ ] GET /docentes
  - [ ] GET /docentes/{id}
  - [ ] POST /docentes
  - [ ] PUT /docentes/{id}
  - [ ] DELETE /docentes/{id}

## Curso
- [ ] GET /cursos
  - [ ] GET /cursos/{id}
  - [ ] POST /cursos
  - [ ] PUT /cursos/{id}
  - [ ] DELETE /cursos/{id}

## Sección
- [ ] GET /secciones
  - [ ] GET /secciones/{id}
  - [ ] POST /secciones
  - [ ] PUT /secciones/{id}
  - [ ] DELETE /secciones/{id}

## Matrícula
- [ ] GET /matriculas
  - [ ] GET /matriculas/{id}
  - [ ] POST /matriculas
  - [ ] PUT /matriculas/{id}
  - [ ] DELETE /matriculas/{id}

---

Notes / Instrucciones de prueba

- Estudiante PUT: el endpoint ahora mergea campos no nulos del DTO en el Usuario asociado (nombre, apellido, numeroDocumento, emailPersonal, idTipoDocumento) y actualiza los datos académicos (ciclo, estadoAcademico, idCarrera). Si no se envía codigoEstudiante, no lo sobreescribe.
- Al marcar una casilla, añadí debajo: fecha, tester y nota breve (ej: "2026-04-25 - arley - PUT /estudiantes/23: OK, actualizó nombre y ciclo").

Ejemplo rápido (PUT /api/v1/estudiantes/23):

```bash
curl -X PUT 'http://localhost:8080/api/v1/estudiantes/23' \
  -H 'Content-Type: application/json' \
  -d '{
    "nombre": "Maria Josefina",
    "apellido": "Ramos de La Cruz Fernandez",
    "numeroDocumento": "71281212",
    "idTipoDocumento": 1,
    "ciclo": 1,
    "estadoAcademico": "ACTIVO",
    "idCarrera": 1,
    "emailPersonal": "maria@gmail.com"
  }'
```

---

Mantengamos este archivo como fuente rápida de estado durante las pruebas.
