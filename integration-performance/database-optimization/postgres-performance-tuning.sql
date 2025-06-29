-- PostgreSQL Performance Optimization for Social Commerce Platform
-- Run these queries as a superuser or database administrator

-- 1. Connection pooling configuration
ALTER SYSTEM SET max_connections = 200;
ALTER SYSTEM SET max_prepared_transactions = 0;

-- 2. Memory configuration
ALTER SYSTEM SET shared_buffers = '2GB';  -- Adjust based on available RAM
ALTER SYSTEM SET effective_cache_size = '6GB';  -- Adjust based on total system RAM
ALTER SYSTEM SET work_mem = '16MB';
ALTER SYSTEM SET maintenance_work_mem = '256MB';

-- 3. Write-ahead logging (WAL) configuration
ALTER SYSTEM SET wal_level = 'replica';
ALTER SYSTEM SET max_wal_size = '2GB';
ALTER SYSTEM SET min_wal_size = '512MB';
ALTER SYSTEM SET checkpoint_timeout = '10min';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;

-- 4. Query planner configuration
ALTER SYSTEM SET random_page_cost = 1.1;  -- For SSD storage
ALTER SYSTEM SET effective_io_concurrency = 200;
ALTER SYSTEM SET default_statistics_target = 100;

-- 5. Create indexes for frequently queried tables
-- Product search optimization
CREATE INDEX idx_products_name_trgm ON products USING gin(name gin_trgm_ops);
CREATE INDEX idx_products_description_trgm ON products USING gin(description gin_trgm_ops);
CREATE INDEX idx_products_vendor_category ON products(vendor_id, category_id, status);
CREATE INDEX idx_products_price_range ON products(price) WHERE status = 'ACTIVE';
CREATE INDEX idx_products_created_at ON products(created_at DESC);

-- Order processing optimization
CREATE INDEX idx_orders_user_status ON orders(user_id, status, created_at DESC);
CREATE INDEX idx_orders_vendor_status ON orders(vendor_id, status, created_at DESC);
CREATE INDEX idx_orders_payment_id ON orders(payment_id);
CREATE INDEX idx_order_items_product ON order_items(product_id, order_id);

-- Payment processing optimization
CREATE INDEX idx_payments_order_status ON payments(order_id, status, created_at DESC);
CREATE INDEX idx_payments_user_method ON payments(user_id, payment_method_id);
CREATE INDEX idx_payments_transaction ON payments(transaction_id);

-- Commission calculation optimization
CREATE INDEX idx_commissions_order_vendor ON commissions(order_id, vendor_id);
CREATE INDEX idx_commissions_influencer ON commissions(influencer_id, calculated_at DESC);
CREATE INDEX idx_commissions_status ON commissions(status, calculated_at);

-- User and authentication optimization
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role_status ON users(role, status);
CREATE INDEX idx_user_sessions_token ON user_sessions(token_hash, expires_at);

-- Analytics optimization
CREATE INDEX idx_analytics_events_timestamp ON analytics_events(event_type, created_at);
CREATE INDEX idx_analytics_events_user ON analytics_events(user_id, created_at DESC);
CREATE INDEX idx_analytics_events_product ON analytics_events(product_id, event_type, created_at);

-- 6. Partitioning for large tables
-- Partition analytics_events by month
CREATE TABLE analytics_events_template (LIKE analytics_events INCLUDING ALL);

DO $$
DECLARE
    start_date DATE;
    end_date DATE;
    table_name TEXT;
BEGIN
    start_date := date_trunc('month', CURRENT_DATE - INTERVAL '6 months');
    
    WHILE start_date <= CURRENT_DATE + INTERVAL '6 months' LOOP
        table_name := 'analytics_events_' || to_char(start_date, 'YYYY_MM');
        end_date := start_date + INTERVAL '1 month';
        
        EXECUTE format('CREATE TABLE %I PARTITION OF analytics_events FOR VALUES FROM (%L) TO (%L)',
                      table_name, start_date, end_date);
        
        EXECUTE format('CREATE INDEX %I ON %I(event_type, created_at)',
                      'idx_' || table_name || '_type_time', table_name);
        
        start_date := end_date;
    END LOOP;
END $$;

-- 7. Materialized views for reporting
CREATE MATERIALIZED VIEW mv_daily_sales AS
SELECT 
    DATE(o.created_at) as sale_date,
    o.vendor_id,
    COUNT(*) as order_count,
    SUM(p.amount) as total_revenue,
    AVG(p.amount) as avg_order_value
FROM orders o
JOIN payments p ON o.payment_id = p.id
WHERE p.status = 'COMPLETED'
GROUP BY DATE(o.created_at), o.vendor_id;

CREATE UNIQUE INDEX idx_mv_daily_sales ON mv_daily_sales(sale_date, vendor_id);

