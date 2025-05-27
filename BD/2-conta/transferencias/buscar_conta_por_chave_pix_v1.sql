CREATE OR REPLACE FUNCTION buscar_conta_por_chave_pix_v1(p_chave VARCHAR)
RETURNS TABLE (
    id BIGINT,
    cliente_id BIGINT,
    cliente_nome VARCHAR,
    user_id INTEGER,
    username VARCHAR,
    saldo_conta NUMERIC,
	tipo_conta VARCHAR,
	numero_conta VARCHAR,
    categoria_conta VARCHAR,
    taxa_manutencao_mensal NUMERIC,
    taxa_acresc_rend NUMERIC,
    taxa_mensal NUMERIC,
    pix_aleatorio VARCHAR,
	status BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT c.id, cl.id, cl.nome, u.id, u.username,
           c.saldo_conta, c.tipo_conta, c.numero_conta, c.categoria_conta, c.taxa_manutencao_mensal,
           c.taxa_acresc_rend, c.taxa_mensal, c.pix_aleatorio, c.status
    FROM contas c
    JOIN clientes cl ON c.cliente_id = cl.id
    JOIN users u ON cl.user_id = u.id
    WHERE c.pix_aleatorio = p_chave;
END;
$$ LANGUAGE plpgsql;
