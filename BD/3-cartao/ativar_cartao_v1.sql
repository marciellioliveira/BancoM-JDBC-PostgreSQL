CREATE OR REPLACE FUNCTION public.ativar_cartao_v1(p_id BIGINT)
RETURNS INTEGER AS $$
DECLARE
    v_rows_updated INTEGER;
BEGIN
    UPDATE cartoes
    SET status = TRUE
    WHERE id = p_id;
    
    GET DIAGNOSTICS v_rows_updated = ROW_COUNT;
    
    RETURN v_rows_updated;
END;
$$ LANGUAGE plpgsql;
