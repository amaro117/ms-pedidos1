# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.14/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.14/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.14/reference/web/servlet.html)
* [Spring Reactive Web (WebClient)](https://docs.spring.io/spring-boot/3.5.14/reference/web/reactive.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Building a Reactive RESTful Web Service](https://spring.io/guides/gs/reactive-rest-service/)

### Arquitectura por capas

El codigo esta organizado en capas (Controller -> Service -> Repository), con
los modelos de dominio y los DTO separados:

```
com.duoc.ms_pedidos
├── MsPedidosApplication      arranque de Spring Boot
├── config
│   └── WebClientConfig       registra el bean WebClient (ms.productos.url)
├── controller
│   └── PedidoController      expone los endpoints REST, delega en el service
├── service
│   └── PedidoService         logica del pedido + enriquecimiento via WebClient
├── repository
│   └── PedidoRepository      almacen en memoria (findAll, findById, save)
├── model
│   └── Pedido                entidad de dominio (id, productoId, cantidad)
└── dto
    ├── PedidoRequest         cuerpo del POST (productoId, cantidad)
    ├── PedidoResponse        respuesta enriquecida (pedido + producto)
    └── Producto              producto recibido de ms-productos
```

Cada capa solo conoce a la de abajo: el `PedidoController` llama al
`PedidoService`, y este al `PedidoRepository`. La inyeccion es por constructor.

### Comunicacion entre microservicios (WebClient)

Este servicio (`ms-pedidos`) no guarda los datos del producto: el `model/Pedido`
solo almacena el `productoId`. Para devolver el pedido junto con el nombre y
precio del producto, el `PedidoService` llama al microservicio `ms-productos`
usando **WebClient**.

Piezas clave:

* `config/WebClientConfig` registra un bean `WebClient` apuntando a
  `ms.productos.url`.
* `dto/Producto` es el DTO con el que se deserializa la respuesta de
  `ms-productos`.
* `dto/PedidoResponse` es el modelo enriquecido que se devuelve al cliente: los
  datos del pedido (`id`, `productoId`, `cantidad`) mas el `producto` completo.
* `PedidoService.enriquecer(...)` hace `GET /api/productos/{id}` y, si el
  producto no existe o el servicio no responde, devuelve uno con nombre
  `"No disponible"`.

La URL del otro microservicio se configura en `application.properties`:

```properties
ms.productos.url=${MS_PRODUCTOS_URL:http://localhost:8081}
```

* En local apunta a `http://localhost:8081`.
* En Docker, `docker-compose.yml` sobreescribe `MS_PRODUCTOS_URL` con
  `http://ms-productos:8081` (DNS interno de la red de Docker).

### Endpoints

* `GET /api/pedidos` — lista los pedidos, cada uno enriquecido con su producto.
* `GET /api/pedidos/{id}` — un pedido por id (404 si no existe).
* `POST /api/pedidos` — crea un pedido; el cuerpo es `{"productoId":2,"cantidad":5}`.

Ejemplo de respuesta enriquecida:

```json
{
  "id": 1,
  "productoId": 1,
  "cantidad": 2,
  "producto": { "id": 1, "nombre": "Teclado mecanico", "precio": 25990 }
}
```

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
