CREATE OR REPLACE FUNCTION public.find_cliente_by_id_v1(
    p_cliente_id bigint
)
RETURNS TABLE(
    cliente_id bigint,
    nome varchar,
    cpf bigint,
    cliente_ativo boolean,
    user_id integer,
    username varchar,
    password varchar,
    user_ativo boolean,
    endereco_id bigint,
    cep varchar,
    cidade varchar,
    estado varchar,
    rua varchar,
    numero varchar,
    bairro varchar,
    complemento varchar
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        c.id AS cliente_id,
        c.nome,
        c.cpf,
        c.cliente_ativo,
        u.id AS user_id,
        u.username,
        u.password,
        u.user_ativo,
        e.id AS endereco_id,
        e.cep,
        e.cidade,
        e.estado,
        e.rua,
        e.numero,
        e.bairro,
        e.complemento
    FROM clientes c
    JOIN users u ON c.user_id = u.id
    LEFT JOIN enderecos e ON e.cliente_id = c.id
    WHERE c.id = p_cliente_id;
END;
$$;
