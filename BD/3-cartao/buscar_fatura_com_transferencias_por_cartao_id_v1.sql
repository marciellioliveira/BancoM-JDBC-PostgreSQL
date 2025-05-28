CREATE OR REPLACE FUNCTION public.buscar_fatura_com_transferencias_por_cartao_id_v1(
		p_cartao_id bigint)
    RETURNS TABLE(
		fatura_id bigint, 
		fatura_cartao_id bigint, 
		valor_total numeric, 
		data_vencimento timestamp without time zone, 
		fatura_status boolean, 
		transferencia_id bigint, 
		transferencia_valor numeric, 
		transferencia_data timestamp without time zone, 
		transferencia_cliente_origem_id bigint,
		transferencia_cliente_destino_id bigint,
		transferencia_conta_origem_id bigint, 
		transferencia_conta_destino_id bigint,
		transferencia_cod_operacao character varying(50),
		transferencia_tipo_cartao character varying(50),
		transferencia_tipo_transferencia character varying(50)
	) 
    LANGUAGE 'plpgsql'
AS $BODY$
BEGIN
    RETURN QUERY
    SELECT 
        f.id,
        f.cartao_id,
        f.valor_total,
        f.data_vencimento,
        f.status,
        t.id,
        t.valor,
        t.data, 
		t.id_cliente_origem,
		t.id_cliente_destino,
        t.id_conta_origem,
        t.id_conta_destino,
		t.codigo_operacao,
		t.tipo_cartao,
		t.tipo_transferencia
    FROM faturas f
    LEFT JOIN fatura_transferencias ft ON f.id = ft.fatura_id
    LEFT JOIN transferencias t ON ft.transferencia_id = t.id
    WHERE f.cartao_id = p_cartao_id;
END;
$BODY$;

ALTER FUNCTION public.buscar_fatura_com_transferencias_por_cartao_id_v1(bigint)
    OWNER TO postgres;
