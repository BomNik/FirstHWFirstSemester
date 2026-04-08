INSERT INTO tasks (title, description, completed, created_at, updated_at, due_date, priority, tags)
VALUES ('Подготовить отчёт',
        'Сделать финальную версию отчёта по проекту',
        FALSE,
        NOW(),
        NOW(),
        NOW() + INTERVAL '3 days',
        'HIGH',
        'work,report,urgent'),
       ('Купить продукты',
        'Молоко, хлеб, сыр',
        FALSE,
        NOW(),
        NOW(),
        NOW() + INTERVAL '1 day',
        'MEDIUM',
        'home,shopping');

INSERT INTO task_attachments (task_id, file_name, stored_file_name, content_type, size, uploaded_at)
VALUES (1, 'report.docx', '8f3a2c1e-report.docx',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 245760, NOW()),
       (1, 'diagram.png', '9b7d5f4a-diagram.png', 'image/png', 53248, NOW()),
       (2, 'list.txt', '1a2b3c4d-list.txt', 'text/plain', 128, NOW());
