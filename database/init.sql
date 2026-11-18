-- =========================================================================
-- DATABASE INITIALIZATION & MIGRATION SCRIPT
-- Platform: Event Ticketing, Booking, Payment & E-Ticket Issuance Engine
-- Engine: PostgreSQL 16
-- =========================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. DROP TABLES IF EXISTS (CLEAN SLATE)
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS ticket_tiers CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- -------------------------------------------------------------------------
-- TABLE: USERS
-- -------------------------------------------------------------------------
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- -------------------------------------------------------------------------
-- TABLE: EVENTS
-- -------------------------------------------------------------------------
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    venue VARCHAR(150) NOT NULL,
    event_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX idx_events_status_date ON events(status, event_date);

-- -------------------------------------------------------------------------
-- TABLE: TICKET_TIERS
-- Catatan: available_quota dikunci menggunakan SELECT ... FOR UPDATE
-- -------------------------------------------------------------------------
CREATE TABLE ticket_tiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    tier_name VARCHAR(50) NOT NULL,
    price NUMERIC(15, 2) NOT NULL,
    total_quota INT NOT NULL,
    available_quota INT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT chk_available_quota CHECK (available_quota >= 0)
);

CREATE INDEX idx_ticket_tiers_event_id ON ticket_tiers(event_id);

-- -------------------------------------------------------------------------
-- TABLE: ORDERS
-- -------------------------------------------------------------------------
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    event_id UUID NOT NULL REFERENCES events(id),
    tier_id UUID NOT NULL REFERENCES ticket_tiers(id),
    quantity INT NOT NULL,
    total_amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_event_tier ON orders(event_id, tier_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_number ON orders(order_number);

-- -------------------------------------------------------------------------
-- TABLE: PAYMENTS
-- -------------------------------------------------------------------------
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL UNIQUE REFERENCES orders(id),
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    payment_method VARCHAR(30) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    paid_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX idx_payments_idempotency ON payments(idempotency_key);
CREATE INDEX idx_payments_order_id ON payments(order_id);

-- -------------------------------------------------------------------------
-- TABLE: AUDIT_LOGS
-- -------------------------------------------------------------------------
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(50) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_name, entity_id);

-- =========================================================================
-- SEED INITIAL DATA
-- =========================================================================

-- 1. Default Users (Passwords BCrypt hashed)
-- admin / AdminPass123!
-- fazri / Password123!
INSERT INTO users (id, username, email, password, full_name, role) VALUES
('a0000000-0000-0000-0000-000000000001', 'admin', 'admin@fintech.com', '$2a$10$wO7bA3oI.PqU84c718qV3e6sN6fW29Z7/j9N0nN9Gz5i9Gk4W4m4.', 'System Administrator', 'ROLE_ADMIN'),
('a0000000-0000-0000-0000-000000000002', 'fazri', 'fazri@fintech.com', '$2a$10$D8bY9c0YpE9R0mN1V7f/0.bS7pL4e5O8V6m5K4H9R3b1G7k4W4m4.', 'Muhammad Fazri Deffara Riswandy', 'ROLE_USER')
ON CONFLICT (username) DO NOTHING;

-- 2. Master Events
INSERT INTO events (id, name, description, venue, event_date, status) VALUES
('e0000000-0000-0000-0000-000000000001', 'Jakarta Summer Music Festival 2026', 'International open-air musical festival featuring multiple electronic and indie stages.', 'Gelora Bung Karno Main Stadium, Jakarta', CURRENT_TIMESTAMP + INTERVAL '60 days', 'PUBLISHED'),
('e0000000-0000-0000-0000-000000000002', 'Nusantara Symphony Orchestra Live 2026', 'Magnificent classical and modern orchestral performance.', 'Jakarta International Theater, Jakarta', CURRENT_TIMESTAMP + INTERVAL '90 days', 'PUBLISHED')
ON CONFLICT DO NOTHING;

-- 3. Ticket Tiers for Jakarta Summer Music Festival 2026
INSERT INTO ticket_tiers (id, event_id, tier_name, price, total_quota, available_quota) VALUES
('t0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'VIP Infinity Lounge', 5000000.00, 100, 100),
('t0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000001', 'General Admission', 2500000.00, 500, 500),
('t0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000001', 'Tribune Seated', 1500000.00, 300, 300)
ON CONFLICT DO NOTHING;

-- 4. Ticket Tiers for Nusantara Symphony Orchestra Live 2026
INSERT INTO ticket_tiers (id, event_id, tier_name, price, total_quota, available_quota) VALUES
('t0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000002', 'Diamond Tier', 4500000.00, 150, 150),
('t0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000002', 'Gold Tier', 1200000.00, 400, 400)
ON CONFLICT DO NOTHING;
