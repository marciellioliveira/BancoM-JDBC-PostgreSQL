CREATE OR REPLACE FUNCTION atualizar_conta_corrente_v1(
    p_id BIGINT,
    p_saldo_conta NUMERIC,
    p_categoria_conta VARCHAR,
    p_taxa_manutencao_mensal NUMERIC
)
RETURNS BOOLEAN AS $$
DECLARE
    linhas_afetadas INT;
BEGIN
    UPDATE contas
    SET saldo_conta = p_saldo_conta,
        categoria_conta = p_categoria_conta,
        taxa_manutencao_mensal = p_taxa_manutencao_mensal
    WHERE id = p_id AND tipo_conta = 'CORRENTE';

    GET DIAGNOSTICS linhas_afetadas = ROW_COUNT;

    IF linhas_afetadas > 0 THEN
        RETURN TRUE;
    ELSE
        RETURN FALSE;
    END IF;
END;
$$ LANGUAGE plpgsql;
