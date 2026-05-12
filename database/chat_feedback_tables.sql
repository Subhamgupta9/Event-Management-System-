CREATE TABLE messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_id INT NOT NULL,
    group_id INT NULL,
    event_id INT NULL,
    message_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_messages_scope CHECK (
        (group_id IS NOT NULL AND event_id IS NULL)
        OR (group_id IS NULL AND event_id IS NOT NULL)
    ),
    CONSTRAINT fk_messages_sender
        FOREIGN KEY (sender_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_messages_group
        FOREIGN KEY (group_id) REFERENCES user_groups(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_messages_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_messages_group_created ON messages(group_id, created_at);
CREATE INDEX idx_messages_event_created ON messages(event_id, created_at);

CREATE TABLE feedback (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    user_id INT NOT NULL,
    rating TINYINT NOT NULL,
    comment VARCHAR(500) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_feedback_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_feedback_event_user UNIQUE (event_id, user_id),
    CONSTRAINT fk_feedback_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_feedback_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_feedback_event_created ON feedback(event_id, created_at);
