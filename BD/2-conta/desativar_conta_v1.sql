CREATE OR REPLACE FUNCTION public.desativar_conta_v1(p_id_conta bigint)
RETURNS boolean
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM contas WHERE id = p_id_conta) THEN
        RAISE EXCEPTION 'Nenhuma conta encontrada com ID: %', p_id_conta;
    END IF;

    UPDATE cartoes SET status = false WHERE conta_id = p_id_conta;
    UPDATE contas SET status = false WHERE id = p_id_conta;

    RETURN true;
END;
$$;
