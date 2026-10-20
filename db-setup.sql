-- ===================================================================
-- PostgreSQL Database Initialization Script
-- Project: Student Accommodation & Hostel Fee Management System
-- ===================================================================

-- 1. Create the database (Run as superuser 'postgres' in psql or pgAdmin)
-- CREATE DATABASE accommodation_db;

-- 2. Connect to the database
-- \c accommodation_db;

-- Note: When running Spring Boot with 'spring.jpa.hibernate.ddl-auto=update',
-- Hibernate automatically creates all tables and foreign keys below.
-- You can run the following SQL statements manually if you wish to pre-create the schema:

-- Students Table
CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    register_number VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    year_of_study INTEGER NOT NULL,
    gender VARCHAR(20) NOT NULL DEFAULT 'MALE'
);

-- Accommodation Rooms Table
CREATE TABLE IF NOT EXISTS rooms (
    id BIGSERIAL PRIMARY KEY,
    room_number VARCHAR(50) NOT NULL UNIQUE,
    building_name VARCHAR(150) NOT NULL,
    room_type VARCHAR(100) NOT NULL,
    capacity INTEGER NOT NULL,
    occupied_beds INTEGER NOT NULL DEFAULT 0,
    monthly_rent DOUBLE PRECISION NOT NULL,
    hostel_type VARCHAR(50) NOT NULL DEFAULT 'BOYS',
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE'
);

-- Bookings Table (Foreign keys referencing students and rooms)
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    booking_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_booking_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT
);

-- Hostel Fee Records Table
CREATE TABLE IF NOT EXISTS hostel_fees (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    hostel_name VARCHAR(150) NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    fee_type VARCHAR(100) NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    paid_amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    remaining_balance DOUBLE PRECISION NOT NULL,
    due_date DATE NOT NULL,
    payment_date DATE,
    payment_method VARCHAR(100),
    transaction_reference VARCHAR(150),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fee_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- Payment Transactions Audit Table
CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    fee_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(100) NOT NULL,
    transaction_reference VARCHAR(150) NOT NULL UNIQUE,
    receipt_number VARCHAR(100) NOT NULL UNIQUE,
    recorded_by VARCHAR(100) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_fee FOREIGN KEY (fee_id) REFERENCES hostel_fees(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

-- In-App Notifications & Alerts Table
CREATE TABLE IF NOT EXISTS in_app_notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_role VARCHAR(50) NOT NULL,
    student_id BIGINT,
    fee_id BIGINT,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    link_url VARCHAR(255),
    dedup_key VARCHAR(255) UNIQUE,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_fee FOREIGN KEY (fee_id) REFERENCES hostel_fees(id) ON DELETE SET NULL
);

-- JPA Inheritance Mapping (JOINED Strategy)
-- Parent Table: persons
CREATE TABLE IF NOT EXISTS persons (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

-- Child Table: student_persons (person_id is PK and FK referencing persons.id)
CREATE TABLE IF NOT EXISTS student_persons (
    person_id BIGINT PRIMARY KEY,
    roll_number VARCHAR(100) NOT NULL,
    course VARCHAR(150) NOT NULL,
    CONSTRAINT fk_student_person_id FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE
);
