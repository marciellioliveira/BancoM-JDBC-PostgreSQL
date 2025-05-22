CREATE OR REPLACE PROCEDURE ativar_cliente_procedure(p_cliente_id BIGINT)
LANGUAGE plpgsql
AS $$
BEGIN
    PERFORM ativar_cliente(p_cliente_id);
    PERFORM ativar_usuario_por_cliente_id(p_cliente_id);
END;
$$;
