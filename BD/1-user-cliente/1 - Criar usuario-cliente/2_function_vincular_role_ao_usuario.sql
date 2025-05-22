CREATE OR REPLACE FUNCTION vincular_role_v1(p_user_id INTEGER, p_role_name TEXT)
RETURNS VOID AS $$
DECLARE
    v_role_id INTEGER;
BEGIN
    SELECT id INTO v_role_id FROM roles WHERE name = p_role_name;
    IF v_role_id IS NULL THEN
        RAISE EXCEPTION 'Role não encontrada: %', p_role_name;
    END IF;

    INSERT INTO user_roles (user_id, role_id) VALUES (p_user_id, v_role_id);
END;
$$ LANGUAGE plpgsql;
