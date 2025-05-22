CREATE OR REPLACE FUNCTION criar_cliente_v1(
    p_nome TEXT,
    p_cpf BIGINT,
    p_cliente_ativo BOOLEAN,
    p_user_id INTEGER
) RETURNS INTEGER AS $$
DECLARE
    v_cliente_id INTEGER;
BEGIN
    INSERT INTO clientes (nome, cpf, cliente_ativo, user_id)
    VALUES (p_nome, p_cpf, p_cliente_ativo, p_user_id)
    RETURNING id INTO v_cliente_id;

    RETURN v_cliente_id;
END;
$$ LANGUAGE plpgsql;
