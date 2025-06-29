
-- Create the marketplace_transactions table
CREATE TABLE marketplace_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_ref VARCHAR(50) NOT NULL UNIQUE,
    amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    seller_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    gateway_transaction_id VARCHAR(255),
    payment_gateway VARCHAR(50),
    commission_amount DECIMAL(19, 4),
    
    INDEX idx_transaction_ref (transaction_ref),
    INDEX idx_seller_id (seller_id),
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_listing_id (listing_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Create a view for transaction analytics
CREATE VIEW marketplace_transaction_analytics AS
SELECT 
    DATE(created_at) as transaction_date,
    payment_method,
    currency,
    status,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    SUM(commission_amount) as total_commission,
    AVG(amount) as average_amount
FROM 
    marketplace_transactions
GROUP BY 
    DATE(created_at),
    payment_method,
    currency,
    status;
