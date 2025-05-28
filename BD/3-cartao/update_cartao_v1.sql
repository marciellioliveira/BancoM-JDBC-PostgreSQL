CREATE OR REPLACE FUNCTION public.update_cartao_v1(
    p_id BIGINT,
    p_limite_credito_pre_aprovado NUMERIC,
    p_total_gasto_mes_credito NUMERIC,
    p_limite_diario_transacao NUMERIC,
    p_total_gasto_mes NUMERIC
)
RETURNS INTEGER AS $$
DECLARE
    rows_affected INTEGER;
BEGIN
    UPDATE cartoes
    SET limite_credito_pre_aprovado = p_limite_credito_pre_aprovado,
        total_gasto_mes_credito = p_total_gasto_mes_credito,
        limite_diario_transacao = p_limite_diario_transacao,
        total_gasto_mes = p_total_gasto_mes
    WHERE id = p_id;

    GET DIAGNOSTICS rows_affected = ROW_COUNT;

    RETURN rows_affected;
END;
$$ LANGUAGE plpgsql;
