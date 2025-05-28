CREATE OR REPLACE PROCEDURE public.associar_transferencia_fatura_v1(
  p_fatura_id bigint,
  p_transferencia_id bigint)
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO fatura_transferencias (fatura_id, transferencia_id)
  VALUES (p_fatura_id, p_transferencia_id);
END;
$$;
