CREATE OR REPLACE PROCEDURE public.criar_usuario_completo_v2(
    IN p_username text,
    IN p_password text,
    IN p_user_ativo boolean,
    IN p_nome text,
    IN p_cpf bigint,
    IN p_cliente_ativo boolean,
    IN p_rua text,
    IN p_numero text,
    IN p_bairro text,
    IN p_cidade text,
    IN p_estado text,
    IN p_complemento text,
    IN p_cep text,
    IN p_role text,
    OUT p_result refcursor)
LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    v_user_id INT;
    v_cliente_id INT;
    v_endereco_id INT;
    v_role_id INT;
BEGIN
    -- Cria usuário
    v_user_id := criar_usuario_v1(p_username, p_password, p_user_ativo);

    -- Verifica se role existe
    SELECT id INTO v_role_id FROM roles WHERE name = p_role;

    IF v_role_id IS NULL THEN
        -- Cria role se não existir
        INSERT INTO roles (name) VALUES (p_role) RETURNING id INTO v_role_id;
    END IF;

    -- Vincula role ao usuário
    PERFORM vincular_role_v1(v_user_id, p_role);

    -- Cria cliente
    v_cliente_id := criar_cliente_v1(p_nome, p_cpf, p_cliente_ativo, v_user_id);

    -- Cria endereço se rua foi passada
    IF p_rua IS NOT NULL THEN
        v_endereco_id := criar_endereco_v1(p_cep, p_rua, p_numero, p_bairro, p_cidade, p_estado, p_complemento, v_cliente_id);
        RAISE NOTICE 'Endereço criado com id: %', v_endereco_id;
    END IF;

    -- Retorna usuário completo
    OPEN p_result FOR SELECT * FROM get_usuario_completo_v1(v_user_id);
END;
$BODY$;
