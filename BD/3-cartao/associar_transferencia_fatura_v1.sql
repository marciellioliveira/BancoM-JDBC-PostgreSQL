CREATE OR REPLACE FUNCTION public.associar_transferencia_fatura_v1(
    p_fatura_id BIGINT,
    p_transferencia_id BIGINT
)
RETURNS VOID AS $$
BEGIN
    INSERT INTO fatura_transferencias (fatura_id, transferencia_id)
    VALUES (p_fatura_id, p_transferencia_id);
END;
$$ LANGUAGE plpgsql;
