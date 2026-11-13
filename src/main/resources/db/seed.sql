INSERT INTO members (full_name, email) VALUES
    ('Amara Holt', 'amara@example.test'),
    ('Theo Lin', 'theo@example.test');

INSERT INTO tools (name, category, status) VALUES
    ('Cordless Drill', 'carpentry', 'AVAILABLE'),
    ('Tile Cutter', 'renovation', 'ON_LOAN'),
    ('Garden Rake', 'gardening', 'AVAILABLE'),
    ('Orbital Sander', 'carpentry', 'MAINTENANCE'),
    ('Claw Hammer', 'carpentry', 'AVAILABLE');

INSERT INTO loans (member_id, tool_id, borrowed_on, due_on, returned_on) VALUES
    (1, 2, DATE '2030-01-05', DATE '2030-01-12', NULL),
    (2, 1, DATE '2029-12-01', DATE '2029-12-08', DATE '2029-12-05');
