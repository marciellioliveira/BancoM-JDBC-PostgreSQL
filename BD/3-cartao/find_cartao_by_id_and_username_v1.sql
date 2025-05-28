CREATE OR REPLACE FUNCTION public.find_cartao_by_id_and_username_v1(p_id BIGINT, p_username VARCHAR)
RETURNS TABLE (
    id BIGINT,
    tipo_conta VARCHAR,
    categoria_conta VARCHAR,
    tipo_cartao VARCHAR,
    numero_cartao VARCHAR,
    status BOOLEAN,
    senha VARCHAR,
    limite_credito_pre_aprovado NUMERIC,
    taxa_utilizacao NUMERIC,
    taxa_seguro_viagem NUMERIC,
    total_gasto_mes_credito NUMERIC,
    limite_diario_transacao NUMERIC,
    total_gasto_mes NUMERIC,
    conta_id BIGINT,
    cliente_id BIGINT,
    saldo_conta NUMERIC,
    numero_conta VARCHAR,
    conta_status BOOLEAN,
    cliente_nome VARCHAR,
    cliente_cpf BIGINT,
    cliente_ativo BOOLEAN,
    username VARCHAR
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        c.id,
        c.tipo_conta,
        c.categoria_conta,
        c.tipo_cartao,
        c.numero_cartao,
        c.status,
        c.senha,
        c.limite_credito_pre_aprovado,
        c.taxa_utilizacao,
        c.taxa_seguro_viagem,
        c.total_gasto_mes_credito,
        c.limite_diario_transacao,
        c.total_gasto_mes,
        co.id,
        co.cliente_id,
        co.saldo_conta,
        co.numero_conta,
        co.status,
        cl.nome,
        cl.cpf,
        cl.cliente_ativo,
        u.username
    FROM cartoes c
    JOIN contas co ON c.conta_id = co.id
    JOIN clientes cl ON co.cliente_id = cl.id
    JOIN users u ON cl.user_id = u.id
    WHERE c.id = p_id AND u.username = p_username;
END;
$$ LANGUAGE plpgsql;
