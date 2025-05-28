CREATE OR REPLACE FUNCTION public.update_conta_v1(
    p_id BIGINT,
    p_saldo_conta NUMERIC,
    p_categoria_conta TEXT,
    p_taxa_manutencao_mensal NUMERIC,
    p_taxa_acresc_rend NUMERIC,
    p_taxa_mensal NUMERIC
)
RETURNS INTEGER AS $$
DECLARE
    rows_affected INTEGER;
BEGIN
    UPDATE contas SET
        saldo_conta = p_saldo_conta,
        categoria_conta = p_categoria_conta,
        taxa_manutencao_mensal = p_taxa_manutencao_mensal,
        taxa_acresc_rend = p_taxa_acresc_rend,
        taxa_mensal = p_taxa_mensal
    WHERE id = p_id;

    GET DIAGNOSTICS rows_affected = ROW_COUNT;

    RETURN rows_affected;
END;
$$ LANGUAGE plpgsql;
