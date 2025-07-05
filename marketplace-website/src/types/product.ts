export interface Product {
  id: string;
  name: string;
  slug: string;
  description: string;
  shortDescription?: string;
  price: number;
  salePrice?: number;
  currency: string;
  sku: string;
  barcode?: string;
  quantity: number;
  images: ProductImage[];
  category: Category;
  subcategory?: Category;
  brand?: Brand;
  vendor: Vendor;
  attributes: ProductAttribute[];
  variants?: ProductVariant[];
  tags: string[];
  rating: number;
  reviewCount: number;
  sold: number;
  featured: boolean;
  status: ProductStatus;
  weight?: number;
  dimensions?: ProductDimensions;
  createdAt: string;
  updatedAt: string;
}

export interface ProductImage {
  id: string;
  url: string;
  alt: string;
  isPrimary: boolean;
  order: number;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  image?: string;
  parentId?: string;
  level: number;
  order: number;
}

export interface Brand {
  id: string;
  name: string;
  slug: string;
  logo?: string;
  description?: string;
}

export interface Vendor {
  id: string;
  name: string;
  slug: string;
  logo?: string;
  banner?: string;
  description?: string;
  rating: number;
  reviewCount: number;
  productCount: number;
  verified: boolean;
  joinedAt: string;
}

export interface ProductAttribute {
  name: string;
  value: string;
  type: AttributeType;
}

export interface ProductVariant {
  id: string;
  name: string;
  sku: string;
  price: number;
  quantity: number;
  attributes: ProductAttribute[];
  image?: string;
}

export interface ProductDimensions {
  length: number;
  width: number;
  height: number;
  unit: 'cm' | 'inch';
}

export enum ProductStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  DISCONTINUED = 'DISCONTINUED',
}

export enum AttributeType {
  COLOR = 'COLOR',
  SIZE = 'SIZE',
  MATERIAL = 'MATERIAL',
  CUSTOM = 'CUSTOM',
}

export interface ProductFilter {
  categories?: string[];
  minPrice?: number;
  maxPrice?: number;
  brands?: string[];
  vendors?: string[];
  rating?: number;
  attributes?: Record<string, string[]>;
  inStock?: boolean;
  featured?: boolean;
  sortBy?: ProductSortOption;
}

export enum ProductSortOption {
  RELEVANCE = 'relevance',
  PRICE_LOW_TO_HIGH = 'price_asc',
  PRICE_HIGH_TO_LOW = 'price_desc',
  RATING = 'rating',
  NEWEST = 'newest',
  BEST_SELLING = 'best_selling',
}