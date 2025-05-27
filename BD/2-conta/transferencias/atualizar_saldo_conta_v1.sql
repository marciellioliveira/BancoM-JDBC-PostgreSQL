--Definindo a função e os parametros - me permite chamar a função passando os valores que quero atualizar --
CREATE OR REPLACE FUNCTION atualizar_saldo_conta_v1(
	p_id BIGINT,
	p_saldo_conta NUMERIC,
	p_categoria_conta VARCHAR,
	p_taxa_manutencao_mensal NUMERIC DEFAULT NULL,
	p_taxa_acresc_rend NUMERIC DEFAULT NULL,
	p_taxa_mensal NUMERIC DEFAULT NULL
)
-- Essa função por ser update, escolhi não retornar valor por isso o VOID --
RETURNS BOOLEAN AS $$
DECLARE
	linhas_afetadas INT;
BEGIN
	UPDATE contas
	SET saldo_conta = p_saldo_conta,
		categoria_conta = p_categoria_conta,
		taxa_manutencao_mensal = p_taxa_manutencao_mensal,
		taxa_acresc_rend = p_taxa_acresc_rend,
		taxa_mensal = p_taxa_mensal
	WHERE id = p_id;

	GET DIAGNOSTICS linhas_afetadas = ROW_COUNT;
	
	-- Retorna true caso alguma linha  tenha sido atualizada --
	IF linhas_afetadas > 0 THEN
		RETURN TRUE;
	ELSE
		RETURN FALSE;
	END IF;
END;
-- Isso faz com que delimite o código da função e define a linguagem (PL/pgSQL) é a linguagem procedural do postgre, usado para logica do SQL --
$$ LANGUAGE plpgsql;

-- Lá em cima tem o BEGIN e o END = eles servem como o bloco principal da função mesmo. Ali que fica o código que vai ser executado ao chamar a função --