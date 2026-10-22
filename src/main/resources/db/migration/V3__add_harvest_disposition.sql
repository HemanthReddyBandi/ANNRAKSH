ALTER TABLE cultivation_plans
    ADD COLUMN sold_quantity NUMERIC(14, 3) NOT NULL DEFAULT 0,
    ADD COLUMN stored_quantity NUMERIC(14, 3) NOT NULL DEFAULT 0,
    ADD COLUMN discarded_quantity NUMERIC(14, 3) NOT NULL DEFAULT 0;
