CREATE OR REPLACE PROCEDURE public.update_fatura_v1(
    p_id bigint,
    p_valor_total numeric,
    p_data_vencimento timestamp without time zone,
    p_status boolean)
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE faturas 
    SET valor_total = p_valor_total,
        data_vencimento = p_data_vencimento,
        status = p_status
    WHERE id = p_id;
END;
$$;
