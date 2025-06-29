// Week 14: Admin UI/UX Refinements

import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  IconButton,
  Tooltip,
  Snackbar,
  Alert,
  CircularProgress,
  Skeleton,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  KeyboardArrowUp as ScrollTopIcon,
  Fullscreen as FullscreenIcon,
  FullscreenExit as FullscreenExitIcon,
  DarkMode as DarkModeIcon,
  LightMode as LightModeIcon,
  Notifications as NotificationIcon,
  Help as HelpIcon,
} from '@mui/icons-material';

// 1. Enhanced Loading States
export const AdminLoadingState: React.FC<{
  type: 'skeleton' | 'spinner' | 'progress';
  rows?: number;
  height?: number;
}> = ({ type, rows = 5, height = 48 }) => {
  switch (type) {
    case 'skeleton':
      return (
        <Box>
          {Array.from({ length: rows }).map((_, index) => (
            <Skeleton key={index} height={height} sx={{ mb: 1 }} />
          ))}
        </Box>
      );
    
    case 'spinner':
      return (
        <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
          <CircularProgress />
        </Box>
      );
    
    case 'progress':
      return (
        <Box sx={{ width: '100%', p: 2 }}>
          <LinearProgress />
        </Box>
      );
    
    default:
      return null;
  }
};

// 2. Enhanced Notification System
export const AdminNotificationBanner: React.FC = () => {
  const [notifications, setNotifications] = useState([]);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    // Subscribe to admin notifications
    const unsubscribe = adminNotifications.subscribe((notification) => {
      setNotifications(prev => [...prev, notification]);
      setOpen(true);
    });

    return unsubscribe;
  }, []);

  const handleClose = (event, reason) => {
    if (reason === 'clickaway') return;
    setOpen(false);
  };

  const handleAction = (notification) => {
    notification.action?.();
    setNotifications(prev => prev.filter(n => n.id !== notification.id));
  };

  return (
    <Snackbar
      open={open}
      autoHideDuration={6000}
      onClose={handleClose}
      anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
    >
      <Alert 
        onClose={handleClose} 
        severity={notifications[0]?.severity || 'info'}
        action={
          notifications[0]?.action && (
            <Button 
              color="inherit" 
              size="small" 
              onClick={() => handleAction(notifications[0])}
            >
              {notifications[0].actionLabel}
            </Button>
          )
        }
      >
        {notifications[0]?.message}
      </Alert>
    </Snackbar>
  );
};

// 3. Scroll to Top Button
export const ScrollToTop: React.FC = () => {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    const toggleVisibility = () => {
      setIsVisible(window.pageYOffset > 300);
    };

    window.addEventListener('scroll', toggleVisibility);
    return () => window.removeEventListener('scroll', toggleVisibility);
  }, []);

  const scrollToTop = () => {
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  };

  return (
    <Box
      sx={{
        position: 'fixed',
        bottom: 24,
        right: 24,
        zIndex: 1000,
        opacity: isVisible ? 1 : 0,
        transition: 'opacity 0.3s',
      }}
    >
      <Tooltip title="Scroll to top">
        <IconButton 
          onClick={scrollToTop}
          sx={{
            bgcolor: 'primary.main',
            color: 'white',
            '&:hover': { bgcolor: 'primary.dark' },
          }}
        >
          <ScrollTopIcon />
        </IconButton>
      </Tooltip>
    </Box>
  );
};

// 4. Fullscreen Toggle
export const FullscreenToggle: React.FC = () => {
  const [isFullscreen, setIsFullscreen] = useState(false);

  const toggleFullscreen = () => {
    if (!document.fullscreenElement) {
      document.documentElement.requestFullscreen();
      setIsFullscreen(true);
    } else {
      document.exitFullscreen();
      setIsFullscreen(false);
    }
  };

  useEffect(() => {
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement);
    };

    document.addEventListener('fullscreenchange', handleFullscreenChange);
    return () => document.removeEventListener('fullscreenchange', handleFullscreenChange);
  }, []);

  return (
    <Tooltip title={isFullscreen ? 'Exit fullscreen' : 'Enter fullscreen'}>
      <IconButton onClick={toggleFullscreen}>
        {isFullscreen ? <FullscreenExitIcon /> : <FullscreenIcon />}
      </IconButton>
    </Tooltip>
  );
};

// 5. Enhanced Dark Mode Toggle
export const ThemeToggle: React.FC<{ onToggle: (mode: 'light' | 'dark') => void }> = ({ onToggle }) => {
  const theme = useTheme();
  const [mode, setMode] = useState<'light' | 'dark'>('light');

  const toggleTheme = () => {
    const newMode = mode === 'light' ? 'dark' : 'light';
    setMode(newMode);
    onToggle(newMode);
    localStorage.setItem('admin-theme', newMode);
  };

  useEffect(() => {
    const savedMode = localStorage.getItem('admin-theme') as 'light' | 'dark';
    if (savedMode) {
      setMode(savedMode);
      onToggle(savedMode);
    }
  }, []);

  return (
    <Tooltip title={`Switch to ${mode === 'light' ? 'dark' : 'light'} mode`}>
      <IconButton onClick={toggleTheme}>
        {mode === 'light' ? <DarkModeIcon /> : <LightModeIcon />}
      </IconButton>
    </Tooltip>
  );
};

