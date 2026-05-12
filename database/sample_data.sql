USE event_management_system;

INSERT INTO users (full_name, email, password, role) VALUES
('Admin User', 'admin@ems.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'ADMIN'),
('Organizer One', 'organizer@ems.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ORGANIZER'),
('Member One', 'member1@ems.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'MEMBER'),
('Member Two', 'member2@ems.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'MEMBER');

INSERT INTO user_groups (name, description, created_by) VALUES
('Tech Club', 'Handles coding and technical event activities.', 2),
('Cultural Team', 'Coordinates dance, music, and arts events.', 1);

INSERT INTO group_members (group_id, user_id) VALUES
(1, 2),
(1, 3),
(1, 4),
(2, 3),
(2, 4);

-- Sample login passwords:
-- admin@ems.com      -> admin
-- organizer@ems.com  -> password123
-- member1@ems.com    -> password123
-- member2@ems.com    -> password123
