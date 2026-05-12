USE event_management_system;

DELETE FROM events
WHERE title IN (
    'Hackathon 2026',
    'Annual Cultural Night',
    'Volunteer Orientation'
);
