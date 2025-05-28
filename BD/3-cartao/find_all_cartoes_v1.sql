CREATE OR REPLACE FUNCTION public.find_all_cartoes_v1()
RETURNS TABLE (
    id BIGINT,
    cartao_tipo_conta VARCHAR,
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
    conta_tipo_conta VARCHAR,
    conta_categoria_conta VARCHAR,
    saldo_conta NUMERIC,
    conta_status BOOLEAN,
    cliente_id BIGINT,
    cliente_nome VARCHAR,
    cliente_cpf BIGINT,
    cliente_ativo BOOLEAN,
    fatura_id BIGINT,
    fatura_valor_total NUMERIC,
    fatura_data_vencimento TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT c.id, c.tipo_conta AS cartao_tipo_conta, c.categoria_conta, c.tipo_cartao, c.numero_cartao, c.status, c.senha,
           c.limite_credito_pre_aprovado, c.taxa_utilizacao, c.taxa_seguro_viagem, c.total_gasto_mes_credito,
           c.limite_diario_transacao, c.total_gasto_mes, ct.id AS conta_id, ct.tipo_conta AS conta_tipo_conta,
           ct.categoria_conta AS conta_categoria_conta, ct.saldo_conta, ct.status AS conta_status,
           cli.id AS cliente_id, cli.nome AS cliente_nome, cli.cpf AS cliente_cpf, cli.cliente_ativo,
           f.id AS fatura_id, f.valor_total AS fatura_valor_total, f.data_vencimento AS fatura_data_vencimento
    FROM cartoes c
    JOIN contas ct ON c.conta_id = ct.id
    JOIN clientes cli ON ct.cliente_id = cli.id
    LEFT JOIN faturas f ON c.fatura_id = f.id;
END;
$$ LANGUAGE plpgsql;
