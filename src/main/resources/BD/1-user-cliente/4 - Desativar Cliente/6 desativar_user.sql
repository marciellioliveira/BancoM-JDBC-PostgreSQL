-- Função 6 - Desativar usuário
CREATE OR REPLACE FUNCTION desativar_user(p_user_id integer)
RETURNS integer AS $$
DECLARE
    v_afetados integer;
BEGIN
    UPDATE users SET user_ativo = false
    WHERE id = p_user_id;
    GET DIAGNOSTICS v_afetados = ROW_COUNT;
    RETURN v_afetados;
END;
$$ LANGUAGE plpgsql;