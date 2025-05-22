CREATE OR REPLACE FUNCTION ativar_usuario_por_cliente_id(p_cliente_id BIGINT)
RETURNS VOID AS $$
DECLARE
    v_user_id INTEGER;
BEGIN
    SELECT user_id INTO v_user_id FROM clientes WHERE id = p_cliente_id;
    
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Usuário não encontrado para cliente_id %', p_cliente_id;
    END IF;

    UPDATE users SET user_ativo = true WHERE id = v_user_id;
END;
$$ LANGUAGE plpgsql;