-- Refresh schedule for materialized view
SELECT cron.schedule('refresh_daily_sales', '0 1 * * *', 'REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_sales;');

-- 8. Table-specific optimizations
-- Product variants optimization
CREATE INDEX idx_product_variants_sku ON product_variants(sku);
CREATE INDEX idx_product_variants_stock ON product_variants(product_id, stock_quantity)
WHERE stock_quantity > 0;

-- Inventory tracking optimization
CREATE INDEX idx_inventory_movements_compound ON inventory_movements(product_variant_id, movement_type, created_at DESC);

-- 9. Query optimization hints
-- Enable query plan caching
ALTER SYSTEM SET plan_cache_mode = 'auto';

-- Enable parallel query execution
ALTER SYSTEM SET max_parallel_workers_per_gather = 4;
ALTER SYSTEM SET max_parallel_workers = 8;
ALTER SYSTEM SET parallel_tuple_cost = 0.1;
ALTER SYSTEM SET parallel_setup_cost = 1000.0;

-- 10. Monitoring queries
-- Create a monitoring schema
CREATE SCHEMA IF NOT EXISTS monitoring;

-- View for slow queries
CREATE OR REPLACE VIEW monitoring.slow_queries AS
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
WHERE idx_scan < 50
ORDER BY idx_scan;

-- View for table sizes
CREATE OR REPLACE VIEW monitoring.table_sizes AS
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    pg_total_relation_size(schemaname||'.'||tablename) as size_bytes
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY size_bytes DESC;

-- 11. Vacuum and analyze automation
-- Auto-vacuum configuration
ALTER TABLE products SET (autovacuum_vacuum_scale_factor = 0.1);
ALTER TABLE orders SET (autovacuum_vacuum_scale_factor = 0.05);
ALTER TABLE payments SET (autovacuum_vacuum_scale_factor = 0.05);
ALTER TABLE analytics_events SET (autovacuum_enabled = true, autovacuum_vacuum_scale_factor = 0.2);

-- 12. Connection pooling optimizations
CREATE OR REPLACE FUNCTION manage_idle_connections()
RETURNS void AS $$
BEGIN
    PERFORM pg_terminate_backend(pid)
    FROM pg_stat_activity
    WHERE datname = current_database()
    AND state = 'idle'
    AND state_change < now() - interval '1 hour'
    AND pid <> pg_backend_pid();
END;
$$ LANGUAGE plpgsql;

-- Schedule the function to run every hour
SELECT cron.schedule('terminate_idle_connections', '0 * * * *', 'SELECT manage_idle_connections();');

-- 13. Apply configuration changes
SELECT pg_reload_conf();

-- 14. Create function for reindexing during low-traffic hours
CREATE OR REPLACE FUNCTION reindex_tables()
RETURNS void AS $$
DECLARE
    table_record RECORD;
BEGIN
    FOR table_record IN 
        SELECT schemaname, tablename 
        FROM pg_tables 
        WHERE schemaname = 'public'
        AND tablename IN ('products', 'orders', 'payments', 'users')
    LOOP
        EXECUTE format('REINDEX TABLE %I.%I', table_record.schemaname, table_record.tablename);
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Schedule reindexing for Sunday 3 AM
SELECT cron.schedule('weekly_reindex', '0 3 * * 0', 'SELECT reindex_tables();');

-- 15. Create monitoring dashboard queries
CREATE OR REPLACE VIEW monitoring.performance_dashboard AS
SELECT 
    'Active Connections' as metric,
    COUNT(*) as value
FROM pg_stat_activity
WHERE state = 'active'
UNION ALL
SELECT 
    'Cache Hit Ratio' as metric,
    (sum(heap_blks_hit) / (sum(heap_blks_hit) + sum(heap_blks_read)))::numeric(5,4) * 100 as value
FROM pg_statio_user_tables
UNION ALL
SELECT 
    'Transaction Wraparound' as metric,
    (SELECT age(datfrozenxid) FROM pg_database WHERE datname = current_database()) as value
UNION ALL
SELECT 
    'Longest Transaction' as metric,
    EXTRACT(EPOCH FROM MAX(age(clock_timestamp(), query_start))) as value
FROM pg_stat_activity
WHERE state != 'idle';

-- 16. Add comments for documentation
COMMENT ON INDEX idx_products_name_trgm IS 'Supports fast text search on product names';
COMMENT ON INDEX idx_orders_user_status IS 'Optimizes user order history queries';
COMMENT ON MATERIALIZED VIEW mv_daily_sales IS 'Pre-aggregated daily sales data for reporting';

-- 17. Final performance configuration
-- Enable track_activity_query_size for monitoring
ALTER SYSTEM SET track_activity_query_size = 4096;

-- Set appropriate lock timeout
ALTER SYSTEM SET lock_timeout = '30s';

-- Set statement timeout for web queries
ALTER SYSTEM SET statement_timeout = '60s';

COMMIT;
