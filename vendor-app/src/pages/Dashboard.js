import React from 'react';

const Dashboard = () => {
  return (
    <div className="dashboard">
      <h1>Dashboard</h1>
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Total Products</h3>
          <p className="stat-number">156</p>
          <span className="stat-change positive">+12% from last month</span>
        </div>
        <div className="stat-card">
          <h3>Active Orders</h3>
          <p className="stat-number">43</p>
          <span className="stat-change positive">+8% from last month</span>
        </div>
        <div className="stat-card">
          <h3>Revenue</h3>
          <p className="stat-number">$12,450</p>
          <span className="stat-change positive">+23% from last month</span>
        </div>
        <div className="stat-card">
          <h3>Customer Rating</h3>
          <p className="stat-number">4.7/5</p>
          <span className="stat-change">No change</span>
        </div>
      </div>
      
      <div className="recent-section">
        <h2>Recent Orders</h2>
        <table className="orders-table">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Customer</th>
              <th>Products</th>
              <th>Total</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>#ORD-001</td>
              <td>John Doe</td>
              <td>2 items</td>
              <td>$125.00</td>
              <td><span className="status pending">Pending</span></td>
            </tr>
            <tr>
              <td>#ORD-002</td>
              <td>Jane Smith</td>
              <td>1 item</td>
              <td>$75.50</td>
              <td><span className="status shipped">Shipped</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Dashboard;