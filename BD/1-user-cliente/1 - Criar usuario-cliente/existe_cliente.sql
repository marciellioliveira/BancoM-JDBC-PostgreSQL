-- DROP FUNCTION IF EXISTS public.existe_cliente(bigint);

CREATE OR REPLACE FUNCTION public.existe_cliente(p_cliente_id bigint)
RETURNS boolean AS $$
DECLARE
    v_exists boolean;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM clientes
        WHERE id = p_cliente_id
    ) INTO v_exists;

    RETURN v_exists;
END;
$$ LANGUAGE plpgsql;
