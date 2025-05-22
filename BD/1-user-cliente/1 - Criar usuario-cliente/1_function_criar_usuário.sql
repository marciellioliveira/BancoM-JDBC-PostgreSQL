CREATE OR REPLACE FUNCTION criar_usuario_v1(
    p_username TEXT,
    p_password TEXT,
    p_user_ativo BOOLEAN
) RETURNS INTEGER AS $$
DECLARE
    v_user_id INTEGER;
BEGIN
    INSERT INTO users (username, password, user_ativo)
    VALUES (p_username, p_password, p_user_ativo)
    RETURNING id INTO v_user_id;

    RETURN v_user_id;
END;
$$ LANGUAGE plpgsql;
