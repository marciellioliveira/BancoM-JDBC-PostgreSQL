CREATE OR REPLACE FUNCTION public.salvar_transferencia_v1(
    p_id_cliente_origem BIGINT,
    p_id_cliente_destino BIGINT,
    p_id_conta_origem BIGINT,
    p_id_conta_destino BIGINT,
    p_tipo_transferencia TEXT,
    p_valor NUMERIC,
    p_data TIMESTAMP,
    p_codigo_operacao TEXT,
    p_tipo_cartao TEXT,
    p_id_cartao BIGINT,
    p_fatura_id BIGINT
)
RETURNS BIGINT
LANGUAGE plpgsql
AS $$
DECLARE
    v_id BIGINT;
BEGIN
    INSERT INTO transferencias (
        id_cliente_origem,
        id_cliente_destino,
        id_conta_origem,
        id_conta_destino,
        tipo_transferencia,
        valor,
        data,
        codigo_operacao,
        tipo_cartao,
        id_cartao,
        fatura_id
    )
    VALUES (
        p_id_cliente_origem,
        p_id_cliente_destino,
        p_id_conta_origem,
        p_id_conta_destino,
        p_tipo_transferencia,
        p_valor,
        p_data,
        p_codigo_operacao,
        p_tipo_cartao,
        p_id_cartao,
        p_fatura_id
    )
    RETURNING id INTO v_id;

    RETURN v_id;
END;
$$;
