import React from 'react';

const Profile = () => {
  return (
    <div className="profile-page">
      <h1>Vendor Profile</h1>
      
      <div className="profile-sections">
        <div className="profile-card">
          <h2>Business Information</h2>
          <form className="profile-form">
            <div className="form-group">
              <label>Business Name</label>
              <input type="text" defaultValue="ABC Electronics Store" />
            </div>
            <div className="form-group">
              <label>Business Type</label>
              <select defaultValue="electronics">
                <option value="electronics">Electronics</option>
                <option value="clothing">Clothing</option>
                <option value="home">Home & Garden</option>
                <option value="other">Other</option>
              </select>
            </div>
            <div className="form-group">
              <label>Business Description</label>
              <textarea rows="4" defaultValue="Leading electronics retailer specializing in consumer electronics and accessories." />
            </div>
            <div className="form-group">
              <label>Business Address</label>
              <input type="text" defaultValue="123 Main Street, City, Country" />
            </div>
            <button type="button" className="btn-primary">Save Changes</button>
          </form>
        </div>

        <div className="profile-card">
          <h2>Contact Information</h2>
          <form className="profile-form">
            <div className="form-group">
              <label>Email</label>
              <input type="email" defaultValue="vendor@example.com" />
            </div>
            <div className="form-group">
              <label>Phone</label>
              <input type="tel" defaultValue="+1234567890" />
            </div>
            <div className="form-group">
              <label>Website</label>
              <input type="url" defaultValue="https://www.example.com" />
            </div>
            <button type="button" className="btn-primary">Update Contact</button>
          </form>
        </div>

        <div className="profile-card">
          <h2>Payment Information</h2>
          <form className="profile-form">
            <div className="form-group">
              <label>Bank Account</label>
              <input type="text" defaultValue="****1234" />
            </div>
            <div className="form-group">
              <label>Payment Schedule</label>
              <select defaultValue="weekly">
                <option value="daily">Daily</option>
                <option value="weekly">Weekly</option>
                <option value="monthly">Monthly</option>
              </select>
            </div>
            <button type="button" className="btn-primary">Update Payment Info</button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Profile;