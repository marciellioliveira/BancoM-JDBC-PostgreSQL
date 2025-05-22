-- Função 1 - Obter user_id a partir de cliente_id
CREATE OR REPLACE FUNCTION get_user_id_by_cliente_id(p_cliente_id bigint)
RETURNS integer AS $$
DECLARE
    v_user_id integer;
BEGIN
    SELECT user_id INTO v_user_id
    FROM clientes
    WHERE id = p_cliente_id;

    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Cliente não encontrado com ID: %', p_cliente_id;
    END IF;

    RETURN v_user_id;
END;
$$ LANGUAGE plpgsql;