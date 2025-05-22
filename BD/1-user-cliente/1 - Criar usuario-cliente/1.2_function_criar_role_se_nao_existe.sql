CREATE OR REPLACE FUNCTION inserir_role_v1(
    p_id integer,
    p_name text
) RETURNS void AS $$
BEGIN
    -- Tenta inserir a role, se já existir, ignora
    INSERT INTO roles (id, name)
    VALUES (p_id, p_name)
    ON CONFLICT (name) DO NOTHING;
END;
$$ LANGUAGE plpgsql;
