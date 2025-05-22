-- Função 4 - Desativar contas do cliente
CREATE OR REPLACE FUNCTION desativar_contas(p_cliente_id bigint)
RETURNS void AS $$
BEGIN
    UPDATE contas SET status = false
    WHERE cliente_id = p_cliente_id;
END;
$$ LANGUAGE plpgsql;