// 6. Quick Help System
export const QuickHelp: React.FC = () => {
  const [helpOpen, setHelpOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const helpRef = useRef(null);

  const helpContent = [
    { id: 1, title: 'User Management', content: 'How to manage users...', keywords: ['user', 'account', 'manage'] },
    { id: 2, title: 'Order Processing', content: 'Processing orders...', keywords: ['order', 'process', 'fulfillment'] },
    { id: 3, title: 'Content Moderation', content: 'Moderating content...', keywords: ['content', 'moderate', 'review'] },
    // ... more help items
  ];

  const filteredHelp = helpContent.filter(item =>
    item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    item.keywords.some(keyword => keyword.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  return (
    <Box sx={{ position: 'relative' }}>
      <Tooltip title="Get help">
        <IconButton onClick={() => setHelpOpen(!helpOpen)}>
          <HelpIcon />
        </IconButton>
      </Tooltip>
      
      {helpOpen && (
        <Paper
          ref={helpRef}
          sx={{
            position: 'absolute',
            top: '100%',
            right: 0,
            width: 320,
            maxHeight: 400,
            overflow: 'auto',
            zIndex: 1000,
            mt: 1,
            p: 2,
          }}
        >
          <TextField
            fullWidth
            size="small"
            placeholder="Search help..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            sx={{ mb: 2 }}
          />
          
          <List dense>
            {filteredHelp.map((item) => (
              <ListItem key={item.id} button>
                <ListItemText
                  primary={item.title}
                  secondary={item.content.substring(0, 50) + '...'}
                />
              </ListItem>
            ))}
          </List>
        </Paper>
      )}
    </Box>
  );
};

// 7. Enhanced Table with Advanced Features
export const EnhancedAdminTable: React.FC<{
  columns: any[];
  data: any[];
  onSort?: (column: string, direction: 'asc' | 'desc') => void;
  onFilter?: (filters: any) => void;
  onExport?: () => void;
}> = ({ columns, data, onSort, onFilter, onExport }) => {
  const [sortConfig, setSortConfig] = useState({ key: '', direction: 'asc' });
  const [filters, setFilters] = useState({});
  const [selectedRows, setSelectedRows] = useState([]);

  const handleSort = (column: string) => {
    const direction = sortConfig.key === column && sortConfig.direction === 'asc' ? 'desc' : 'asc';
    setSortConfig({ key: column, direction });
    onSort?.(column, direction);
  };

  const handleFilter = (column: string, value: string) => {
    const newFilters = { ...filters, [column]: value };
    setFilters(newFilters);
    onFilter?.(newFilters);
  };

  return (
    <Box>
      <TableToolbar
        numSelected={selectedRows.length}
        onExport={onExport}
        filters={filters}
        onFilterChange={handleFilter}
      />
      
      <TableContainer component={Paper}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell padding="checkbox">
                <Checkbox
                  indeterminate={selectedRows.length > 0 && selectedRows.length < data.length}
                  checked={data.length > 0 && selectedRows.length === data.length}
                  onChange={(e) => {
                    setSelectedRows(e.target.checked ? data.map(row => row.id) : []);
                  }}
                />
              </TableCell>
              {columns.map((column) => (
                <TableCell key={column.id} sortDirection={sortConfig.key === column.id ? sortConfig.direction : false}>
                  <TableSortLabel
                    active={sortConfig.key === column.id}
                    direction={sortConfig.key === column.id ? sortConfig.direction : 'asc'}
                    onClick={() => handleSort(column.id)}
                  >
                    {column.label}
                  </TableSortLabel>
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {data.map((row) => (
              <TableRow
                key={row.id}
                selected={selectedRows.includes(row.id)}
                hover
                onClick={() => {
                  setSelectedRows(prev =>
                    prev.includes(row.id)
                      ? prev.filter(id => id !== row.id)
                      : [...prev, row.id]
                  );
                }}
              >
                <TableCell padding="checkbox">
                  <Checkbox checked={selectedRows.includes(row.id)} />
                </TableCell>
                {columns.map((column) => (
                  <TableCell key={column.id}>
                    {column.render ? column.render(row[column.id], row) : row[column.id]}
                  </TableCell>
                ))}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};

// 8. Responsive Navigation
export const ResponsiveAdminNav: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  if (isMobile) {
    return (
      <>
        <IconButton
          color="inherit"
          aria-label="open drawer"
          onClick={handleDrawerToggle}
          sx={{ mr: 2, display: { md: 'none' } }}
        >
          <MenuIcon />
        </IconButton>
        
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }} // Better mobile performance
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: 240 },
          }}
        >
          <AdminNavContent />
        </Drawer>
      </>
    );
  }

  return (
    <Drawer
      sx={{
        width: 240,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: 240,
          boxSizing: 'border-box',
        },
      }}
      variant="permanent"
      anchor="left"
    >
      <AdminNavContent />
    </Drawer>
  );
};

export default {
  AdminLoadingState,
  AdminNotificationBanner,
  ScrollToTop,
  FullscreenToggle,
  ThemeToggle,
  QuickHelp,
  EnhancedAdminTable,
  ResponsiveAdminNav,
};
