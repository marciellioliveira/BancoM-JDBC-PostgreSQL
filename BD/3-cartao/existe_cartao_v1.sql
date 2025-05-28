CREATE OR REPLACE FUNCTION public.existe_cartao_v1(p_id BIGINT)
RETURNS BOOLEAN AS $$
DECLARE
    v_exists BOOLEAN;
BEGIN
    SELECT EXISTS(SELECT 1 FROM cartoes WHERE id = p_id) INTO v_exists;
    RETURN v_exists;
END;
$$ LANGUAGE plpgsql;
