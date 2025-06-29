# Marketplace Service API Documentation

## Core API

### MarketplaceApplication
- MarketplaceApplication(): Default constructor
- MarketplaceApplication(String name, String description): Constructor with name and description
- UUID getId(): Get the application ID
- String getName(): Get the application name
- void setName(String name): Set the application name
- String getDescription(): Get the application description
- void setDescription(String description): Set the application description
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp

### SecurityConfig
- SecurityConfig(): Default constructor with secure marketplace defaults
- List<String> getAllowedOrigins(): Get the allowed origins for CORS
- void setAllowedOrigins(List<String> allowedOrigins): Set the allowed origins
- boolean isCsrfEnabled(): Check if CSRF protection is enabled
- void setCsrfEnabled(boolean csrfEnabled): Enable or disable CSRF protection
- String getTokenExpirationSeconds(): Get the token expiration time in seconds
- void setTokenExpirationSeconds(String tokenExpirationSeconds): Set the token expiration time
- String getJwtSecret(): Get the JWT secret for customer authentication
- void setJwtSecret(String jwtSecret): Set the JWT secret

## Product Discovery API

### ProductDiscoveryComponent
- ProductDiscoveryComponent(): Default constructor
- ProductDiscoveryComponent(String name, String description): Constructor with name and description
- void addProductCategory(ProductCategory category): Add a product category
- boolean removeProductCategory(UUID categoryId): Remove a product category
- List<ProductCategory> getProductCategories(): Get all product categories
- List<Product> getFeaturedProducts(): Get featured products
- List<Product> getRecommendedProducts(UUID customerId): Get personalized recommendations

### Product
- Product(): Default constructor
- Product(String name, String description, BigDecimal price, UUID vendorId): Constructor with basic details
- UUID getId(): Get the product ID
- String getName(): Get the product name
- void setName(String name): Set the product name
- String getDescription(): Get the product description
- void setDescription(String description): Set the product description
- BigDecimal getPrice(): Get the product price
- void setPrice(BigDecimal price): Set the product price
- UUID getVendorId(): Get the vendor ID
- void setVendorId(UUID vendorId): Set the vendor ID
- ProductStatus getStatus(): Get the product status
- void setStatus(ProductStatus status): Set the product status
- List<String> getImages(): Get product images
- void addImage(String imageUrl): Add a product image
- LocalDateTime getCreatedAt(): Get the creation timestamp
- LocalDateTime getUpdatedAt(): Get the last update timestamp

### ProductCategory
- ProductCategory(): Default constructor
- ProductCategory(String name, String description): Constructor with name and description
- UUID getId(): Get the category ID
- String getName(): Get the category name
- void setName(String name): Set the category name
- String getDescription(): Get the category description
- void setDescription(String description): Set the category description
- ProductCategory getParentCategory(): Get the parent category
- void setParentCategory(ProductCategory parentCategory): Set the parent category
- List<ProductCategory> getSubCategories(): Get subcategories
- boolean isActive(): Check if the category is active
- void setActive(boolean active): Set the category as active or inactive

## Search Engine API

### SearchEngine
- SearchEngine(): Default constructor
- SearchEngine(String name, String description): Constructor with name and description
- SearchResult<Product> searchProducts(String query): Search products by query
- SearchResult<Product> searchProducts(String query, SearchFilters filters): Search with filters
- SearchResult<Product> searchProducts(String query, SearchFilters filters, SortOptions sort): Search with filters and sorting
- List<String> getSuggestedQueries(String partial): Get search suggestions
- void indexProduct(Product product): Index a product for search
- void removeFromIndex(UUID productId): Remove product from search index

### SearchFilters
- SearchFilters(): Default constructor
- SearchFilters addCategoryFilter(UUID categoryId): Add category filter
- SearchFilters addPriceRangeFilter(BigDecimal minPrice, BigDecimal maxPrice): Add price range filter
- SearchFilters addVendorFilter(UUID vendorId): Add vendor filter
- SearchFilters addRatingFilter(double minRating): Add minimum rating filter
- SearchFilters addAvailabilityFilter(boolean inStock): Add availability filter
- Map<String, Object> getFilters(): Get all applied filters

### SearchResult<T>
- SearchResult(List<T> results, long totalCount): Constructor with results and count
- List<T> getResults(): Get search results
- long getTotalCount(): Get total count of matching items
- int getPageSize(): Get page size
- int getCurrentPage(): Get current page number
- boolean hasMore(): Check if more results are available

## Shopping Cart API

### ShoppingCart
- ShoppingCart(): Default constructor
- ShoppingCart(UUID customerId): Constructor with customer ID
- void addItem(CartItem item): Add item to cart
- void updateItem(UUID productId, int quantity): Update item quantity
- boolean removeItem(UUID productId): Remove item from cart
- List<CartItem> getItems(): Get all cart items
- int getItemCount(): Get total item count
- BigDecimal getTotalPrice(): Get total cart price
- int getVendorCount(): Get number of unique vendors
- void clearCart(): Clear all items from cart

### CartItem
- CartItem(): Default constructor
- CartItem(Product product, int quantity): Constructor with product and quantity
- UUID getId(): Get the cart item ID
- Product getProduct(): Get the product
- void setProduct(Product product): Set the product
- int getQuantity(): Get the quantity
- void setQuantity(int quantity): Set the quantity
- BigDecimal getUnitPrice(): Get the unit price
- BigDecimal getTotalPrice(): Get the total price for this item
- LocalDateTime getAddedAt(): Get the timestamp when item was added

