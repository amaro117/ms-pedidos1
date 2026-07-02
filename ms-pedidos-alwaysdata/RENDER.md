# Despliegue de ms-pedidos en Render

## Opción A — Blueprint automático (`render.yaml`)
1. Sube este proyecto a un repo de GitHub.
2. En Render: **New → Blueprint** → conecta el repo. Render detecta `render.yaml`
   y crea el servicio automáticamente con el healthcheck y las variables ya definidas.

## Opción B — Manual
1. **New → Web Service** → conecta el repo → Runtime: **Docker**.
2. Dockerfile Path: `Dockerfile` / Docker Context: `.`
3. Variables de entorno:
   ```
   SPRING_PROFILES_ACTIVE=prod
   MS_PRODUCTOS_URL=http://localhost:8081   # o la URL real si despliegas ms-productos también
   DB_HOST=postgresql-tucuenta.alwaysdata.net
   DB_PORT=5432
   DB_NAME=tucuenta_pedidos
   DB_USER=tu_usuario_alwaysdata
   DB_PASSWORD=tu_password_alwaysdata
   ```
   Estos 5 últimos son los datos que ves en el panel de alwaysdata en
   **Databases → PostgreSQL**. No necesitas configurar `PORT` manualmente: Render lo
   inyecta solo y `application.yml` ya lo lee con `${PORT:8080}`.
4. Activa **Generate Domain** para obtener la URL pública.

## Base de datos: alwaysdata (PostgreSQL)
1. Crea tu cuenta en https://www.alwaysdata.com (gratis).
2. **Databases → PostgreSQL → Add a database**, ponle nombre (ej: `tucuenta_pedidos`).
3. Crea o usa un **Database user** con todos los privilegios sobre esa base.
4. Anota host, puerto, nombre de base, usuario y contraseña — van directo en las
   variables `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` de Render.
5. **No necesitas crear tablas a mano**: Flyway corre automáticamente al iniciar la app
   y ejecuta `src/main/resources/db/migration/V1__create_pedidos_table.sql`, que crea la
   tabla `pedidos` y le inserta 2 registros de ejemplo.

## Verificar que quedó arriba
```bash
curl https://<tu-servicio>.onrender.com/actuator/health
curl https://<tu-servicio>.onrender.com/api/pedidos
curl https://<tu-servicio>.onrender.com/swagger-ui.html
```
Si `GET /api/pedidos` devuelve los 2 pedidos de ejemplo (`productoId: 1` y `productoId: 3`),
Flyway corrió bien y la app está leyendo desde PostgreSQL real, no desde memoria.

## Qué se agregó respecto a la versión anterior
- **YAML multi-perfil**: `application.yml` (base) + `application-dev.yml` / `-docker.yml` / `-prod.yml`.
- **Puerto dinámico**: `server.port: ${PORT:8080}` — antes estaba fijo en `8082`, lo que
  hacía fallar el healthcheck de Render.
- **Swagger/OpenAPI**: `springdoc-openapi-starter-webmvc-ui` + anotaciones `@Tag`, `@Operation`,
  `@ApiResponse(s)` en `PedidoController`. Disponible en `/swagger-ui.html`.
- **Spring Boot Actuator**: expone `/actuator/health`, usado como healthcheck de Render.
- **Persistencia real con PostgreSQL (alwaysdata)**: `Pedido` pasó de ser un POJO en un
  `ArrayList` a una entidad `@Entity` con `spring-boot-starter-data-jpa`, y `PedidoRepository`
  ahora extiende `JpaRepository`. La tabla se crea sola vía **Flyway**
  (`V1__create_pedidos_table.sql`), no hay que crearla a mano en alwaysdata.
- **Tests JUnit 5 + Mockito**: `PedidoServiceTest` con `BDDMockito` (`given/willReturn`) y
  `@ParameterizedTest` + `@CsvSource` para `crear()`, además de casos de `listar()`,
  `buscarPorId()` (encontrado / no encontrado) y el fallback cuando `ms-productos` no responde.
- `render.yaml` para desplegar con un clic vía Blueprint.
- `.dockerignore` para builds más rápidos y limpios.
