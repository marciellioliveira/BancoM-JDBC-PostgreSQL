CREATE OR REPLACE FUNCTION atualizar_cliente_v1(
    p_id BIGINT,
    p_nome VARCHAR,
    p_cliente_ativo BOOLEAN
)
RETURNS VOID AS $$
BEGIN
    UPDATE clientes
    SET nome = p_nome,
        cliente_ativo = p_cliente_ativo
    WHERE id = p_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Cliente com id % não encontrado.', p_id;
    END IF;
END;
$$ LANGUAGE plpgsql;
