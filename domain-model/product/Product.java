package com.microsocial.commerce.domain.model.product;

import com.microsocial.commerce.domain.model.category.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Consolidated entity representing a product in the social commerce system.
 * This class merges functionality from various implementations.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 5000)
    private String description;

    @Column(nullable = false, unique = true)
    private String sku;

    private String slug;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal salePrice;

    private Integer quantity;

    @Column(nullable = false)
    private Boolean featured = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    // Many-to-many relationship with categories
    @ManyToMany
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    // One-to-many relationship with product variants
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductVariant> variants = new HashSet<>();

    // One-to-many relationship with product images
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> images = new HashSet<>();

    // One-to-many relationship with product attributes
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductAttribute> attributes = new HashSet<>();

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private String vendorName;

    private Double averageRating;

    private Integer reviewCount;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Custom methods for relationship management

    /**
     * Adds a category to this product.
     * 
     * @param category the category to add
     * @return this product for method chaining
     */
    public Product addCategory(Category category) {
        categories.add(category);
        category.getProducts().add(this);
        return this;
    }

    /**
     * Removes a category from this product.
     * 
     * @param category the category to remove
     * @return this product for method chaining
     */
    public Product removeCategory(Category category) {
        categories.remove(category);
        category.getProducts().remove(this);
        return this;
    }

    /**
     * Adds a variant to this product.
     * 
     * @param variant the variant to add
     * @return this product for method chaining
     */
    public Product addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
        return this;
    }

    /**
     * Removes a variant from this product.
     * 
     * @param variant the variant to remove
     * @return this product for method chaining
     */
    public Product removeVariant(ProductVariant variant) {
        variants.remove(variant);
        variant.setProduct(null);
        return this;
    }

    /**
     * Adds an image to this product.
     * 
     * @param image the image to add
     * @return this product for method chaining
     */
    public Product addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
        return this;
    }

    /**
     * Removes an image from this product.
     * 
     * @param image the image to remove
     * @return this product for method chaining
     */
    public Product removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
        return this;
    }

    /**
     * Adds an attribute to this product.
     * 
     * @param attribute the attribute to add
     * @return this product for method chaining
     */
    public Product addAttribute(ProductAttribute attribute) {
        attributes.add(attribute);
        attribute.setProduct(this);
        return this;
    }

    /**
     * Removes an attribute from this product.
     * 
     * @param attribute the attribute to remove
     * @return this product for method chaining
     */
    public Product removeAttribute(ProductAttribute attribute) {
        attributes.remove(attribute);
        attribute.setProduct(null);
        return this;
    }

    /**
     * Checks if this product is on sale.
     * 
     * @return true if the product is on sale, false otherwise
     */
    public boolean isOnSale() {
        return salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0 
               && salePrice.compareTo(price) < 0;
    }

    /**
     * Gets the current price of the product (sale price if on sale, regular price otherwise).
     * 
     * @return the current price
     */
    public BigDecimal getCurrentPrice() {
        return isOnSale() ? salePrice : price;
    }

    /**
     * Calculates the discount percentage if the product is on sale.
     * 
     * @return the discount percentage or zero if not on sale
     */
    public BigDecimal getDiscountPercentage() {
        if (!isOnSale()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount = price.subtract(salePrice);
        return discount.multiply(new BigDecimal("100")).divide(price, 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Create a consolidated Product from the legacy microsocialecommerce Product.
     * 
     * @param legacyProduct the legacy product to convert
     * @return the consolidated Product
     */
    public static Product fromLegacyMicrosocialecommerceProduct(com.microsocialecommerce.product.model.Product legacyProduct) {
        if (legacyProduct == null) {
            return null;
        }
        
        return Product.builder()
                .name(legacyProduct.getName())
                .description(legacyProduct.getDescription())
                .price(legacyProduct.getPrice())
                .quantity(legacyProduct.getStockQuantity())
                .featured(legacyProduct.getFeatured())
                .vendorId(Long.parseLong(legacyProduct.getSellerId()))
                .vendorName("Unknown") // Not available in legacy model
                .sku(generateSku(legacyProduct.getName())) // Generate SKU from name
                .status(ProductStatus.ACTIVE)
                .active(true)
                .build();
    }
    
    /**
     * Create a consolidated Product from the legacy socialecommerceecosystem Product.
     * 
     * @param legacyProduct the legacy product to convert
     * @return the consolidated Product
     */
    public static Product fromLegacySocialecommerceecosystemProduct(com.socialecommerceecosystem.productservice.model.Product legacyProduct) {
        if (legacyProduct == null) {
            return null;
        }
        
        ProductStatus status = ProductStatus.fromLegacyStatus(legacyProduct.getStatus());
        
        Product product = Product.builder()
                .name(legacyProduct.getName())
                .description(legacyProduct.getDescription())
                .price(legacyProduct.getPrice())
                .salePrice(legacyProduct.getSalePrice())
                .sku(legacyProduct.getSku())
                .slug(legacyProduct.getSlug())
                .quantity(legacyProduct.getQuantity())
                .featured(legacyProduct.getFeatured())
                .active(legacyProduct.getActive())
                .status(status)
                .vendorId(legacyProduct.getVendorId())
                .vendorName(legacyProduct.getVendorName())
                .averageRating(legacyProduct.getAverageRating())
                .reviewCount(legacyProduct.getReviewCount())
                .createdAt(legacyProduct.getCreatedAt())
                .updatedAt(legacyProduct.getUpdatedAt())
                .build();
        
        // Note: Categories, variants, images, and attributes would need to be handled separately
        
        return product;
    }
    
    /**
     * Generate a SKU from a product name.
     * This is a placeholder implementation and would need to be enhanced in a real system.
     * 
     * @param name the product name
     * @return a generated SKU
     */
    private static String generateSku(String name) {
        if (name == null || name.isEmpty()) {
            return "PROD-" + System.currentTimeMillis();
        }
        
        String cleanName = name.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (cleanName.length() > 10) {
            cleanName = cleanName.substring(0, 10);
        }
        
        return cleanName + "-" + System.currentTimeMillis() % 10000;
    }
}
