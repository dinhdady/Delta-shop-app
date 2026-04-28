-- ============================================================
-- Delta Sports - Database Schema V1
-- Advanced PostgreSQL Schema with Full E-commerce Support
-- ============================================================

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";      -- Full-text search
CREATE EXTENSION IF NOT EXISTS "unaccent";      -- Vietnamese accent-insensitive search
CREATE EXTENSION IF NOT EXISTS "btree_gin";     -- GIN indexes

-- ============================================================
-- ENUMS
-- ============================================================

CREATE TYPE user_role AS ENUM ('CUSTOMER', 'ADMIN', 'SUPER_ADMIN');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'BANNED', 'PENDING_VERIFICATION');
CREATE TYPE order_status AS ENUM (
    'PENDING', 'CONFIRMED', 'PROCESSING',
    'SHIPPED', 'DELIVERED', 'CANCELLED', 'REFUNDED'
);
CREATE TYPE payment_status AS ENUM ('PENDING', 'PAID', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED');
CREATE TYPE payment_method AS ENUM ('COD', 'VNPAY', 'MOMO', 'BANK_TRANSFER', 'CREDIT_CARD');
CREATE TYPE discount_type AS ENUM ('PERCENTAGE', 'FIXED_AMOUNT', 'FREE_SHIPPING', 'BUY_X_GET_Y');
CREATE TYPE product_status AS ENUM ('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK', 'DISCONTINUED');
CREATE TYPE review_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE notification_type AS ENUM (
    'ORDER_PLACED', 'ORDER_CONFIRMED', 'ORDER_SHIPPED',
    'ORDER_DELIVERED', 'ORDER_CANCELLED', 'PAYMENT_SUCCESS',
    'PAYMENT_FAILED', 'REVIEW_APPROVED', 'PROMOTION', 'SYSTEM'
);
CREATE TYPE address_type AS ENUM ('HOME', 'OFFICE', 'OTHER');
CREATE TYPE stock_movement_type AS ENUM ('PURCHASE', 'SALE', 'RETURN', 'ADJUSTMENT', 'DAMAGED');

-- ============================================================
-- CORE TABLES
-- ============================================================

-- Users
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    phone           VARCHAR(20) UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    avatar_url      VARCHAR(500),
    date_of_birth   DATE,
    role            user_role NOT NULL DEFAULT 'CUSTOMER',
    status          user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified  BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at   TIMESTAMP WITH TIME ZONE,
    loyalty_points  INTEGER NOT NULL DEFAULT 0,
    total_spent     NUMERIC(15, 2) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMP WITH TIME ZONE   -- soft delete
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE,
    user_agent  VARCHAR(500),
    ip_address  INET,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Email verification tokens
CREATE TABLE verification_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(255) NOT NULL UNIQUE,
    type        VARCHAR(50) NOT NULL,  -- EMAIL_VERIFY, PASSWORD_RESET, PHONE_OTP
    expires_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at     TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- User addresses
CREATE TABLE user_addresses (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type            address_type NOT NULL DEFAULT 'HOME',
    recipient_name  VARCHAR(200) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    province        VARCHAR(100) NOT NULL,
    district        VARCHAR(100) NOT NULL,
    ward            VARCHAR(100) NOT NULL,
    street_address  VARCHAR(500) NOT NULL,
    postal_code     VARCHAR(20),
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- PRODUCT CATALOG
-- ============================================================

-- Sport categories (hierarchical)
CREATE TABLE categories (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_id       UUID REFERENCES categories(id) ON DELETE SET NULL,
    name            VARCHAR(200) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    image_url       VARCHAR(500),
    icon_class      VARCHAR(100),
    sort_order      INTEGER NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    meta_title      VARCHAR(300),
    meta_description VARCHAR(500),
    path            TEXT,               -- materialized path: /uuid/uuid/uuid
    depth           SMALLINT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Brands
CREATE TABLE brands (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(200) NOT NULL UNIQUE,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    logo_url        VARCHAR(500),
    website_url     VARCHAR(500),
    description     TEXT,
    country_of_origin VARCHAR(100),
    is_featured     BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Attribute definitions (e.g. Size, Color, Material)
CREATE TABLE attributes (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(50) NOT NULL UNIQUE,
    type        VARCHAR(30) NOT NULL DEFAULT 'TEXT', -- TEXT, COLOR, SIZE, NUMBER
    unit        VARCHAR(20),
    is_filterable BOOLEAN NOT NULL DEFAULT TRUE,
    is_required   BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order  INTEGER NOT NULL DEFAULT 0
);

-- Products (master product)
CREATE TABLE products (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id         UUID NOT NULL REFERENCES categories(id),
    brand_id            UUID REFERENCES brands(id),
    name                VARCHAR(500) NOT NULL,
    slug                VARCHAR(600) NOT NULL UNIQUE,
    sku                 VARCHAR(100) UNIQUE,
    short_description   TEXT,
    description         TEXT,
    status              product_status NOT NULL DEFAULT 'ACTIVE',
    base_price          NUMERIC(15, 2) NOT NULL,
    compare_price       NUMERIC(15, 2),         -- Original price before discount
    cost_price          NUMERIC(15, 2),         -- Internal cost
    weight              NUMERIC(8, 3),          -- kg
    length              NUMERIC(8, 2),          -- cm
    width               NUMERIC(8, 2),          -- cm
    height              NUMERIC(8, 2),          -- cm
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    is_new_arrival      BOOLEAN NOT NULL DEFAULT FALSE,
    is_best_seller      BOOLEAN NOT NULL DEFAULT FALSE,
    tags                VARCHAR(100)[],
    sport_types         VARCHAR(100)[],         -- football, basketball, swimming, etc.
    total_sold          INTEGER NOT NULL DEFAULT 0,
    total_views         INTEGER NOT NULL DEFAULT 0,
    average_rating      NUMERIC(3, 2) NOT NULL DEFAULT 0,
    review_count        INTEGER NOT NULL DEFAULT 0,
    meta_title          VARCHAR(300),
    meta_description    VARCHAR(500),
    meta_keywords       VARCHAR(500),
    search_vector       TSVECTOR,              -- Full-text search
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMP WITH TIME ZONE   -- soft delete
);

-- Product variants (size/color combinations)
CREATE TABLE product_variants (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku             VARCHAR(150) NOT NULL UNIQUE,
    name            VARCHAR(300),
    price_modifier  NUMERIC(10, 2) NOT NULL DEFAULT 0,  -- +/- from base_price
    final_price     NUMERIC(15, 2) GENERATED ALWAYS AS (0) STORED, -- computed in app
    stock_quantity  INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,       -- in checkout but not paid
    min_stock_alert INTEGER NOT NULL DEFAULT 5,
    barcode         VARCHAR(100),
    image_url       VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    weight          NUMERIC(8, 3),
    sort_order      INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_stock_positive CHECK (stock_quantity >= 0)
);

-- Variant attribute values (e.g. Size=XL, Color=Red)
CREATE TABLE variant_attribute_values (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    variant_id      UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    attribute_id    UUID NOT NULL REFERENCES attributes(id),
    value           VARCHAR(200) NOT NULL,
    display_value   VARCHAR(200),               -- e.g. "Đỏ" for "RED"
    color_code      VARCHAR(10),                -- Hex for color attributes
    UNIQUE (variant_id, attribute_id)
);

-- Product images
CREATE TABLE product_images (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id      UUID REFERENCES product_variants(id) ON DELETE SET NULL,
    url             VARCHAR(500) NOT NULL,
    public_id       VARCHAR(255),               -- Cloudinary public ID
    alt_text        VARCHAR(300),
    sort_order      INTEGER NOT NULL DEFAULT 0,
    is_primary      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Product related / cross-sell
CREATE TABLE product_relations (
    product_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    related_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    type        VARCHAR(30) NOT NULL DEFAULT 'RELATED', -- RELATED, CROSS_SELL, UP_SELL
    sort_order  INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (product_id, related_id, type)
);

-- ============================================================
-- INVENTORY
-- ============================================================

CREATE TABLE stock_movements (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    variant_id      UUID NOT NULL REFERENCES product_variants(id),
    order_id        UUID,                       -- FK added later
    type            stock_movement_type NOT NULL,
    quantity        INTEGER NOT NULL,           -- positive=in, negative=out
    quantity_before INTEGER NOT NULL,
    quantity_after  INTEGER NOT NULL,
    note            TEXT,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- PROMOTIONS & DISCOUNTS
-- ============================================================

CREATE TABLE promotions (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                VARCHAR(300) NOT NULL,
    code                VARCHAR(50) UNIQUE,     -- NULL for auto-applied promotions
    description         TEXT,
    type                discount_type NOT NULL,
    value               NUMERIC(10, 2) NOT NULL,  -- % or fixed amount
    min_order_amount    NUMERIC(15, 2),
    max_discount_amount NUMERIC(15, 2),
    usage_limit         INTEGER,
    usage_per_user      INTEGER NOT NULL DEFAULT 1,
    used_count          INTEGER NOT NULL DEFAULT 0,
    applies_to          VARCHAR(20) NOT NULL DEFAULT 'ALL',  -- ALL, CATEGORY, PRODUCT, BRAND
    starts_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at             TIMESTAMP WITH TIME ZONE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    is_stackable        BOOLEAN NOT NULL DEFAULT FALSE,
    created_by          UUID REFERENCES users(id),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Promotion applicable items
CREATE TABLE promotion_items (
    promotion_id    UUID NOT NULL REFERENCES promotions(id) ON DELETE CASCADE,
    item_id         UUID NOT NULL,              -- category_id or product_id or brand_id
    item_type       VARCHAR(20) NOT NULL,       -- CATEGORY, PRODUCT, BRAND
    PRIMARY KEY (promotion_id, item_id)
);

-- Promotion usage tracking
CREATE TABLE promotion_usages (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    promotion_id    UUID NOT NULL REFERENCES promotions(id),
    user_id         UUID NOT NULL REFERENCES users(id),
    order_id        UUID,
    discount_amount NUMERIC(15, 2) NOT NULL,
    used_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (promotion_id, user_id, order_id)
);

-- ============================================================
-- SHOPPING CART
-- ============================================================

CREATE TABLE carts (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    session_id      VARCHAR(255) UNIQUE,        -- for guest checkout
    expires_at      TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE cart_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id         UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    variant_id      UUID NOT NULL REFERENCES product_variants(id),
    quantity        INTEGER NOT NULL DEFAULT 1,
    unit_price      NUMERIC(15, 2) NOT NULL,    -- Price at time of adding
    added_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (cart_id, variant_id),
    CONSTRAINT chk_cart_qty CHECK (quantity > 0)
);

-- ============================================================
-- ORDERS
-- ============================================================

CREATE TABLE orders (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number        VARCHAR(30) NOT NULL UNIQUE,    -- DLT-2024-000001
    user_id             UUID REFERENCES users(id) ON DELETE SET NULL,
    guest_email         VARCHAR(255),                    -- for guest orders
    status              order_status NOT NULL DEFAULT 'PENDING',
    payment_status      payment_status NOT NULL DEFAULT 'PENDING',
    payment_method      payment_method NOT NULL DEFAULT 'COD',

    -- Shipping address snapshot
    shipping_name       VARCHAR(200) NOT NULL,
    shipping_phone      VARCHAR(20) NOT NULL,
    shipping_province   VARCHAR(100) NOT NULL,
    shipping_district   VARCHAR(100) NOT NULL,
    shipping_ward       VARCHAR(100) NOT NULL,
    shipping_address    VARCHAR(500) NOT NULL,

    -- Pricing
    subtotal            NUMERIC(15, 2) NOT NULL,
    discount_amount     NUMERIC(15, 2) NOT NULL DEFAULT 0,
    shipping_fee        NUMERIC(15, 2) NOT NULL DEFAULT 0,
    tax_amount          NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_amount        NUMERIC(15, 2) NOT NULL,
    loyalty_points_used INTEGER NOT NULL DEFAULT 0,
    loyalty_points_earned INTEGER NOT NULL DEFAULT 0,

    -- Promotion
    promotion_id        UUID REFERENCES promotions(id),
    promotion_code      VARCHAR(50),

    -- Tracking
    notes               TEXT,
    admin_notes         TEXT,
    tracking_number     VARCHAR(100),
    estimated_delivery  DATE,
    shipped_at          TIMESTAMP WITH TIME ZONE,
    delivered_at        TIMESTAMP WITH TIME ZONE,
    cancelled_at        TIMESTAMP WITH TIME ZONE,
    cancel_reason       TEXT,

    -- Audit
    ip_address          INET,
    user_agent          VARCHAR(500),
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id        UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    variant_id      UUID REFERENCES product_variants(id) ON DELETE SET NULL,
    product_id      UUID REFERENCES products(id) ON DELETE SET NULL,

    -- Snapshot at time of order
    product_name    VARCHAR(500) NOT NULL,
    variant_name    VARCHAR(300),
    product_sku     VARCHAR(150),
    product_image   VARCHAR(500),

    quantity        INTEGER NOT NULL,
    unit_price      NUMERIC(15, 2) NOT NULL,
    discount_amount NUMERIC(15, 2) NOT NULL DEFAULT 0,
    total_price     NUMERIC(15, 2) NOT NULL,

    is_reviewed     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Order status history
CREATE TABLE order_status_history (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id    UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    from_status order_status,
    to_status   order_status NOT NULL,
    note        TEXT,
    created_by  UUID REFERENCES users(id),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Update FK
ALTER TABLE stock_movements ADD CONSTRAINT fk_stock_order
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL;
ALTER TABLE promotion_usages ADD CONSTRAINT fk_promo_order
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL;

-- ============================================================
-- PAYMENTS
-- ============================================================

CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id            UUID NOT NULL REFERENCES orders(id),
    gateway             VARCHAR(50) NOT NULL,       -- VNPAY, MOMO, etc.
    gateway_txn_id      VARCHAR(255),               -- External transaction ID
    gateway_ref_id      VARCHAR(255),
    amount              NUMERIC(15, 2) NOT NULL,
    currency            CHAR(3) NOT NULL DEFAULT 'VND',
    status              payment_status NOT NULL DEFAULT 'PENDING',
    gateway_response    JSONB,                      -- Raw gateway response
    paid_at             TIMESTAMP WITH TIME ZONE,
    failed_at           TIMESTAMP WITH TIME ZONE,
    failure_reason      TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- REVIEWS & RATINGS
-- ============================================================

CREATE TABLE reviews (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id      UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_item_id   UUID REFERENCES order_items(id) ON DELETE SET NULL,
    rating          SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title           VARCHAR(300),
    body            TEXT,
    status          review_status NOT NULL DEFAULT 'PENDING',
    is_verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    helpful_count   INTEGER NOT NULL DEFAULT 0,
    unhelpful_count INTEGER NOT NULL DEFAULT 0,
    admin_reply     TEXT,
    admin_reply_at  TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, order_item_id)
);

CREATE TABLE review_images (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    review_id   UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    url         VARCHAR(500) NOT NULL,
    public_id   VARCHAR(255),
    sort_order  INTEGER NOT NULL DEFAULT 0
);

-- Review helpful votes
CREATE TABLE review_votes (
    review_id   UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_helpful  BOOLEAN NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (review_id, user_id)
);

-- ============================================================
-- WISHLIST
-- ============================================================

CREATE TABLE wishlists (
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id  UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    added_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, product_id)
);

-- ============================================================
-- SHIPPING
-- ============================================================

CREATE TABLE shipping_zones (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(200) NOT NULL,
    provinces   VARCHAR(100)[],
    is_active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE shipping_methods (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    zone_id             UUID NOT NULL REFERENCES shipping_zones(id),
    name                VARCHAR(200) NOT NULL,
    carrier             VARCHAR(100),
    base_fee            NUMERIC(10, 2) NOT NULL,
    fee_per_kg          NUMERIC(10, 2) NOT NULL DEFAULT 0,
    free_shipping_above NUMERIC(15, 2),         -- Free if order > this amount
    estimated_days_min  SMALLINT NOT NULL DEFAULT 1,
    estimated_days_max  SMALLINT NOT NULL DEFAULT 3,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================================
-- NOTIFICATIONS
-- ============================================================

CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        notification_type NOT NULL,
    title       VARCHAR(300) NOT NULL,
    body        TEXT NOT NULL,
    data        JSONB,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    read_at     TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- LOYALTY POINTS LEDGER
-- ============================================================

CREATE TABLE loyalty_transactions (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_id        UUID REFERENCES orders(id) ON DELETE SET NULL,
    points          INTEGER NOT NULL,           -- positive=earn, negative=spend
    balance_after   INTEGER NOT NULL,
    description     VARCHAR(300) NOT NULL,
    expires_at      TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- SEO & CONTENT
-- ============================================================

CREATE TABLE banners (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title       VARCHAR(300) NOT NULL,
    image_url   VARCHAR(500) NOT NULL,
    mobile_image_url VARCHAR(500),
    link_url    VARCHAR(500),
    position    VARCHAR(50) NOT NULL DEFAULT 'HOME_HERO',
    sort_order  INTEGER NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at   TIMESTAMP WITH TIME ZONE,
    ends_at     TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES - Performance Critical
-- ============================================================

-- Users
CREATE INDEX idx_users_email ON users(email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_phone ON users(phone) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_role ON users(role);

-- Products - full text search
CREATE INDEX idx_products_search_vector ON products USING GIN(search_vector);
CREATE INDEX idx_products_name_trgm ON products USING GIN(name gin_trgm_ops);
CREATE INDEX idx_products_category ON products(category_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_brand ON products(brand_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_status ON products(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_price ON products(base_price) WHERE deleted_at IS NULL;
CREATE INDEX idx_products_featured ON products(is_featured) WHERE deleted_at IS NULL AND status = 'ACTIVE';
CREATE INDEX idx_products_tags ON products USING GIN(tags);
CREATE INDEX idx_products_sports ON products USING GIN(sport_types);

-- Variants
CREATE INDEX idx_variants_product ON product_variants(product_id) WHERE is_active = TRUE;
CREATE INDEX idx_variants_stock ON product_variants(stock_quantity) WHERE is_active = TRUE;

-- Orders
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_orders_created ON orders(created_at DESC);

-- Order items
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- Reviews
CREATE INDEX idx_reviews_product ON reviews(product_id) WHERE status = 'APPROVED';
CREATE INDEX idx_reviews_user ON reviews(user_id);

-- Wishlists
CREATE INDEX idx_wishlists_user ON wishlists(user_id);

-- Notifications
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read, created_at DESC);

-- Categories
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_path ON categories USING GIN(path gin_trgm_ops);

-- Promotions
CREATE INDEX idx_promotions_code ON promotions(code) WHERE is_active = TRUE;
CREATE INDEX idx_promotions_dates ON promotions(starts_at, ends_at) WHERE is_active = TRUE;

-- Cart
CREATE INDEX idx_carts_user ON carts(user_id);
CREATE INDEX idx_carts_session ON carts(session_id);

-- Refresh tokens
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- ============================================================
-- TRIGGERS
-- ============================================================

-- Auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_products_updated_at BEFORE UPDATE ON products FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_variants_updated_at BEFORE UPDATE ON product_variants FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_orders_updated_at BEFORE UPDATE ON orders FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_categories_updated_at BEFORE UPDATE ON categories FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- Update product search vector
CREATE OR REPLACE FUNCTION update_product_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector =
        setweight(to_tsvector('simple', unaccent(coalesce(NEW.name, ''))), 'A') ||
        setweight(to_tsvector('simple', unaccent(coalesce(NEW.short_description, ''))), 'B') ||
        setweight(to_tsvector('simple', unaccent(coalesce(NEW.description, ''))), 'C') ||
        setweight(to_tsvector('simple', unaccent(array_to_string(coalesce(NEW.tags, '{}'), ' '))), 'D');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_product_search_vector
BEFORE INSERT OR UPDATE ON products
FOR EACH ROW EXECUTE FUNCTION update_product_search_vector();

-- Update product rating stats when review changes
CREATE OR REPLACE FUNCTION update_product_rating()
RETURNS TRIGGER AS $$
DECLARE
    v_product_id UUID;
BEGIN
    v_product_id := COALESCE(NEW.product_id, OLD.product_id);
    UPDATE products
    SET average_rating = (
            SELECT COALESCE(AVG(rating), 0)
            FROM reviews
            WHERE product_id = v_product_id AND status = 'APPROVED'
        ),
        review_count = (
            SELECT COUNT(*)
            FROM reviews
            WHERE product_id = v_product_id AND status = 'APPROVED'
        )
    WHERE id = v_product_id;
    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_review_rating
AFTER INSERT OR UPDATE OR DELETE ON reviews
FOR EACH ROW EXECUTE FUNCTION update_product_rating();

-- Prevent negative stock
CREATE OR REPLACE FUNCTION check_variant_stock()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.stock_quantity < 0 THEN
        RAISE EXCEPTION 'Stock quantity cannot be negative for variant %', NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_stock
BEFORE UPDATE ON product_variants
FOR EACH ROW EXECUTE FUNCTION check_variant_stock();

-- ============================================================
-- SEED DATA
-- ============================================================

-- Default attributes
INSERT INTO attributes (name, code, type, is_filterable, sort_order) VALUES
('Kích cỡ', 'SIZE', 'SIZE', TRUE, 1),
('Màu sắc', 'COLOR', 'COLOR', TRUE, 2),
('Chất liệu', 'MATERIAL', 'TEXT', TRUE, 3),
('Trọng lượng', 'WEIGHT', 'NUMBER', TRUE, 4),
('Giới tính', 'GENDER', 'TEXT', TRUE, 5);

-- Root categories
INSERT INTO categories (name, slug, description, sort_order, depth, path) VALUES
('Bóng đá', 'bong-da', 'Dụng cụ và trang thiết bị bóng đá chuyên dụng Delta', 1, 0, ''),
('Bóng rổ', 'bong-ro', 'Dụng cụ và trang thiết bị bóng rổ chuyên dụng Delta', 2, 0, ''),
('Cầu lông', 'cau-long', 'Vợt, cầu và phụ kiện cầu lông chuyên dụng Delta', 3, 0, ''),
('Bơi lội', 'boi-loi', 'Đồ bơi, kính bơi và phụ kiện bơi lội Delta', 4, 0, ''),
('Gym & Fitness', 'gym-fitness', 'Dụng cụ tập gym và thiết bị thể dục Delta', 5, 0, ''),
('Chạy bộ', 'chay-bo', 'Giày và phụ kiện chạy bộ chuyên nghiệp Delta', 6, 0, ''),
('Tennis', 'tennis', 'Vợt, bóng và phụ kiện tennis chuyên dụng Delta', 7, 0, ''),
('Cờ vua & Cờ tướng', 'co-vua', 'Bộ cờ và phụ kiện Delta', 8, 0, '');

-- Default shipping zones
INSERT INTO shipping_zones (name, provinces) VALUES
('Nội thành HCM', ARRAY['Hồ Chí Minh']),
('Nội thành Hà Nội', ARRAY['Hà Nội']),
('Toàn quốc', ARRAY['*']);

-- Admin user (password: Admin@123)
INSERT INTO users (email, password_hash, first_name, last_name, role, status, email_verified)
VALUES ('admin@delta-sports.vn',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj5cz2uXrFSm',
        'Delta', 'Admin', 'SUPER_ADMIN', 'ACTIVE', TRUE);