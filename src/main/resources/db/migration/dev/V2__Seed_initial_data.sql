-- Seed initial data for development/testing
-- This migration adds sample data for local development and testing
-- Only runs in development environments (local, test profiles)
-- 
-- To disable in production, use: spring.flyway.locations=classpath:db/migration/prod
-- To enable in dev, use: spring.flyway.locations=classpath:db/migration,classpath:db/migration/dev

-- Insert specialties
INSERT INTO specialties (name) VALUES 
('Cardiology'),
('Dermatology'),
('Neurology'),
('Orthopedics'),
('Pediatrics')
ON CONFLICT (name) DO NOTHING;

-- Insert time slots
INSERT INTO time_slots (start_time, end_time) VALUES 
('09:00:00', '10:00:00'),
('10:00:00', '11:00:00'),
('11:00:00', '12:00:00'),
('14:00:00', '15:00:00'),
('15:00:00', '16:00:00'),
('16:00:00', '17:00:00')
ON CONFLICT (start_time, end_time) DO NOTHING;

-- Insert admin users (password: password)
INSERT INTO admins (name, email, password) VALUES 
('Admin User', 'admin@uphill.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi')
ON CONFLICT (email) DO NOTHING;

-- Insert patients
INSERT INTO patients (name, email, dob, medical_history) VALUES 
('John Doe', 'john.doe@email.com', '1985-03-15', 'No significant medical history'),
('Jane Smith', 'jane.smith@email.com', '1990-07-22', 'Allergic to penicillin'),
('Bob Johnson', 'bob.johnson@email.com', '1978-11-08', 'Hypertension, diabetes type 2'),
('Alice Brown', 'alice.brown@email.com', '1992-05-14', 'Asthma'),
('Charlie Wilson', 'charlie.wilson@email.com', '1988-09-30', 'No significant medical history')
ON CONFLICT (email) DO NOTHING;

-- Insert doctors
INSERT INTO doctors (name, email, external_id, specialty_id) VALUES 
('Dr. Sarah Johnson', 'sarah.johnson@hospital.com', 'DOC001', (SELECT id FROM specialties WHERE name = 'Cardiology' LIMIT 1)),
('Dr. Michael Chen', 'michael.chen@hospital.com', 'DOC002', (SELECT id FROM specialties WHERE name = 'Dermatology' LIMIT 1)),
('Dr. Emily Davis', 'emily.davis@hospital.com', 'DOC003', (SELECT id FROM specialties WHERE name = 'Neurology' LIMIT 1)),
('Dr. Robert Wilson', 'robert.wilson@hospital.com', 'DOC004', (SELECT id FROM specialties WHERE name = 'Orthopedics' LIMIT 1)),
('Dr. Lisa Anderson', 'lisa.anderson@hospital.com', 'DOC005', (SELECT id FROM specialties WHERE name = 'Pediatrics' LIMIT 1))
ON CONFLICT (email) DO NOTHING;

-- Insert rooms
INSERT INTO rooms (name, room_external_id, location) VALUES 
('Room 101', 'ROOM001', 'First Floor - Cardiology Wing'),
('Room 102', 'ROOM002', 'First Floor - Cardiology Wing'),
('Room 201', 'ROOM003', 'Second Floor - Dermatology Wing'),
('Room 202', 'ROOM004', 'Second Floor - Dermatology Wing'),
('Room 301', 'ROOM005', 'Third Floor - Neurology Wing'),
('Room 302', 'ROOM006', 'Third Floor - Neurology Wing'),
('Room 401', 'ROOM007', 'Fourth Floor - Orthopedics Wing'),
('Room 402', 'ROOM008', 'Fourth Floor - Orthopedics Wing'),
('Room 501', 'ROOM009', 'Fifth Floor - Pediatrics Wing'),
('Room 502', 'ROOM010', 'Fifth Floor - Pediatrics Wing')
ON CONFLICT (room_external_id) WHERE room_external_id IS NOT NULL DO NOTHING;
