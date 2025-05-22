CREATE OR REPLACE FUNCTION atualizar_usuario_v1(
    p_id INTEGER,
    p_username VARCHAR,
    p_user_ativo BOOLEAN
)
RETURNS VOID AS $$
BEGIN
    UPDATE users
    SET username = p_username,
        user_ativo = p_user_ativo
    WHERE id = p_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Usuário com id % não encontrado.', p_id;
    END IF;
END;
$$ LANGUAGE plpgsql;
