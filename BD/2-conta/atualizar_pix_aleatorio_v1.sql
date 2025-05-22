CREATE OR REPLACE FUNCTION public.atualizar_pix_aleatorio_v1(
    p_id_conta BIGINT,
    p_novo_pix TEXT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
    -- Verifica se a conta existe
    IF NOT EXISTS (SELECT 1 FROM contas WHERE id = p_id_conta) THEN
        RAISE EXCEPTION 'Conta com ID % não encontrada.', p_id_conta;
    END IF;

    -- Atualiza o valor do pix_aleatorio
    UPDATE contas SET pix_aleatorio = p_novo_pix WHERE id = p_id_conta;

    RETURN true;
END;
$$;
