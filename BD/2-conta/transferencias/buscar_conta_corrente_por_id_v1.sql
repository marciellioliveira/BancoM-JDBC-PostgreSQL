CREATE OR REPLACE FUNCTION buscar_conta_corrente_por_id_v1(p_id BIGINT)
RETURNS TABLE (
    id BIGINT,
    cliente_id BIGINT,
    cliente_nome VARCHAR,
    saldo_conta NUMERIC,
    tipo_conta VARCHAR,
    categoria_conta VARCHAR,
	numero_conta VARCHAR,
    taxa_manutencao_mensal NUMERIC,
    taxa_acresc_rend NUMERIC,
    taxa_mensal NUMERIC,
    pix_aleatorio VARCHAR,
	status BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        c.id,
        cl.id AS cliente_id,
        cl.nome AS cliente_nome,
        c.saldo_conta,
        c.tipo_conta,
        c.categoria_conta,
		c.numero_conta,
        c.taxa_manutencao_mensal,
        c.taxa_acresc_rend,
        c.taxa_mensal,
        c.pix_aleatorio,
		c.status
    FROM contas c
    JOIN clientes cl ON c.cliente_id = cl.id
    WHERE c.id = p_id AND c.tipo_conta = 'CORRENTE';
END;
$$ LANGUAGE plpgsql;
