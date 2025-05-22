CREATE OR REPLACE FUNCTION public.insert_conta_completa(
    p_cliente_id BIGINT,
    p_tipo_conta TEXT,
    p_categoria_conta TEXT,
    p_saldo_conta NUMERIC,
    p_numero_conta TEXT,
    p_pix_aleatorio TEXT,
    p_status BOOLEAN,
    p_taxa_manutencao_mensal NUMERIC,
    p_taxa_acresc_rend NUMERIC,
    p_taxa_mensal NUMERIC
)
RETURNS BIGINT AS
$$
DECLARE
    v_id BIGINT;
BEGIN
    INSERT INTO contas (
        cliente_id, tipo_conta, categoria_conta, saldo_conta,
        numero_conta, pix_aleatorio, status,
        taxa_manutencao_mensal, taxa_acresc_rend, taxa_mensal
    ) VALUES (
        p_cliente_id, p_tipo_conta, p_categoria_conta, p_saldo_conta,
        p_numero_conta, p_pix_aleatorio, p_status,
        p_taxa_manutencao_mensal, p_taxa_acresc_rend, p_taxa_mensal
    )
    RETURNING id INTO v_id;

    RETURN v_id;

EXCEPTION WHEN OTHERS THEN
    -- Aqui pode logar ou tratar o erro
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;
