
-- Create the listing_reviews table
CREATE TABLE listing_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    listing_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    order_id BIGINT,
    rating INT NOT NULL,
    title VARCHAR(200),
    content VARCHAR(2000),
    is_verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
    has_images BOOLEAN NOT NULL DEFAULT FALSE,
    image_urls VARCHAR(1000),
    helpful_votes INT NOT NULL DEFAULT 0,
    not_helpful_votes INT NOT NULL DEFAULT 0,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    has_seller_reply BOOLEAN NOT NULL DEFAULT FALSE,
    seller_reply VARCHAR(1000),
    seller_reply_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    
    INDEX idx_listing_id (listing_id),
    INDEX idx_user_id (user_id),
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_order_id (order_id),
    INDEX idx_rating (rating),
    INDEX idx_is_approved (is_approved),
    INDEX idx_created_at (created_at),
    
    -- Ensure a user can only review a listing once
    UNIQUE KEY uk_user_listing (user_id, listing_id)
);

-- Create a view for review analytics
CREATE VIEW listing_review_analytics AS
SELECT 
    listing_id,
    COUNT(*) as total_reviews,
    AVG(rating) as average_rating,
    COUNT(CASE WHEN rating = 5 THEN 1 ELSE NULL END) as five_star_count,
    COUNT(CASE WHEN rating = 4 THEN 1 ELSE NULL END) as four_star_count,
    COUNT(CASE WHEN rating = 3 THEN 1 ELSE NULL END) as three_star_count,
    COUNT(CASE WHEN rating = 2 THEN 1 ELSE NULL END) as two_star_count,
    COUNT(CASE WHEN rating = 1 THEN 1 ELSE NULL END) as one_star_count,
    COUNT(CASE WHEN is_verified_purchase = TRUE THEN 1 ELSE NULL END) as verified_purchase_count,
    COUNT(CASE WHEN has_images = TRUE THEN 1 ELSE NULL END) as reviews_with_images_count,
    COUNT(CASE WHEN has_seller_reply = TRUE THEN 1 ELSE NULL END) as reviews_with_seller_reply_count,
    SUM(helpful_votes) as total_helpful_votes,
    SUM(not_helpful_votes) as total_not_helpful_votes
FROM 
    listing_reviews
WHERE 
    is_approved = TRUE
GROUP BY 
    listing_id;

-- Create a view for seller review analytics
CREATE VIEW seller_review_analytics AS
SELECT 
    seller_id,
    COUNT(*) as total_reviews,
    AVG(rating) as average_rating,
    COUNT(CASE WHEN rating >= 4 THEN 1 ELSE NULL END) as positive_reviews,
    COUNT(CASE WHEN rating <= 2 THEN 1 ELSE NULL END) as negative_reviews,
    COUNT(CASE WHEN has_seller_reply = TRUE THEN 1 ELSE NULL END) as replied_reviews_count,
    AVG(CASE WHEN created_at > DATE_SUB(NOW(), INTERVAL 30 DAY) THEN rating ELSE NULL END) as last_30_days_rating
FROM 
    listing_reviews
WHERE 
    is_approved = TRUE
GROUP BY 
    seller_id;
