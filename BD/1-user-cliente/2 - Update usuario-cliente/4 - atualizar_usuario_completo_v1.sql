CREATE OR REPLACE PROCEDURE atualizar_usuario_completo_v1(
    p_user_id INTEGER,
    p_username VARCHAR,
    p_user_ativo BOOLEAN,

    p_cliente_id BIGINT,
    p_nome VARCHAR,
    p_cliente_ativo BOOLEAN,

    p_rua VARCHAR,
    p_numero VARCHAR,
    p_bairro VARCHAR,
    p_cidade VARCHAR,
    p_estado VARCHAR,
    p_complemento VARCHAR,
    p_cep VARCHAR,

    OUT ref REFCURSOR
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- Bloco transacional para garantir atomicidade
    BEGIN
        PERFORM atualizar_usuario_v1(p_user_id, p_username, p_user_ativo);
        PERFORM atualizar_cliente_v1(p_cliente_id, p_nome, p_cliente_ativo);
        PERFORM atualizar_endereco_v1(
            p_cliente_id,
            p_rua,
            p_numero,
            p_bairro,
            p_cidade,
            p_estado,
            p_complemento,
            p_cep
        );
    EXCEPTION
        WHEN OTHERS THEN
            RAISE EXCEPTION 'Erro ao atualizar usuário completo: %', SQLERRM;
    END;

    OPEN ref FOR
    SELECT * FROM get_usuario_completo_v1(p_user_id);
END;
$$;