## Checkout Process API

### CheckoutProcess
- CheckoutProcess(): Default constructor
- CheckoutProcess(String name, String description): Constructor with name and description
- Order processCheckout(UUID customerId, PaymentInfo paymentInfo, ShippingAddress address): Process checkout
- OrderValidation validateCheckout(UUID customerId): Validate checkout prerequisites
- PaymentOptions getPaymentOptions(): Get available payment options
- ShippingOptions getShippingOptions(ShippingAddress address): Get shipping options

### Order
- Order(): Default constructor
- Order(UUID customerId, List<OrderItem> items): Constructor with customer and items
- UUID getId(): Get the order ID
- UUID getCustomerId(): Get the customer ID
- List<OrderItem> getItems(): Get order items
- OrderStatus getStatus(): Get the order status
- void setStatus(OrderStatus status): Set the order status
- BigDecimal getTotalAmount(): Get the total order amount
- ShippingAddress getShippingAddress(): Get the shipping address
- void setShippingAddress(ShippingAddress address): Set the shipping address
- PaymentInfo getPaymentInfo(): Get the payment information
- String getTrackingNumber(): Get the tracking number
- LocalDateTime getOrderDate(): Get the order date
- LocalDateTime getEstimatedDelivery(): Get estimated delivery date

### PaymentInfo
- PaymentInfo(): Default constructor
- PaymentInfo(PaymentMethod method, String token): Constructor with payment method and token
- PaymentMethod getPaymentMethod(): Get the payment method
- void setPaymentMethod(PaymentMethod method): Set the payment method
- String getPaymentToken(): Get the payment token
- void setPaymentToken(String token): Set the payment token
- BillingAddress getBillingAddress(): Get the billing address
- void setBillingAddress(BillingAddress address): Set the billing address
- boolean isValid(): Check if payment info is valid

## Data Access API

### ProductRepository
- Product save(Product product): Save a product
- Optional<Product> findById(UUID id): Find product by ID
- List<Product> findAll(): Find all products
- List<Product> findByCategory(UUID categoryId): Find products by category
- List<Product> findByVendor(UUID vendorId): Find products by vendor
- List<Product> findFeaturedProducts(): Find featured products
- Page<Product> searchProducts(String query, SearchFilters filters, Pageable pageable): Search products
- void delete(Product product): Delete a product
- boolean deleteById(UUID id): Delete product by ID

### OrderRepository
- Order save(Order order): Save an order
- Optional<Order> findById(UUID id): Find order by ID
- List<Order> findByCustomerId(UUID customerId): Find orders by customer
- List<Order> findByStatus(OrderStatus status): Find orders by status
- List<Order> findByDateRange(LocalDateTime start, LocalDateTime end): Find orders by date range
- void delete(Order order): Delete an order

### CartRepository
- Cart save(Cart cart): Save a cart
- Optional<Cart> findByCustomerId(UUID customerId): Find cart by customer ID
- void clearCart(UUID customerId): Clear customer's cart
- void delete(Cart cart): Delete a cart

## REST API Endpoints

### Product Endpoints
- GET /api/products: Get all products with pagination
- GET /api/products/{id}: Get product by ID
- GET /api/products/category/{categoryId}: Get products by category
- GET /api/products/vendor/{vendorId}: Get products by vendor
- GET /api/products/featured: Get featured products
- GET /api/products/search: Search products with query and filters
- POST /api/products: Create new product (vendor only)
- PUT /api/products/{id}: Update product (vendor only)
- DELETE /api/products/{id}: Delete product (vendor only)

### Cart Endpoints
- GET /api/cart: Get customer's cart
- POST /api/cart/items: Add item to cart
- PUT /api/cart/items/{productId}: Update cart item quantity
- DELETE /api/cart/items/{productId}: Remove item from cart
- DELETE /api/cart: Clear entire cart
- GET /api/cart/summary: Get cart summary

### Order Endpoints
- POST /api/orders/checkout: Process checkout and create order
- GET /api/orders: Get customer's orders
- GET /api/orders/{id}: Get order by ID
- GET /api/orders/{id}/status: Get order status
- PUT /api/orders/{id}/cancel: Cancel order (if allowed)

### Category Endpoints
- GET /api/categories: Get all categories
- GET /api/categories/{id}: Get category by ID
- GET /api/categories/{id}/products: Get products in category
- GET /api/categories/tree: Get category hierarchy

## Integration API

### VendorIntegration
- List<Product> syncVendorProducts(UUID vendorId): Sync products from vendor
- void notifyVendorOfOrder(UUID vendorId, Order order): Notify vendor of new order
- VendorPerformance getVendorMetrics(UUID vendorId): Get vendor performance metrics

### PaymentIntegration
- PaymentResult processPayment(Order order, PaymentInfo paymentInfo): Process payment
- RefundResult processRefund(UUID orderId, BigDecimal amount): Process refund
- List<PaymentMethod> getAvailablePaymentMethods(): Get available payment methods

### InventoryIntegration
- boolean checkAvailability(UUID productId, int quantity): Check product availability
- void reserveInventory(List<OrderItem> items): Reserve inventory for order
- void releaseInventory(List<OrderItem> items): Release reserved inventory