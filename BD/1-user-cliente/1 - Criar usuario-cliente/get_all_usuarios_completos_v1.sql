CREATE OR REPLACE FUNCTION public.get_all_usuarios_completos_v1()
RETURNS TABLE (
    user_id integer,
    username character varying,
    password character varying,
    user_ativo boolean,
    cliente_id bigint,
    nome character varying,
    cpf bigint,
    cliente_ativo boolean,
    endereco_id bigint,
    rua character varying,
    numero character varying,
    bairro character varying,
    cidade character varying,
    estado character varying,
    complemento character varying,
    cep character varying,
    role_name character varying
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        u.id,
        u.username,
        u.password,
        u.user_ativo,
        c.id,
        c.nome,
        c.cpf,
        c.cliente_ativo,
        e.id,
        e.rua,
        e.numero,
        e.bairro,
        e.cidade,
        e.estado,
        e.complemento,
        e.cep,
        r.name
    FROM users u
    LEFT JOIN user_roles ur ON ur.user_id = u.id
    LEFT JOIN roles r ON r.id = ur.role_id
    LEFT JOIN clientes c ON c.user_id = u.id
    LEFT JOIN enderecos e ON e.cliente_id = c.id;
END;
$$;

ALTER FUNCTION public.get_all_usuarios_completos_v1()
OWNER TO postgres;
