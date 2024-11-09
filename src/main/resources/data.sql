-- LINTING RULES
INSERT INTO rule (id, created_at, updated_at, name, type, value_type)
VALUES ('d3908a96-da94-49b6-884b-f5b3a618e567', '2024-09-11 01:01:01.000001', '2024-09-11 01:01:01.000001', 'allow_expression_in_println', 'LINT', 'BOOLEAN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule (id, created_at, updated_at, name, type, value_type)
VALUES ('3b714f82-e849-45f0-a483-4ac13ecb6142', '2024-09-11 01:01:01.000001', '2024-09-11 01:01:01.000001', 'allow_expression_in_readinput', 'LINT', 'BOOLEAN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule (id, created_at, updated_at, name, type, value_type)
VALUES ('e5c540e1-2d77-4c3b-b5dd-40d6eefbeaed', '2024-09-11 01:01:01.000001', '2024-09-11 01:01:01.000001', 'naming_convention', 'LINT', 'STRING')
ON CONFLICT (id) DO NOTHING;

-- FORMAT RULES
INSERT INTO rule (id, created_at, updated_at, name, type, value_type)
VALUES ('1e7a876b-cece-4413-b38c-f27729301583', '2024-09-11 01:01:01.000001', '2024-09-11 01:01:01.000001', 'declaration_colon_trailing_whitespaces', 'FORMAT', 'BOOLEAN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule (id, created_at, updated_at, name, type, value_type)
VALUES ('d7eb83f5-d065-44d8-8f48-11c56f9dfe6a', '2024-09-11 01:01:01.000001', '2024-09-11 01:01:01.000001', 'declaration_colon_leading_whitespaces', 'FORMAT', 'BOOLEAN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule (id, created_at, updated_at, name, type, value_type)
VALUES ('48ff9dfd-b553-4f95-a4e5-4268e10549de', '2024-09-11 01:01:01.000001', '2024-09-11 01:01:01.000001', 'assignation_equal_wrap_whitespaces', 'FORMAT', 'BOOLEAN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO rule (id, created_at, updated_at, name, type, value_type)
VALUES ('59149a42-2812-4244-ad8b-1df5daff7db0', '2024-09-11 01:01:01.000001', '2024-09-11 01:01:01.000001', 'println_trailing_line_jump', 'FORMAT', 'INTEGER')
ON CONFLICT (id) DO NOTHING;
