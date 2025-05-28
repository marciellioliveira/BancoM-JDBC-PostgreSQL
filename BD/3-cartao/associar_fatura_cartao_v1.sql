CREATE OR REPLACE FUNCTION public.associar_fatura_cartao_v1(
    p_cartao_id BIGINT,
    p_fatura_id BIGINT
)
RETURNS INTEGER AS $$
DECLARE
    rows_affected INTEGER;
BEGIN
    UPDATE cartoes
    SET fatura_id = p_fatura_id
    WHERE id = p_cartao_id;

    GET DIAGNOSTICS rows_affected = ROW_COUNT;

    RETURN rows_affected;
END;
$$ LANGUAGE plpgsql;
