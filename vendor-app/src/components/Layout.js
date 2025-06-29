import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const Layout = ({ children }) => {
  const location = useLocation();
  
  const isActive = (path) => location.pathname === path;
  
  return (
    <div className="app-layout">
      <nav className="sidebar">
        <div className="logo">
          <h2>Vendor Portal</h2>
        </div>
        <ul className="nav-menu">
          <li className={isActive('/') ? 'active' : ''}>
            <Link to="/">Dashboard</Link>
          </li>
          <li className={isActive('/products') ? 'active' : ''}>
            <Link to="/products">Products</Link>
          </li>
          <li className={isActive('/orders') ? 'active' : ''}>
            <Link to="/orders">Orders</Link>
          </li>
          <li className={isActive('/analytics') ? 'active' : ''}>
            <Link to="/analytics">Analytics</Link>
          </li>
          <li className={isActive('/profile') ? 'active' : ''}>
            <Link to="/profile">Profile</Link>
          </li>
        </ul>
      </nav>
      <main className="main-content">
        <header className="top-bar">
          <div className="search-bar">
            <input type="search" placeholder="Search..." />
          </div>
          <div className="user-info">
            <span>Welcome, Vendor</span>
            <button className="logout-btn">Logout</button>
          </div>
        </header>
        <div className="content">
          {children}
        </div>
      </main>
    </div>
  );
};

export default Layout;