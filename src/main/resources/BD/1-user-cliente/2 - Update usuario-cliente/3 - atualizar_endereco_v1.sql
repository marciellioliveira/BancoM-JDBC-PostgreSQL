CREATE OR REPLACE FUNCTION atualizar_endereco_v1(
    p_cliente_id BIGINT,
    p_rua VARCHAR,
    p_numero VARCHAR,
    p_bairro VARCHAR,
    p_cidade VARCHAR,
    p_estado VARCHAR,
    p_complemento VARCHAR,
    p_cep VARCHAR
)
RETURNS VOID AS $$
BEGIN
    UPDATE enderecos
    SET rua = p_rua,
        numero = p_numero,
        bairro = p_bairro,
        cidade = p_cidade,
        estado = p_estado,
        complemento = p_complemento,
        cep = p_cep
    WHERE cliente_id = p_cliente_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Endereço com cliente_id % não encontrado.', p_cliente_id;
    END IF;
END;
$$ LANGUAGE plpgsql;
