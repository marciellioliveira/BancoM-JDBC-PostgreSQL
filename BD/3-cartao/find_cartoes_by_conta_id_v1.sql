CREATE OR REPLACE FUNCTION public.find_cartoes_by_conta_id_v1(p_conta_id BIGINT)
RETURNS TABLE (
    id BIGINT,
    numero_cartao TEXT,
    status BOOLEAN,
    tipo_conta TEXT,
    categoria_conta TEXT,
    tipo_cartao TEXT,
    senha TEXT,
    saldo_conta NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id,
        c.numero_cartao::TEXT,
        c.status,
        c.tipo_conta::TEXT,
        c.categoria_conta::TEXT,
        c.tipo_cartao::TEXT,
        c.senha::TEXT,
        t.saldo_conta
    FROM cartoes c
    JOIN contas t ON c.conta_id = t.id
    WHERE c.conta_id = p_conta_id;
END;
$$ LANGUAGE plpgsql;
