CREATE OR REPLACE FUNCTION public.insert_fatura_v1(
    p_cartao_id BIGINT,
    p_data_vencimento TIMESTAMP,
    p_valor_total NUMERIC,
    p_status BOOLEAN
)
RETURNS BIGINT AS $$
DECLARE
    new_id BIGINT;
BEGIN
    INSERT INTO faturas (cartao_id, data_vencimento, valor_total, status)
    VALUES (p_cartao_id, p_data_vencimento, p_valor_total, p_status)
    RETURNING id INTO new_id;

    RETURN new_id;
END;
$$ LANGUAGE plpgsql;
