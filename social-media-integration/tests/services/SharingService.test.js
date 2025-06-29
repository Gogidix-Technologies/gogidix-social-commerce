const { expect } = require('chai');
const sinon = require('sinon');
const mongoose = require('mongoose');
const axios = require('axios');

// Import the service and models
const SharingService = require('../../src/services/SharingService');
const SocialShare = require('../../src/models/SocialShare');
const OAuthService = require('../../src/services/OAuthService');
const EngagementMetric = require('../../src/models/EngagementMetric');

describe('SharingService', () => {
  // Setup stubs before each test
  beforeEach(() => {
    // Stub OAuthService methods
    this.validateTokenStub = sinon.stub(OAuthService, 'validateToken');
    this.getAccessTokenStub = sinon.stub(OAuthService, 'getAccessToken');
    
    // Stub SocialShare methods
    this.socialShareCreateStub = sinon.stub(SocialShare, 'create');
    this.socialShareGetShareCountsStub = sinon.stub(SocialShare.statics, 'getShareCountsByContent');
    
    // Stub EngagementMetric methods
    this.trackEngagementStub = sinon.stub(EngagementMetric, 'trackEngagement');
    
    // Stub axios
    this.axiosPostStub = sinon.stub(axios, 'post');
  });
  
  // Clean up stubs after each test
  afterEach(() => {
    sinon.restore();
  });
  
  describe('shareContent', () => {
    it('should successfully share content to Facebook', async () => {
      // Setup test data
      const userId = 'user123';
      const platform = 'facebook';
      const contentData = {
        type: 'product',
        id: 'product123',
        name: 'Test Product',
        description: 'A test product',
        image: 'http://example.com/image.jpg',
        url: 'http://example.com/products/product123'
      };
      const message = 'Check out this awesome product!';
      
      // Setup stubs to return successful responses
      this.validateTokenStub.withArgs(userId, platform).resolves(true);
      this.getAccessTokenStub.withArgs(userId, platform).resolves('mock-access-token');
      
      const shareRecord = {
        _id: 'share123',
        userId,
        contentType: contentData.type,
        contentId: contentData.id,
        platform,
        success: false,
        save: sinon.stub().resolves()
      };
      
      this.socialShareCreateStub.resolves(shareRecord);
      
      this.axiosPostStub.resolves({
        data: {
          id: 'fb_post_123'
        }
      });
      
      this.trackEngagementStub.resolves({});
      
      // Call the method
      const result = await SharingService.shareContent(userId, platform, contentData, message);
      
      // Assertions
      expect(result).to.be.an('object');
      expect(result.success).to.be.true;
      expect(result.shareId).to.equal('share123');
      expect(result.platform).to.equal('facebook');
      
      // Verify stubs were called with correct arguments
      sinon.assert.calledWith(this.validateTokenStub, userId, platform);
      sinon.assert.calledWith(this.getAccessTokenStub, userId, platform);
      sinon.assert.calledWith(this.socialShareCreateStub, sinon.match({
        userId,
        contentType: contentData.type,
        contentId: contentData.id,
        platform,
        message
      }));
      
      // Verify engagement was tracked
      sinon.assert.calledWith(this.trackEngagementStub, sinon.match({
        entityType: contentData.type,
        entityId: contentData.id,
        metricType: 'share',
        platform,
        userId
      }));
    });
    
    it('should throw an error if user has no valid token', async () => {
      // Setup test data
      const userId = 'user123';
      const platform = 'twitter';
      const contentData = {
        type: 'product',
        id: 'product123'
      };
      
      // Setup stub to return false (no valid token)
      this.validateTokenStub.withArgs(userId, platform).resolves(false);
      
      // Call the method and expect it to throw
      try {
        await SharingService.shareContent(userId, platform, contentData);
        // If we get here, the test should fail
        expect.fail('Expected method to throw');
      } catch (error) {
        expect(error.message).to.include('No valid twitter connection found');
      }
    });
    
    it('should handle errors during sharing', async () => {
      // Setup test data
      const userId = 'user123';
      const platform = 'facebook';
      const contentData = {
        type: 'product',
        id: 'product123',
        name: 'Test Product',
        image: 'http://example.com/image.jpg',
        url: 'http://example.com/products/product123'
      };
      
      // Setup stubs
      this.validateTokenStub.withArgs(userId, platform).resolves(true);
      this.getAccessTokenStub.withArgs(userId, platform).resolves('mock-access-token');
      
      const shareRecord = {
        _id: 'share123',
        userId,
        contentType: contentData.type,
        contentId: contentData.id,
        platform,
        success: false,
        save: sinon.stub().resolves()
      };
      
      this.socialShareCreateStub.resolves(shareRecord);
      
      // Make axios throw an error
      const errorMessage = 'API Error';
      this.axiosPostStub.rejects(new Error(errorMessage));
      
      // Call the method and expect it to throw
      try {
        await SharingService.shareContent(userId, platform, contentData);
        // If we get here, the test should fail
        expect.fail('Expected method to throw');
      } catch (error) {
        expect(error.message).to.include(`Failed to share to ${platform}`);
        
        // Verify the share record was updated with the error
        sinon.assert.calledWith(shareRecord.save);
        expect(shareRecord.success).to.be.false;
        expect(shareRecord.error).to.be.an('object');
      }
    });
  });
  
  describe('generateShareableLink', () => {
    it('should generate a valid shareable link', () => {
      // Setup test data
      const userId = 'user123';
      const contentData = {
        type: 'product',
        id: 'product123',
        name: 'Test Product',
        description: 'A test product',
        image: 'http://example.com/image.jpg'
      };
      const platform = 'facebook';
      
      // Call the method
      const result = SharingService.generateShareableLink(userId, contentData, platform);
      
      // Assertions
      expect(result).to.be.an('object');
      expect(result.url).to.be.a('string');
      expect(result.url).to.include(contentData.type);
      expect(result.url).to.include(contentData.id);
      expect(result.url).to.include(`ref=${userId}`);
      expect(result.url).to.include('utm_source=facebook');
      
      expect(result.metadata).to.be.an('object');
      expect(result.metadata.title).to.equal(contentData.name);
      expect(result.metadata.description).to.equal(contentData.description);
      expect(result.metadata.image).to.equal(contentData.image);
      
      expect(result.utmParams).to.be.an('object');
      expect(result.utmParams.utm_source).to.equal(platform);
    });
    
    it('should throw an error if content data is missing required fields', () => {
      // Setup invalid test data (missing id)
      const userId = 'user123';
      const contentData = {
        type: 'product'
        // Missing id
      };
      
      // Call the method and expect it to throw
      expect(() => {
        SharingService.generateShareableLink(userId, contentData);
      }).to.throw('Content type and ID are required');
    });
  });
  
  describe('getShareStats', () => {
    it('should return share statistics for content', async () => {
      // Setup test data
      const contentType = 'product';
      const contentId = 'product123';
      
      // Setup mock share stats
      const mockStats = [
        { platform: 'facebook', count: 10, clicks: 5 },
        { platform: 'twitter', count: 5, clicks: 2 }
      ];
      
      // Setup stub to return mock stats
      this.socialShareGetShareCountsStub.withArgs(contentType, contentId).resolves(mockStats);
      
      // Call the method
      const result = await SharingService.getShareStats(contentType, contentId);
      
      // Assertions
      expect(result).to.be.an('object');
      expect(result.byPlatform).to.deep.equal(mockStats);
      expect(result.totals).to.be.an('object');
      expect(result.totals.shareCount).to.equal(15); // 10 + 5
      expect(result.totals.clickCount).to.equal(7);  // 5 + 2
      
      // Verify stub was called with correct arguments
      sinon.assert.calledWith(this.socialShareGetShareCountsStub, contentType, contentId);
    });
    
    it('should handle errors when getting share stats', async () => {
      // Setup test data
      const contentType = 'product';
      const contentId = 'product123';
      
      // Setup stub to throw an error
      const errorMessage = 'Database error';
      this.socialShareGetShareCountsStub.withArgs(contentType, contentId).rejects(new Error(errorMessage));
      
      // Call the method and expect it to throw
      try {
        await SharingService.getShareStats(contentType, contentId);
        // If we get here, the test should fail
        expect.fail('Expected method to throw');
      } catch (error) {
        expect(error.message).to.include('Failed to get share statistics');
      }
    });
  });
});