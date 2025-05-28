CREATE OR REPLACE FUNCTION aplicar_taxa_manutencao_em_lotes_v1(batch_size INT)
RETURNS VOID AS $$
DECLARE
    conta_id BIGINT;
BEGIN
    FOR conta_id IN
        SELECT id FROM contas
        WHERE tipo_conta = 'CORRENTE'
        AND status = TRUE
        AND taxa_manutencao_mensal IS NOT NULL
        LIMIT batch_size
    LOOP
        UPDATE contas
        SET saldo_conta = saldo_conta - taxa_manutencao_mensal,
            categoria_conta = CASE
                WHEN (saldo_conta - taxa_manutencao_mensal) >= 10000 THEN 'PREMIUM'
                WHEN (saldo_conta - taxa_manutencao_mensal) >= 5000 THEN 'SUPER'
                ELSE 'COMUM'
            END
        WHERE id = conta_id;
    END LOOP;
END;
$$ LANGUAGE plpgsql;
