CREATE OR REPLACE FUNCTION get_usuario_completo_v1(p_user_id integer)
RETURNS TABLE(
    user_id integer,
    username varchar(100),
    password varchar(255),
    user_ativo boolean,
    cliente_id bigint,
    nome varchar(100),
    cpf bigint,
    cliente_ativo boolean,
    endereco_id bigint,
    rua varchar(100),
    numero varchar(20),
    bairro varchar(50),
    cidade varchar(50),
    estado varchar(50),
    complemento varchar(100),
    cep varchar(20)
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
        e.id AS endereco_id,
        e.rua,
        e.numero,
        e.bairro,
        e.cidade,
        e.estado,
        e.complemento,
        e.cep
    FROM users u
    JOIN clientes c ON c.user_id = u.id
    LEFT JOIN enderecos e ON e.cliente_id = c.id
    WHERE u.id = p_user_id;
END;
$$ LANGUAGE plpgsql;
