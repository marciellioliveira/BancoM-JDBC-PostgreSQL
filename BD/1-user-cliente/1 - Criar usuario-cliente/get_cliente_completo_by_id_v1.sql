CREATE OR REPLACE FUNCTION public.get_cliente_completo_by_id_v1(
    p_cliente_id bigint
)
RETURNS TABLE (
    cliente_id bigint,
    cliente_nome character varying,
    cliente_cpf bigint,
    cliente_ativo boolean,
    user_id integer,
    user_username character varying,
    user_password character varying,
    user_ativo boolean,
    endereco_id bigint,
    cep character varying,
    cidade character varying,
    estado character varying,
    rua character varying,
    numero character varying,
    bairro character varying,
    complemento character varying,
    conta_id bigint,
    numero_conta character varying,
    saldo numeric,
    status boolean,
    tipo character varying
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id,
        c.nome,
        c.cpf,
        c.cliente_ativo,
        u.id,
        u.username,
        u.password,
        u.user_ativo,
        e.id,
        e.cep,
        e.cidade,
        e.estado,
        e.rua,
        e.numero,
        e.bairro,
        e.complemento,
        ct.id,
        ct.numero,
        ct.saldo,
        ct.status,
        ct.tipo
    FROM clientes c
    JOIN users u ON c.user_id = u.id
    LEFT JOIN enderecos e ON e.cliente_id = c.id
    LEFT JOIN contas ct ON ct.cliente_id = c.id
    WHERE c.id = p_cliente_id;
END;
$$;

ALTER FUNCTION public.get_cliente_completo_by_id_v1(bigint)
OWNER TO postgres;
