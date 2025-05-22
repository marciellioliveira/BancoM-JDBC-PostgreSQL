-- Função 5 - Desativar cliente
CREATE OR REPLACE FUNCTION desativar_cliente(p_cliente_id bigint)
RETURNS integer AS $$
DECLARE
    v_afetados integer;
BEGIN
    UPDATE clientes SET cliente_ativo = false
    WHERE id = p_cliente_id;
    GET DIAGNOSTICS v_afetados = ROW_COUNT;
    RETURN v_afetados;
END;
$$ LANGUAGE plpgsql;