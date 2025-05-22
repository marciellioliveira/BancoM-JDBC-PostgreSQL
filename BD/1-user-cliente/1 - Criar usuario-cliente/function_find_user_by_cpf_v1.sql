CREATE OR REPLACE FUNCTION find_user_by_cpf_v1(p_cpf bigint)
RETURNS TABLE (
    user_id integer,
    username character varying(100), -- alterar para varchar(100)
    password character varying(100), -- faça o mesmo para outras colunas varchar
    user_ativo boolean,
    cliente_id bigint,
    nome character varying(100),
    cpf bigint,
    cliente_ativo boolean,
    endereco_id bigint,
    rua character varying(100),
    numero character varying(20),
    bairro character varying(50),
    cidade character varying(50),
    estado character varying(50),
    complemento character varying(100),
    cep character varying(20),
    role_name character varying(100)
) AS $$
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
    JOIN clientes c ON c.user_id = u.id
    LEFT JOIN enderecos e ON e.cliente_id = c.id
    LEFT JOIN user_roles ur ON ur.user_id = u.id
    LEFT JOIN roles r ON r.id = ur.role_id
    WHERE c.cpf = p_cpf;
END;
$$ LANGUAGE plpgsql;
