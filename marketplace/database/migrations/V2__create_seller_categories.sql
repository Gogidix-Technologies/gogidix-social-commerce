
-- Create the seller_categories table
CREATE TABLE seller_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(1000),
    default_commission_rate DECIMAL(5, 2) NOT NULL,
    minimum_approval_rating INT,
    minimum_monthly_sales DECIMAL(19, 4),
    allows_featured_listings BOOLEAN,
    has_priority_support BOOLEAN,
    is_default BOOLEAN,
    display_order INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    INDEX idx_name (name),
    INDEX idx_display_order (display_order),
    INDEX idx_is_default (is_default)
);

-- Alter the sellers table to add the category relation
ALTER TABLE sellers ADD COLUMN category_id BIGINT;
ALTER TABLE sellers ADD CONSTRAINT fk_seller_category FOREIGN KEY (category_id) REFERENCES seller_categories(id);
ALTER TABLE sellers ADD INDEX idx_category_id (category_id);

-- Insert default seller categories
INSERT INTO seller_categories (
    name, 
    description, 
    default_commission_rate, 
    minimum_approval_rating, 
    minimum_monthly_sales, 
    allows_featured_listings, 
    has_priority_support, 
    is_default, 
    display_order, 
    created_at
) VALUES 
(
    'Standard', 
    'Default category for new sellers', 
    0.10, -- 10% commission
    NULL, 
    NULL, 
    FALSE, 
    FALSE, 
    TRUE, -- This is the default category
    100, 
    NOW()
),
(
    'Silver', 
    'Intermediate sellers with good performance', 
    0.08, -- 8% commission
    85, -- Minimum 85% approval rating
    1000.00, -- Minimum $1000 monthly sales
    FALSE, 
    FALSE, 
    FALSE, 
    80, 
    NOW()
),
(
    'Gold', 
    'Established sellers with excellent performance', 
    0.06, -- 6% commission
    90, -- Minimum 90% approval rating
    5000.00, -- Minimum $5000 monthly sales
    TRUE, -- Can have featured listings
    FALSE, 
    FALSE, 
    60, 
    NOW()
),
(
    'Platinum', 
    'Top sellers with exceptional performance', 
    0.05, -- 5% commission
    95, -- Minimum 95% approval rating
    10000.00, -- Minimum $10000 monthly sales
    TRUE, -- Can have featured listings
    TRUE, -- Has priority support
    FALSE, 
    40, 
    NOW()
),
(
    'Enterprise', 
    'Business sellers with custom arrangements', 
    0.04, -- 4% commission
    95, -- Minimum 95% approval rating
    50000.00, -- Minimum $50000 monthly sales
    TRUE, -- Can have featured listings
    TRUE, -- Has priority support
    FALSE, 
    20, 
    NOW()
);

-- Create a view for category performance analytics
CREATE VIEW seller_category_analytics AS
SELECT 
    sc.id as category_id,
    sc.name as category_name,
    COUNT(s.id) as seller_count,
    AVG(s.approval_rating) as avg_approval_rating,
    SUM(s.lifetime_sales) as total_sales,
    AVG(s.lifetime_sales) as avg_lifetime_sales
FROM 
    seller_categories sc
LEFT JOIN 
    sellers s ON sc.id = s.category_id
GROUP BY 
    sc.id, sc.name;
