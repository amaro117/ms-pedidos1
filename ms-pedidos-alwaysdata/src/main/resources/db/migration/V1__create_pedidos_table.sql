CREATE TABLE pedidos (
    id           BIGSERIAL PRIMARY KEY,
    producto_id  BIGINT NOT NULL,
    cantidad     INTEGER NOT NULL
);

INSERT INTO pedidos (producto_id, cantidad) VALUES
    (1, 2),
    (3, 1);
