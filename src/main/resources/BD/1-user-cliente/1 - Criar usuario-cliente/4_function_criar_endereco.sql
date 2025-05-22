CREATE OR REPLACE FUNCTION criar_endereco_v1(
    p_cep TEXT,
    p_rua TEXT,
    p_numero TEXT,
    p_bairro TEXT,
    p_cidade TEXT,
    p_estado TEXT,
    p_complemento TEXT,    
    p_cliente_id INTEGER
) RETURNS INTEGER AS $$
DECLARE
    v_endereco_id INTEGER;
BEGIN
    INSERT INTO enderecos (cep, rua, numero, bairro, cidade, estado, complemento, cliente_id)
    VALUES (p_cep, p_rua, p_numero, p_bairro, p_cidade, p_estado, p_complemento, p_cliente_id)
    RETURNING id INTO v_endereco_id;

    RETURN v_endereco_id;
END;
$$ LANGUAGE plpgsql;
