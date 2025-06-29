import React from 'react';

const Analytics = () => {
  return (
    <div className="analytics-page">
      <h1>Analytics</h1>
      
      <div className="date-filter">
        <button className="date-btn">Today</button>
        <button className="date-btn active">This Week</button>
        <button className="date-btn">This Month</button>
        <button className="date-btn">This Year</button>
      </div>

      <div className="analytics-grid">
        <div className="chart-card">
          <h3>Sales Overview</h3>
          <div className="chart-placeholder">
            <p>Sales Chart</p>
            <div className="chart-bars">
              <div className="bar" style={{height: '60%'}}></div>
              <div className="bar" style={{height: '80%'}}></div>
              <div className="bar" style={{height: '45%'}}></div>
              <div className="bar" style={{height: '90%'}}></div>
              <div className="bar" style={{height: '70%'}}></div>
            </div>
          </div>
        </div>

        <div className="chart-card">
          <h3>Top Products</h3>
          <ul className="top-products-list">
            <li>
              <span className="product-name">Product A</span>
              <span className="product-sales">152 sales</span>
            </li>
            <li>
              <span className="product-name">Product B</span>
              <span className="product-sales">98 sales</span>
            </li>
            <li>
              <span className="product-name">Product C</span>
              <span className="product-sales">76 sales</span>
            </li>
          </ul>
        </div>

        <div className="chart-card">
          <h3>Customer Demographics</h3>
          <div className="demographics">
            <div className="demographic-item">
              <span>Age 18-24:</span>
              <span>25%</span>
            </div>
            <div className="demographic-item">
              <span>Age 25-34:</span>
              <span>40%</span>
            </div>
            <div className="demographic-item">
              <span>Age 35-44:</span>
              <span>20%</span>
            </div>
            <div className="demographic-item">
              <span>Age 45+:</span>
              <span>15%</span>
            </div>
          </div>
        </div>

        <div className="chart-card">
          <h3>Revenue Breakdown</h3>
          <div className="revenue-breakdown">
            <div className="revenue-item">
              <span className="category">Electronics</span>
              <span className="amount">$4,520</span>
              <span className="percentage">45%</span>
            </div>
            <div className="revenue-item">
              <span className="category">Clothing</span>
              <span className="amount">$3,200</span>
              <span className="percentage">32%</span>
            </div>
            <div className="revenue-item">
              <span className="category">Home</span>
              <span className="amount">$2,300</span>
              <span className="percentage">23%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Analytics;