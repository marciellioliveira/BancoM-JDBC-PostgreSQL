CREATE OR REPLACE FUNCTION public.alterar_limite_cartao_credito_v1(
    p_id BIGINT,
    p_limite_credito_pre_aprovado NUMERIC
)
RETURNS INTEGER AS $$
DECLARE
    rows_affected INTEGER;
BEGIN
    UPDATE cartoes
    SET limite_credito_pre_aprovado = p_limite_credito_pre_aprovado
    WHERE id = p_id;

    GET DIAGNOSTICS rows_affected = ROW_COUNT;

    RETURN rows_affected;
END;
$$ LANGUAGE plpgsql;
