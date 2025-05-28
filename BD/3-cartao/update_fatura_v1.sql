CREATE OR REPLACE FUNCTION public.update_fatura_v1(
    p_id BIGINT,
    p_valor_total NUMERIC,
    p_data_vencimento TIMESTAMP,
    p_status BOOLEAN
)
RETURNS VOID AS $$
BEGIN
    UPDATE faturas 
    SET valor_total = p_valor_total,
        data_vencimento = p_data_vencimento,
        status = p_status
    WHERE id = p_id;
END;
$$ LANGUAGE plpgsql;
