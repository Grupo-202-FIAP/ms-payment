-- 1. Criar a tabela de QR Code primeiro (ou Payment)
CREATE TABLE public.tb_qr_code
(
    id           UUID           NOT NULL,
    currency     VARCHAR(255)   NOT NULL,
    expires_at   TIMESTAMPTZ(6) NULL,
    order_id     UUID           NOT NULL,
    qr_code      VARCHAR(2048)  NOT NULL,
    total_amount NUMERIC(38, 2) NOT NULL,
    payment_id   UUID NULL,
    CONSTRAINT tb_qr_code_pkey PRIMARY KEY (id)
);

-- 2. Criar a tabela de Pagamento
CREATE TABLE public.tb_payment
(
    id                UUID           NOT NULL,
    amount            NUMERIC(38, 2) NOT NULL,
    order_id          UUID NULL,
    payment_date_time TIMESTAMP(6) NULL,
    payment_method    VARCHAR(255)   NOT NULL,
    status            VARCHAR(255)   NOT NULL,
    updated_at        TIMESTAMP(6) NULL,
    qr_code_id        UUID NULL,
    CONSTRAINT tb_payment_pkey PRIMARY KEY (id),
    CONSTRAINT tb_payment_payment_method_check CHECK (payment_method = 'QR_CODE'),
    CONSTRAINT tb_payment_status_check CHECK (status IN ('PROCESSED', 'PENDING', 'EXPIRED')),
    CONSTRAINT uk_payment_order_id UNIQUE (order_id)
);

-- 3. Adicionar as chaves estrangeiras (Resolvendo a referência circular)
ALTER TABLE public.tb_payment
    ADD CONSTRAINT fk_payment_qr_code FOREIGN KEY (qr_code_id) REFERENCES public.tb_qr_code (id);

ALTER TABLE public.tb_qr_code
    ADD CONSTRAINT fk_qr_code_payment FOREIGN KEY (payment_id) REFERENCES public.tb_payment (id);

-- 4. Índices para performance (Recomendado)
CREATE INDEX idx_payment_order_id ON public.tb_payment (order_id);
CREATE INDEX idx_qr_code_order_id ON public.tb_qr_code (order_id);