CREATE OR REPLACE FUNCTION public.ativar_conta_v1(p_id_conta bigint)
RETURNS boolean
LANGUAGE plpgsql
AS $$
DECLARE
    v_rows_affected integer;
BEGIN
    -- Atualiza o status da conta
    UPDATE contas SET status = true WHERE id = p_id_conta;
    GET DIAGNOSTICS v_rows_affected = ROW_COUNT;

    IF v_rows_affected = 0 THEN
        RAISE EXCEPTION 'Nenhuma conta encontrada com ID: %', p_id_conta;
    END IF;

    RETURN true;
END;
$$;
