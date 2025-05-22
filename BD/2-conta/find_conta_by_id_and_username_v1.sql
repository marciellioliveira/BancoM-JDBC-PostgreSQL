CREATE OR REPLACE FUNCTION public.find_conta_by_id_and_username_v1(p_id bigint, p_username varchar)
RETURNS TABLE (
    id bigint,
    cliente_id bigint,
    cliente_nome varchar,
    user_id integer,
    username varchar,
    tipo_conta varchar,
    categoria_conta varchar,
    saldo_conta numeric,
    numero_conta varchar,
    pix_aleatorio varchar,
    status boolean,
    taxa_manutencao_mensal numeric,
    taxa_acresc_rend numeric,
    taxa_mensal numeric
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        c.id,
        cl.id AS cliente_id,
        cl.nome AS cliente_nome,
        u.id AS user_id,
        u.username,
        c.tipo_conta,
        c.categoria_conta,
        c.saldo_conta,
        c.numero_conta,
        c.pix_aleatorio,
        c.status,
        c.taxa_manutencao_mensal,
        c.taxa_acresc_rend,
        c.taxa_mensal
    FROM contas c
    JOIN clientes cl ON c.cliente_id = cl.id
    JOIN users u ON cl.user_id = u.id
    WHERE c.id = p_id AND u.username = p_username;
END;
$$;
