package com.microsocial.commerce.domain.model.product;

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

/**
 * Entity representing a product variant.
 * Variants are variations of a product with different attributes like size, color, etc.
 */
@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true)
    private String sku;

    private String name;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal salePrice;

    private Integer quantity;

    private Boolean active;

    @Column(length = 1000)
    private String attributesJson;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Checks if this variant is on sale.
     * 
     * @return true if the variant is on sale, false otherwise
     */
    public boolean isOnSale() {
        return salePrice != null && salePrice.compareTo(BigDecimal.ZERO) > 0 
               && salePrice.compareTo(price) < 0;
    }

    /**
     * Gets the current price of the variant (sale price if on sale, regular price otherwise).
     * 
     * @return the current price
     */
    public BigDecimal getCurrentPrice() {
        return isOnSale() ? salePrice : price;
    }

    /**
     * Checks if this variant is in stock.
     * 
     * @return true if the variant is in stock, false otherwise
     */
    public boolean isInStock() {
        return quantity != null && quantity > 0;
    }
    
    /**
     * Create a consolidated ProductVariant from the legacy socialecommerceecosystem ProductVariant.
     * 
     * @param legacyVariant the legacy variant to convert
     * @return the consolidated ProductVariant
     */
    public static ProductVariant fromLegacyVariant(com.socialecommerceecosystem.productservice.model.ProductVariant legacyVariant) {
        if (legacyVariant == null) {
            return null;
        }
        
        // Note: The Product reference must be handled separately
        return ProductVariant.builder()
                .sku(legacyVariant.getSku())
                .name(legacyVariant.getName())
                .price(legacyVariant.getPrice())
                .salePrice(legacyVariant.getSalePrice())
                .quantity(legacyVariant.getQuantity())
                .active(legacyVariant.getActive())
                .attributesJson(legacyVariant.getAttributesJson())
                .createdAt(legacyVariant.getCreatedAt())
                .updatedAt(legacyVariant.getUpdatedAt())
                .build();
    }
}
