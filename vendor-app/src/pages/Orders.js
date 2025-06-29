import React from 'react';

const Orders = () => {
  const orders = [
    {
      id: 'ORD-001',
      customer: 'John Doe',
      date: '2025-01-06',
      items: 2,
      total: 125.00,
      status: 'Pending',
      paymentStatus: 'Paid'
    },
    {
      id: 'ORD-002',
      customer: 'Jane Smith',
      date: '2025-01-06',
      items: 1,
      total: 75.50,
      status: 'Shipped',
      paymentStatus: 'Paid'
    },
    {
      id: 'ORD-003',
      customer: 'Bob Johnson',
      date: '2025-01-05',
      items: 3,
      total: 220.00,
      status: 'Delivered',
      paymentStatus: 'Paid'
    }
  ];

  return (
    <div className="orders-page">
      <h1>Orders</h1>
      
      <div className="orders-stats">
        <div className="stat-mini">
          <span className="label">New Orders</span>
          <span className="value">12</span>
        </div>
        <div className="stat-mini">
          <span className="label">Processing</span>
          <span className="value">8</span>
        </div>
        <div className="stat-mini">
          <span className="label">Shipped</span>
          <span className="value">15</span>
        </div>
        <div className="stat-mini">
          <span className="label">Delivered</span>
          <span className="value">45</span>
        </div>
      </div>

      <div className="orders-table-container">
        <table className="orders-table">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Customer</th>
              <th>Date</th>
              <th>Items</th>
              <th>Total</th>
              <th>Payment</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {orders.map(order => (
              <tr key={order.id}>
                <td>#{order.id}</td>
                <td>{order.customer}</td>
                <td>{order.date}</td>
                <td>{order.items}</td>
                <td>${order.total.toFixed(2)}</td>
                <td>
                  <span className={`payment-status ${order.paymentStatus.toLowerCase()}`}>
                    {order.paymentStatus}
                  </span>
                </td>
                <td>
                  <span className={`order-status ${order.status.toLowerCase()}`}>
                    {order.status}
                  </span>
                </td>
                <td>
                  <button className="btn-small">View</button>
                  <button className="btn-small">Update</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Orders;