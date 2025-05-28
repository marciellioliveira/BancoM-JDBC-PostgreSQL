CREATE OR REPLACE FUNCTION public.find_fatura_by_cartao_id_v1(p_cartao_id BIGINT)
RETURNS TABLE (
    id BIGINT,
    cartao_id BIGINT,
    valor_total NUMERIC,
    data_vencimento TIMESTAMP,
    status BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        f.id,
        f.cartao_id,
        f.valor_total,
        f.data_vencimento,
        f.status
    FROM faturas f
    WHERE f.cartao_id = p_cartao_id
    LIMIT 1;
END;
$$ LANGUAGE plpgsql;
