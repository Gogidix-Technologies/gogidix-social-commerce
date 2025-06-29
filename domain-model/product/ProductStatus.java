package com.microsocial.commerce.domain.model.product;

/**
 * Enum representing the status of a product.
 * Used to track the product lifecycle from draft to active to discontinued.
 */
public enum ProductStatus {
    /**
     * Product is in draft state, not yet published.
     */
    DRAFT,
    
    /**
     * Product is active and available for purchase.
     */
    ACTIVE,
    
    /**
     * Product is out of stock, but will be available again.
     */
    OUT_OF_STOCK,
    
    /**
     * Product is on sale with a discounted price.
     */
    ON_SALE,
    
    /**
     * Product is coming soon, but not yet available.
     */
    COMING_SOON,
    
    /**
     * Product is discontinued and no longer available.
     */
    DISCONTINUED,
    
    /**
     * Product is pending approval from admin.
     */
    PENDING_APPROVAL,
    
    /**
     * Product has been rejected by admin.
     */
    REJECTED,
    
    /**
     * Product is archived but can be restored.
     */
    ARCHIVED;
    
    /**
     * Convert from legacy ProductStatus (socialecommerceecosystem) to consolidated ProductStatus.
     * 
     * @param legacyStatus The legacy status to convert
     * @return The corresponding consolidated status
     */
    public static ProductStatus fromLegacyStatus(com.socialecommerceecosystem.productservice.model.ProductStatus legacyStatus) {
        if (legacyStatus == null) {
            return null;
        }
        
        switch (legacyStatus) {
            case DRAFT:
                return DRAFT;
            case ACTIVE:
                return ACTIVE;
            case OUT_OF_STOCK:
                return OUT_OF_STOCK;
            case ON_SALE:
                return ON_SALE;
            case COMING_SOON:
                return COMING_SOON;
            case DISCONTINUED:
                return DISCONTINUED;
            case PENDING_APPROVAL:
                return PENDING_APPROVAL;
            case REJECTED:
                return REJECTED;
            case ARCHIVED:
                return ARCHIVED;
            default:
                return ACTIVE;
        }
    }
}
