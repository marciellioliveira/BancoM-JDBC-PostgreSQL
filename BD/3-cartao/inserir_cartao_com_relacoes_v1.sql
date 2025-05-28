-- FUNCTION: public.inserir_cartao_com_relacoes_v1(character varying, character varying, character varying, character varying, boolean, character varying, bigint, numeric, numeric)

-- DROP FUNCTION IF EXISTS public.inserir_cartao_com_relacoes_v1(character varying, character varying, character varying, character varying, boolean, character varying, bigint, numeric, numeric);

CREATE OR REPLACE FUNCTION public.inserir_cartao_com_relacoes_v1(
	p_tipo_conta character varying,
	p_categoria_conta character varying,
	p_tipo_cartao character varying,
	p_numero_cartao character varying,
	p_status boolean,
	p_senha character varying,
	p_conta_id bigint,
	p_limite_credito_pre_aprovado numeric,
	p_limite_diario_transacao numeric)
    RETURNS TABLE(id_cartao bigint) 
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$

DECLARE
		v_id_cartao BIGINT;
BEGIN
		INSERT INTO cartoes(
			tipo_conta, 
			categoria_conta, 
			tipo_cartao, 
			numero_cartao, 
			status, 
			senha, 
			conta_id,
			limite_credito_pre_aprovado, 
			limite_diario_transacao)
		VALUES (
			p_tipo_conta, 
			p_categoria_conta, 
			p_tipo_cartao, 
			p_numero_cartao, 
			p_status, 
			p_senha, 
			p_conta_id, 
			p_limite_credito_pre_aprovado, 
			p_limite_diario_transacao)
		RETURNING id INTO v_id_cartao;

		RETURN QUERY SELECT v_id_cartao;
END;
$BODY$;

ALTER FUNCTION public.inserir_cartao_com_relacoes_v1(character varying, character varying, character varying, character varying, boolean, character varying, bigint, numeric, numeric)
    OWNER TO postgres;
