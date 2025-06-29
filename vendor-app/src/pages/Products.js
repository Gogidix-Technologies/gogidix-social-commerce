import React, { useState } from 'react';

const Products = () => {
  const [products] = useState([
    { id: 1, name: 'Product A', category: 'Electronics', price: 99.99, stock: 50, status: 'Active' },
    { id: 2, name: 'Product B', category: 'Clothing', price: 49.99, stock: 100, status: 'Active' },
    { id: 3, name: 'Product C', category: 'Home', price: 29.99, stock: 0, status: 'Out of Stock' },
  ]);

  return (
    <div className="products-page">
      <div className="page-header">
        <h1>Products</h1>
        <button className="btn-primary">Add New Product</button>
      </div>
      
      <div className="filters">
        <input type="search" placeholder="Search products..." className="search-input" />
        <select className="filter-select">
          <option>All Categories</option>
          <option>Electronics</option>
          <option>Clothing</option>
          <option>Home</option>
        </select>
        <select className="filter-select">
          <option>All Status</option>
          <option>Active</option>
          <option>Out of Stock</option>
          <option>Discontinued</option>
        </select>
      </div>

      <div className="products-grid">
        {products.map(product => (
          <div key={product.id} className="product-card">
            <div className="product-image">
              <img src={`https://via.placeholder.com/200?text=${product.name}`} alt={product.name} />
            </div>
            <div className="product-info">
              <h3>{product.name}</h3>
              <p className="category">{product.category}</p>
              <p className="price">${product.price}</p>
              <p className="stock">Stock: {product.stock}</p>
              <span className={`status ${product.status.toLowerCase().replace(' ', '-')}`}>
                {product.status}
              </span>
            </div>
            <div className="product-actions">
              <button className="btn-secondary">Edit</button>
              <button className="btn-danger">Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Products;