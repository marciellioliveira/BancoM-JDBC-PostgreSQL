-- Cria uma função que passa o batch_size como parâmetro e não retorna nada --
CREATE OR REPLACE FUNCTION aplicar_rendimento_em_lotes_v1(batch_size INT)
RETURNS VOID AS $$

-- Cria conta_id, que vai armazenar cada id selecionado no loop --
DECLARE
    conta_id BIGINT;
BEGIN
-- Seleciona a conta --
    FOR conta_id IN
        SELECT id FROM contas
        WHERE tipo_conta = 'POUPANCA'
        AND status = TRUE
        AND taxa_acresc_rend IS NOT NULL
        LIMIT batch_size
		-- Pega o id das contas válidas até o limite que especifiquei --
    LOOP
		-- Atualiza as contas --
        UPDATE contas
        SET saldo_conta = saldo_conta + (saldo_conta * taxa_acresc_rend),
            categoria_conta = (
                SELECT categoria FROM (
                    VALUES
                    (10000, 'PREMIUM'),
                    (5000, 'SUPER'),
                    (0, 'COMUM')
                ) AS limites(valor, categoria)
                WHERE (saldo_conta + (saldo_conta * taxa_acresc_rend)) >= limites.valor
                ORDER BY limites.valor DESC
                LIMIT 1
            )
        WHERE id = conta_id;
    END LOOP;
END;
$$ LANGUAGE plpgsql;
