package com.duoc.ms_pedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.duoc.ms_pedidos.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
