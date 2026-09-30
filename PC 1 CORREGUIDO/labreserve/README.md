# LabReserve UTEC

API REST hecha con Spring Boot para reservar equipos de los laboratorios.

## Requisitos
- Java 17
- Maven
- PostgreSQL (o Docker)

## Cómo ejecutar
1. Levantar la base de datos:
   ```
   docker compose up -d
   ```
   Si no usas Docker, crea una base de datos llamada `labreserve` con usuario `postgres` y contraseña `postgres`.
2. Abrir el proyecto en IntelliJ y ejecutar `LabReserveApplication`.
3. La API corre en `http://localhost:8080`.

## Usuarios de prueba
Al iniciar la app se crean:
- admin / Admin2026! (ROLE_ADMIN)
- tech.lab / Tech2026! (ROLE_TECHNICIAN, encargado del FabLab)

Los usuarios que se registran con `/auth/register` son estudiantes.

## Endpoints
- POST /auth/register
- POST /auth/login
- POST /laboratories/{labId}/slots
- GET /equipment-slots
- POST /equipment-slots/{slotId}/reservations
- GET /my-lab-reservations

Para los endpoints protegidos hay que mandar el header `Authorization: Bearer <token>`.

## Pruebas
```
mvn test
```
La prueba de integración usa Testcontainers, así que necesita Docker abierto.
