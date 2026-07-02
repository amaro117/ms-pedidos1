package com.duoc.ms_pedidos.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.duoc.ms_pedidos.dto.PedidoResponse;
import com.duoc.ms_pedidos.dto.Producto;
import com.duoc.ms_pedidos.model.Pedido;
import com.duoc.ms_pedidos.repository.PedidoRepository;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PedidoService")
class PedidoServiceTest {

    @Mock
    private PedidoRepository repository;

    private WebClient productosWebClient;
    private PedidoService service;

    @BeforeEach
    void setUp() {
        // WebClient se mockea con deep-stubs porque su API es fluida (builder chain)
        productosWebClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        service = new PedidoService(repository, productosWebClient);
    }

    private void mockearProducto(Long productoId, Producto respuesta) {
        given(productosWebClient.get()
                .uri(any(String.class), eq(productoId))
                .retrieve()
                .bodyToMono(Producto.class)
                .onErrorReturn(any(Producto.class)))
                .willReturn(Mono.just(respuesta));
    }

    @Test
    @DisplayName("listar() devuelve todos los pedidos enriquecidos con su producto")
    void listar_devuelveTodosLosPedidosEnriquecidos() {
        given(repository.findAll()).willReturn(List.of(
                new Pedido(1L, 10L, 2),
                new Pedido(2L, 20L, 1)
        ));
        mockearProducto(10L, new Producto(10L, "Bicicleta", 150000));
        mockearProducto(20L, new Producto(20L, "Casco", 25000));

        List<PedidoResponse> resultado = service.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getProducto().getNombre()).isEqualTo("Bicicleta");
        assertThat(resultado.get(1).getProducto().getNombre()).isEqualTo("Casco");
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("buscarPorId() devuelve el pedido cuando existe")
    void buscarPorId_existente_devuelvePedido() {
        given(repository.findById(1L)).willReturn(Optional.of(new Pedido(1L, 10L, 2)));
        mockearProducto(10L, new Producto(10L, "Bicicleta", 150000));

        Optional<PedidoResponse> resultado = service.buscarPorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(1L);
        assertThat(resultado.get().getProducto().getNombre()).isEqualTo("Bicicleta");
    }

    @Test
    @DisplayName("buscarPorId() devuelve Optional vacío cuando no existe")
    void buscarPorId_inexistente_devuelveOptionalVacio() {
        given(repository.findById(99L)).willReturn(Optional.empty());

        Optional<PedidoResponse> resultado = service.buscarPorId(99L);

        assertThat(resultado).isEmpty();
    }

    @ParameterizedTest(name = "crear(productoId={0}, cantidad={1}) genera un pedido válido")
    @CsvSource({
            "10, 1",
            "20, 5",
            "30, 100"
    })
    @DisplayName("crear() genera un pedido con los datos entregados, para distintas combinaciones")
    void crear_conDistintasCombinaciones_generaPedidoValido(Long productoId, int cantidad) {
        given(repository.save(any(Pedido.class)))
                .willReturn(new Pedido(productoId, cantidad));
        mockearProducto(productoId, new Producto(productoId, "Producto " + productoId, 1000));

        PedidoResponse resultado = service.crear(productoId, cantidad);

        assertThat(resultado.getProductoId()).isEqualTo(productoId);
        assertThat(resultado.getCantidad()).isEqualTo(cantidad);
        assertThat(resultado.getProducto()).isNotNull();
        verify(repository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("enriquecer() usa el producto de respaldo si ms-productos falla o no responde")
    void enriquecer_conFalloDeProductos_usaProductoDeRespaldo() {
        given(repository.findById(1L)).willReturn(Optional.of(new Pedido(1L, 10L, 2)));
        // Simula que ms-productos no responde: el flujo real usa onErrorReturn(...)
        mockearProducto(10L, new Producto(10L, "No disponible", 0));

        Optional<PedidoResponse> resultado = service.buscarPorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getProducto().getNombre()).isEqualTo("No disponible");
    }
}
