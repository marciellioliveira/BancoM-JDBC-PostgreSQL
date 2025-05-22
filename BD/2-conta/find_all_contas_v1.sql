CREATE OR REPLACE FUNCTION public.find_all_contas_v1()
RETURNS TABLE (
    id bigint,
    cliente_id bigint,
    cliente_nome varchar,
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
    JOIN clientes cl ON c.cliente_id = cl.id;
END;
$$;
