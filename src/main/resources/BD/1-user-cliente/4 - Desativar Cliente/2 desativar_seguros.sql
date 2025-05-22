-- Função 2 - Desativar seguros do cliente
CREATE OR REPLACE FUNCTION desativar_seguros(p_cliente_id bigint)
RETURNS void AS $$
BEGIN
    UPDATE seguros SET ativo = false
    WHERE cartao_id IN (
        SELECT id FROM cartoes
        WHERE conta_id IN (
            SELECT id FROM contas WHERE cliente_id = p_cliente_id
        )
    );
END;
$$ LANGUAGE plpgsql;