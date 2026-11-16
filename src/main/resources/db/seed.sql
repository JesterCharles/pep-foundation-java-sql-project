-- Synthetic Lantern Tool Library data. Every name and email is invented. Parent rows come before the child rows
-- that point at them: categories and members before tools and loans, and a referring member before the members
-- they referred. Loan ids are generated in insert order (1-14).

INSERT INTO categories (code, label) VALUES
    ('HAND', 'Hand tools'),
    ('POWER', 'Power tools'),
    ('GARDEN', 'Garden'),
    ('LADDERS', 'Ladders and access'),
    ('CLEANING', 'Cleaning');

INSERT INTO members (id, full_name, email, tier, active, referred_by) VALUES
    ('M-0001', 'Avery Quill', 'avery.quill@example.test', 'REGULAR', TRUE, NULL),
    ('M-0002', 'Bram Okafor', 'bram.okafor@example.test', 'CHAMPION', TRUE, 'M-0001'),
    ('M-0003', 'Cleo Marsh', 'cleo.marsh@example.test', 'STARTER', TRUE, 'M-0001'),
    ('M-0004', 'Dana Whitlow', 'dana.whitlow@example.test', 'STARTER', TRUE, 'M-0002'),
    ('M-0005', 'Eli Northcott', 'eli.northcott@example.test', 'REGULAR', FALSE, NULL),
    ('M-0006', 'Fern Ito', 'fern.ito@example.test', 'STARTER', TRUE, NULL);

INSERT INTO tools (code, name, category_code, replacement_cents, status, shelf) VALUES
    ('LT-1001-2', 'Claw hammer', 'HAND', 1800, 'AVAILABLE', 'A-01'),
    ('LT-1002-3', 'Socket set', 'HAND', 4500, 'ON_LOAN', 'A-02'),
    ('LT-1003-4', 'Cordless drill', 'POWER', 12900, 'ON_LOAN', 'B-01'),
    ('LT-1004-5', 'Circular saw', 'POWER', 15900, 'AVAILABLE', 'B-02'),
    ('LT-1005-6', 'Orbital sander', 'POWER', 7900, 'IN_REPAIR', 'B-03'),
    ('LT-1006-7', 'Hedge trimmer', 'GARDEN', 8900, 'ON_LOAN', 'C-01'),
    ('LT-1007-8', 'Lawn aerator', 'GARDEN', 6500, 'AVAILABLE', 'C-02'),
    ('LT-1008-9', 'Wheelbarrow', 'GARDEN', 9900, 'AVAILABLE', 'C-03'),
    ('LT-1009-1', 'Step ladder', 'LADDERS', 7500, 'ON_LOAN', 'D-01'),
    ('LT-1010-2', 'Extension ladder', 'LADDERS', 18900, 'AVAILABLE', 'D-02'),
    ('LT-1011-3', 'Tile cutter', 'HAND', 5400, 'RETIRED', 'A-03'),
    ('LT-1012-4', 'Pipe wrench', 'HAND', 2600, 'AVAILABLE', 'A-01');

INSERT INTO loans (member_id, tool_code, loaned_on, due_on, returned_on, fee_cents) VALUES
    ('M-0001', 'LT-1001-2', DATE '2026-09-01', DATE '2026-09-15', DATE '2026-09-14', 0),
    ('M-0002', 'LT-1003-4', DATE '2026-09-03', DATE '2026-09-10', DATE '2026-09-14', 200),
    ('M-0003', 'LT-1006-7', DATE '2026-09-05', DATE '2026-09-15', DATE '2026-09-15', 0),
    ('M-0001', 'LT-1003-4', DATE '2026-09-20', DATE '2026-09-27', DATE '2026-09-27', 0),
    ('M-0004', 'LT-1009-1', DATE '2026-09-22', DATE '2026-09-29', DATE '2026-10-02', 225),
    ('M-0002', 'LT-1001-2', DATE '2026-09-25', DATE '2026-10-09', DATE '2026-10-08', 0),
    ('M-0005', 'LT-1007-8', DATE '2026-09-26', DATE '2026-10-06', DATE '2026-10-06', 0),
    ('M-0001', 'LT-1002-3', DATE '2026-10-01', DATE '2026-10-15', NULL, 0),
    ('M-0003', 'LT-1003-4', DATE '2026-10-02', DATE '2026-10-09', NULL, 0),
    ('M-0002', 'LT-1006-7', DATE '2026-10-03', DATE '2026-10-13', NULL, 0),
    ('M-0004', 'LT-1009-1', DATE '2026-10-05', DATE '2026-10-12', NULL, 0),
    ('M-0001', 'LT-1004-5', DATE '2026-09-10', DATE '2026-09-17', DATE '2026-09-16', 0),
    ('M-0003', 'LT-1010-2', DATE '2026-09-12', DATE '2026-09-19', DATE '2026-09-21', 150),
    ('M-0002', 'LT-1012-4', DATE '2026-09-15', DATE '2026-09-29', DATE '2026-09-29', 0);
