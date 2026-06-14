-- V2: seed default categories so images can refer to them.
INSERT INTO categories (name) VALUES 
('Tech'),
('Nature'),
('Architecture'),
('Aesthetics')
ON CONFLICT (name) DO NOTHING;
