CREATE OR REPLACE FUNCTION insert_role_if_not_exists_v1(p_id INTEGER, p_name TEXT)
RETURNS VOID AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM roles WHERE name = p_name) THEN
        INSERT INTO roles (id, name) VALUES (p_id, p_name);
    END IF;
END;
$$ LANGUAGE plpgsql;
