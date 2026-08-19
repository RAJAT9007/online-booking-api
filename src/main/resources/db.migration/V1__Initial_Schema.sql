-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(500) NOT NULL,
    role VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create cities table
CREATE TABLE IF NOT EXISTS cities (
    id BIGSERIAL PRIMARY KEY,
    city_name VARCHAR(255) UNIQUE NOT NULL
);

-- Create movies table
CREATE TABLE IF NOT EXISTS movies (
    id BIGSERIAL PRIMARY KEY,
    movie_name VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    rating DOUBLE PRECISION,
    duration INTEGER
);

-- Create theatres table
CREATE TABLE IF NOT EXISTS theatres (
    id BIGSERIAL PRIMARY KEY,
    theatre_name VARCHAR(255) NOT NULL,
    city_id BIGINT NOT NULL,
    location VARCHAR(500),
    FOREIGN KEY (city_id) REFERENCES cities(id)
);

-- Create theater_screens table (NO price column)
CREATE TABLE IF NOT EXISTS theater_screens (
    id BIGSERIAL PRIMARY KEY,
    theater_id BIGINT NOT NULL,
    screen_name VARCHAR(100) NOT NULL,
    total_seats INTEGER NOT NULL,
    status VARCHAR(50),
    FOREIGN KEY (theater_id) REFERENCES theatres(id)
);

-- Create seats table (HAS price column)
CREATE TABLE IF NOT EXISTS seats (
    id BIGSERIAL PRIMARY KEY,
    screen_id BIGINT NOT NULL,
    seat_number VARCHAR(50) NOT NULL,
    seat_type VARCHAR(50) NOT NULL,
    row_name VARCHAR(10) NOT NULL,
    status VARCHAR(50),
    price DOUBLE PRECISION,
    FOREIGN KEY (screen_id) REFERENCES theater_screens(id),
    UNIQUE(screen_id, seat_number)
);

-- Create screen_timing table (Show entity, HAS price column)
CREATE TABLE IF NOT EXISTS screen_timing (
    show_id BIGSERIAL PRIMARY KEY,
    screen_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    language VARCHAR(50),
    price DOUBLE PRECISION,
    available_seats INTEGER,
    status VARCHAR(50),
    FOREIGN KEY (screen_id) REFERENCES theater_screens(id),
    FOREIGN KEY (movie_id) REFERENCES movies(id)
);

-- Create show_seats table
CREATE TABLE IF NOT EXISTS show_seats (
    id BIGSERIAL PRIMARY KEY,
    show_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    price DOUBLE PRECISION,
    status VARCHAR(50),
    locked_by_user_id BIGINT,
    lock_expiry_time TIMESTAMP,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (show_id) REFERENCES screen_timing(show_id),
    FOREIGN KEY (seat_id) REFERENCES seats(id),
    UNIQUE(show_id, seat_id)
);

-- Create bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    show_id BIGINT NOT NULL,
    booking_time TIMESTAMP NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version BIGINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (show_id) REFERENCES screen_timing(show_id)
);

-- Create booking_seats table
CREATE TABLE IF NOT EXISTS booking_seats (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id),
    UNIQUE(booking_id, seat_id)
);

-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    payment_method VARCHAR(50),
    payment_date TIMESTAMP,
    amount DECIMAL(10, 2),
    status VARCHAR(50),
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_bookings_show_id ON bookings(show_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_time ON bookings(booking_time);
CREATE INDEX IF NOT EXISTS idx_booking_seats_booking_id ON booking_seats(booking_id);
CREATE INDEX IF NOT EXISTS idx_booking_seats_seat_id ON booking_seats(seat_id);
CREATE INDEX IF NOT EXISTS idx_show_seats_show_id ON show_seats(show_id);
CREATE INDEX IF NOT EXISTS idx_show_seats_status ON show_seats(status);
CREATE INDEX IF NOT EXISTS idx_seats_screen_id ON seats(screen_id);
CREATE INDEX IF NOT EXISTS idx_screen_timing_screen_id ON screen_timing(screen_id);
CREATE INDEX IF NOT EXISTS idx_screen_timing_movie_id ON screen_timing(movie_id);

