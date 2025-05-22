CREATE OR REPLACE FUNCTION find_role_by_name_v1(p_name text)
RETURNS TABLE (
    id integer,
    name character varying(50)
)
AS $$
BEGIN
    RETURN QUERY
    SELECT r.id, r.name
    FROM roles r
    WHERE r.name = p_name;
END;
$$ LANGUAGE plpgsql;
