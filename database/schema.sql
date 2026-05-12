DROP DATABASE IF EXISTS event_management_system;
CREATE DATABASE event_management_system;
USE event_management_system;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'ORGANIZER', 'MEMBER') NOT NULL DEFAULT 'MEMBER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_groups (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_groups_users
        FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE group_members (
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id),
    CONSTRAINT fk_group_members_group
        FOREIGN KEY (group_id) REFERENCES user_groups(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_group_members_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE events (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    event_date DATE NOT NULL,
    location VARCHAR(150) NOT NULL,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_events_users
        FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_events_event_date ON events(event_date);
CREATE INDEX idx_events_location ON events(location);
CREATE INDEX idx_events_created_by ON events(created_by);

CREATE TABLE event_groups (
    event_id INT NOT NULL,
    group_id INT NOT NULL,
    PRIMARY KEY (event_id, group_id),
    CONSTRAINT fk_event_groups_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_event_groups_group
        FOREIGN KEY (group_id) REFERENCES user_groups(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_event_groups_group_id ON event_groups(group_id);

CREATE TABLE invitations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    event_id INT NOT NULL,
    invited_user_id INT NOT NULL,
    source_group_id INT NULL,
    sent_by INT NOT NULL,
    message VARCHAR(255) NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    CONSTRAINT fk_invitations_event
        FOREIGN KEY (event_id) REFERENCES events(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_invitations_user
        FOREIGN KEY (invited_user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_invitations_group
        FOREIGN KEY (source_group_id) REFERENCES user_groups(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_invitations_sender
        FOREIGN KEY (sent_by) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_invitations_invited_user_status ON invitations(invited_user_id, status);
CREATE INDEX idx_invitations_event_id ON invitations(event_id);

CREATE TABLE notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    message VARCHAR(255) NOT NULL,
    link_url VARCHAR(255) NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);

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
