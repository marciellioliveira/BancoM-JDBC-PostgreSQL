CREATE OR REPLACE FUNCTION ativar_cliente(p_cliente_id BIGINT)
RETURNS VOID AS $$
BEGIN
    UPDATE clientes SET cliente_ativo = true WHERE id = p_cliente_id;
END;
$$ LANGUAGE plpgsql;
