CREATE OR REPLACE FUNCTION public.alterar_limite_cartao_debito_v1(
    p_id BIGINT,
    p_limite_diario_transacao NUMERIC
)
RETURNS INTEGER AS $$
DECLARE
    rows_affected INTEGER;
BEGIN
    UPDATE cartoes
    SET limite_diario_transacao = p_limite_diario_transacao
    WHERE id = p_id;

    GET DIAGNOSTICS rows_affected = ROW_COUNT;

    RETURN rows_affected;
END;
$$ LANGUAGE plpgsql;
