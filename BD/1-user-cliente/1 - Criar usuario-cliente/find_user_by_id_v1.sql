CREATE OR REPLACE FUNCTION find_user_by_id_v1(p_id integer)
RETURNS TABLE (
    user_id integer,
    username text,
    password text,
    user_ativo boolean,
    cliente_id bigint,
    nome text,
    cpf bigint,
    cliente_ativo boolean,
    endereco_id bigint,
    rua text,
    numero text,
    bairro text,
    cidade text,
    estado text,
    complemento text,
    cep text,
    role_name text
)
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.id,
        u.username::text,
        u.password::text,
        u.user_ativo,
        c.id,
        c.nome::text,
        c.cpf,
        c.cliente_ativo,
        e.id,
        e.rua::text,
        e.numero::text,
        e.bairro::text,
        e.cidade::text,
        e.estado::text,
        e.complemento::text,
        e.cep::text,
        r.name::text
    FROM users u
    JOIN clientes c ON c.user_id = u.id
    LEFT JOIN enderecos e ON e.cliente_id = c.id
    LEFT JOIN user_roles ur ON ur.user_id = u.id
    LEFT JOIN roles r ON r.id = ur.role_id
    WHERE u.id = p_id;
END;
$$ LANGUAGE plpgsql;
