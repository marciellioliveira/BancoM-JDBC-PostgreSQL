-- Procedure final que chama todas as funções para desativar user/cliente
CREATE OR REPLACE PROCEDURE desativar_cliente_procedure(p_cliente_id bigint)
LANGUAGE plpgsql
AS $$
DECLARE
    v_user_id integer;
BEGIN
    v_user_id := get_user_id_by_cliente_id(p_cliente_id);

    PERFORM desativar_seguros(p_cliente_id);
    PERFORM desativar_cartoes(p_cliente_id);
    PERFORM desativar_contas(p_cliente_id);
    PERFORM desativar_cliente(p_cliente_id);
    PERFORM desativar_user(v_user_id);
END;
$$;