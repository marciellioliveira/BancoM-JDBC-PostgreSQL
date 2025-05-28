CREATE OR REPLACE FUNCTION public.find_cartoes_by_username_v1(p_username VARCHAR)
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
    saldo_conta NUMERIC,
    numero_conta VARCHAR,
    conta_status BOOLEAN,
    cliente_id BIGINT,
    cliente_nome VARCHAR,
    cliente_cpf BIGINT,
    cliente_ativo BOOLEAN
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
        ct.id,
        ct.saldo_conta,
        ct.numero_conta,
        ct.status,
        cl.id,
        cl.nome,
        cl.cpf,
        cl.cliente_ativo
    FROM cartoes c
    INNER JOIN contas ct ON c.conta_id = ct.id
    INNER JOIN clientes cl ON ct.cliente_id = cl.id
    INNER JOIN users u ON cl.user_id = u.id
    WHERE u.username = p_username;
END;
$$ LANGUAGE plpgsql;
