
CREATE TYPE appointment_status AS ENUM (
  'BOOKED',
  'CONFIRMED',
  'CANCELLED',
  'COMPLETED'
);

CREATE TABLE admins (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  password TEXT NOT NULL
);

CREATE INDEX idx_admins_name ON admins(name);

CREATE TABLE patients (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  dob TEXT NOT NULL,
  medical_history TEXT
);

CREATE TABLE specialties (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL UNIQUE
);

CREATE TABLE doctors (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  external_id TEXT,
  specialty_id BIGINT NOT NULL REFERENCES specialties(id)
);

CREATE INDEX idx_doctors_specialty_id ON doctors(specialty_id);
CREATE UNIQUE INDEX idx_doctors_external_id ON doctors(external_id) WHERE external_id IS NOT NULL;

CREATE TABLE rooms (
  id BIGSERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  room_external_id TEXT,
  location TEXT
);

CREATE UNIQUE INDEX idx_rooms_external_id ON rooms(room_external_id) WHERE room_external_id IS NOT NULL;

CREATE TABLE time_slots (
  id BIGSERIAL PRIMARY KEY,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  UNIQUE (start_time, end_time)
);

CREATE TABLE appointments (
  id BIGSERIAL PRIMARY KEY,
  doctor_id BIGINT NOT NULL REFERENCES doctors(id),
  patient_id BIGINT NOT NULL REFERENCES patients(id),
  room_id BIGINT NOT NULL REFERENCES rooms(id),
  time_slot_id BIGINT NOT NULL REFERENCES time_slots(id),
  date DATE NOT NULL,
  status TEXT NOT NULL DEFAULT 'BOOKED',

  UNIQUE (doctor_id, date, time_slot_id),
  UNIQUE (room_id, date, time_slot_id)
);

CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_room_id ON appointments(room_id);
CREATE INDEX idx_appointments_time_slot_id ON appointments(time_slot_id);
CREATE INDEX idx_appointments_date_timeslot_id ON appointments(date, time_slot_id, id);
CREATE INDEX idx_appointments_status_date ON appointments(status, date);

CREATE TABLE activity_logs (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  action TEXT NOT NULL,
  description TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_logs_user_created ON activity_logs(user_id, created_at);
CREATE INDEX idx_activity_logs_created_at ON activity_logs(created_at);
CREATE INDEX idx_activity_logs_action ON activity_logs(action);

CREATE TABLE outbox_events (
  id BIGSERIAL PRIMARY KEY,
  aggregate_id BIGINT NOT NULL,
  aggregate_type TEXT NOT NULL,
  event_type TEXT NOT NULL,
  payload JSONB NOT NULL,
  status TEXT NOT NULL DEFAULT 'PENDING',
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  
  UNIQUE (aggregate_id, event_type, status)
);

CREATE INDEX idx_outbox_events_status_created_at ON outbox_events(status, created_at);
CREATE INDEX idx_outbox_events_aggregate ON outbox_events(aggregate_type, aggregate_id);
CREATE INDEX idx_outbox_events_pending ON outbox_events(created_at) WHERE status = 'PENDING';
CREATE INDEX idx_outbox_events_status ON outbox_events(status);
CREATE INDEX idx_outbox_events_status_next_retry ON outbox_events(status, next_retry_at);
