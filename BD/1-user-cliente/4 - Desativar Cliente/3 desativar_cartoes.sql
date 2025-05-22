-- Função 3 - Desativar cartões do cliente
CREATE OR REPLACE FUNCTION desativar_cartoes(p_cliente_id bigint)
RETURNS void AS $$
BEGIN
    UPDATE cartoes SET status = false
    WHERE conta_id IN (
        SELECT id FROM contas WHERE cliente_id = p_cliente_id
    );
END;
$$ LANGUAGE plpgsql;