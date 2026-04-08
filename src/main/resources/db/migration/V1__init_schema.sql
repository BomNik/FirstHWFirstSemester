CREATE TABLE tasks
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    completed   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    due_date    TIMESTAMP,
    priority    VARCHAR(16)  NOT NULL,
    tags        TEXT
);

CREATE TABLE task_attachments
(
    id               BIGSERIAL PRIMARY KEY,
    task_id          BIGINT       NOT NULL,
    file_name        VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    content_type     VARCHAR(255) NOT NULL,
    size             BIGINT       NOT NULL,
    uploaded_at      TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_task_attachments_task
        FOREIGN KEY (task_id)
            REFERENCES tasks (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_task_attachments_task_id
    ON task_attachments (task_id);
