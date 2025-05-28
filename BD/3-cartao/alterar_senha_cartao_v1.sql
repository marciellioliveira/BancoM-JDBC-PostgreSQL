CREATE OR REPLACE FUNCTION alterar_senha_cartao_v1(p_id BIGINT, p_senha TEXT)
RETURNS TEXT AS $$
BEGIN
    UPDATE cartoes
    SET senha = p_senha
    WHERE id = p_id;

    RETURN 'OK';
END;
$$ LANGUAGE plpgsql;
