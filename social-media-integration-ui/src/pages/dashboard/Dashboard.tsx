import React from 'react';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  Box,
  Avatar,
  Chip,
  LinearProgress,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  IconButton,
} from '@mui/material';
import {
  Facebook,
  Twitter,
  Instagram,
  LinkedIn,
  TrendingUp,
  TrendingDown,
  MoreVert,
  Launch,
} from '@mui/icons-material';

const Dashboard = () => {
  const platformStats = [
    {
      platform: 'Facebook',
      icon: <Facebook />,
      color: '#1877F2',
      followers: '125.4K',
      engagement: '4.2%',
      posts: 24,
      trend: 'up',
      change: '+12%',
    },
    {
      platform: 'Instagram',
      icon: <Instagram />,
      color: '#E4405F',
      followers: '89.2K',
      engagement: '6.8%',
      posts: 36,
      trend: 'up',
      change: '+8.5%',
    },
    {
      platform: 'Twitter',
      icon: <Twitter />,
      color: '#1DA1F2',
      followers: '45.1K',
      engagement: '3.1%',
      posts: 18,
      trend: 'down',
      change: '-2.3%',
    },
    {
      platform: 'LinkedIn',
      icon: <LinkedIn />,
      color: '#0A66C2',
      followers: '32.7K',
      engagement: '5.4%',
      posts: 12,
      trend: 'up',
      change: '+15.2%',
    },
  ];

  const recentPosts = [
    {
      platform: 'Instagram',
      content: 'New summer collection is here! 🌞',
      engagement: '2.1K likes, 45 comments',
      time: '2 hours ago',
      status: 'Published',
    },
    {
      platform: 'Facebook',
      content: 'Flash sale: 50% off all electronics',
      engagement: '1.8K likes, 67 comments',
      time: '4 hours ago',
      status: 'Published',
    },
    {
      platform: 'Twitter',
      content: 'Customer service excellence award 🏆',
      engagement: '890 likes, 23 retweets',
      time: '6 hours ago',
      status: 'Published',
    },
  ];

  const StatCard = ({ platform, icon, color, followers, engagement, posts, trend, change }: any) => (
    <Card>
      <CardContent>
        <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
          <Box display="flex" alignItems="center">
            <Avatar sx={{ bgcolor: color, mr: 2 }}>{icon}</Avatar>
            <Typography variant="h6">{platform}</Typography>
          </Box>
          <IconButton size="small">
            <MoreVert />
          </IconButton>
        </Box>
        
        <Grid container spacing={2}>
          <Grid item xs={4}>
            <Typography variant="body2" color="text.secondary">
              Followers
            </Typography>
            <Typography variant="h6">{followers}</Typography>
          </Grid>
          <Grid item xs={4}>
            <Typography variant="body2" color="text.secondary">
              Engagement
            </Typography>
            <Typography variant="h6">{engagement}</Typography>
          </Grid>
          <Grid item xs={4}>
            <Typography variant="body2" color="text.secondary">
              Posts
            </Typography>
            <Typography variant="h6">{posts}</Typography>
          </Grid>
        </Grid>
        
        <Box display="flex" alignItems="center" mt={2}>
          {trend === 'up' ? (
            <TrendingUp color="success" fontSize="small" />
          ) : (
            <TrendingDown color="error" fontSize="small" />
          )}
          <Typography
            variant="body2"
            color={trend === 'up' ? 'success.main' : 'error.main'}
            ml={0.5}
          >
            {change} from last month
          </Typography>
        </Box>
      </CardContent>
    </Card>
  );

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>
        Social Media Dashboard
      </Typography>
      
      {/* Overview Cards */}
      <Grid container spacing={3} mb={4}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="primary">
                Total Reach
              </Typography>
              <Typography variant="h4">524.3K</Typography>
              <LinearProgress 
                variant="determinate" 
                value={75} 
                sx={{ mt: 2 }}
                color="primary"
              />
              <Typography variant="body2" color="text.secondary" mt={1}>
                +18% from last week
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="secondary">
                Engagement Rate
              </Typography>
              <Typography variant="h4">4.8%</Typography>
              <LinearProgress 
                variant="determinate" 
                value={60} 
                sx={{ mt: 2 }}
                color="secondary"
              />
              <Typography variant="body2" color="text.secondary" mt={1}>
                +2.4% from last week
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="success.main">
                Active Campaigns
              </Typography>
              <Typography variant="h4">12</Typography>
              <LinearProgress 
                variant="determinate" 
                value={80} 
                sx={{ mt: 2 }}
                color="success"
              />
              <Typography variant="body2" color="text.secondary" mt={1}>
                3 ending this week
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="warning.main">
                Scheduled Posts
              </Typography>
              <Typography variant="h4">28</Typography>
              <LinearProgress 
                variant="determinate" 
                value={45} 
                sx={{ mt: 2 }}
                color="warning"
              />
              <Typography variant="body2" color="text.secondary" mt={1}>
                Next post in 2 hours
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Platform Statistics */}
      <Grid container spacing={3} mb={4}>
        {platformStats.map((platform) => (
          <Grid item xs={12} sm={6} md={3} key={platform.platform}>
            <StatCard {...platform} />
          </Grid>
        ))}
      </Grid>

      {/* Recent Activity */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent Posts
              </Typography>
              <List>
                {recentPosts.map((post, index) => (
                  <ListItem key={index} divider={index < recentPosts.length - 1}>
                    <ListItemAvatar>
                      <Avatar>
                        {post.platform === 'Instagram' && <Instagram />}
                        {post.platform === 'Facebook' && <Facebook />}
                        {post.platform === 'Twitter' && <Twitter />}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={post.content}
                      secondary={
                        <Box>
                          <Typography variant="body2" color="text.secondary">
                            {post.engagement} • {post.time}
                          </Typography>
                          <Chip 
                            label={post.status}
                            size="small"
                            color="success"
                            sx={{ mt: 1 }}
                          />
                        </Box>
                      }
                    />
                    <IconButton>
                      <Launch />
                    </IconButton>
                  </ListItem>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Quick Actions
              </Typography>
              <Box display="flex" flexDirection="column" gap={2}>
                <Box
                  p={2}
                  border="1px solid"
                  borderColor="divider"
                  borderRadius={1}
                  textAlign="center"
                  sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
                >
                  <Typography variant="body1">Create New Post</Typography>
                </Box>
                <Box
                  p={2}
                  border="1px solid"
                  borderColor="divider"
                  borderRadius={1}
                  textAlign="center"
                  sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
                >
                  <Typography variant="body1">Schedule Campaign</Typography>
                </Box>
                <Box
                  p={2}
                  border="1px solid"
                  borderColor="divider"
                  borderRadius={1}
                  textAlign="center"
                  sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
                >
                  <Typography variant="body1">Analytics Report</Typography>
                </Box>
                <Box
                  p={2}
                  border="1px solid"
                  borderColor="divider"
                  borderRadius={1}
                  textAlign="center"
                  sx={{ cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }}
                >
                  <Typography variant="body1">Manage Accounts</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;