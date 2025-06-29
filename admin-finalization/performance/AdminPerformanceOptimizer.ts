// Week 14: Admin Interface Performance Optimization

import { debounce, memoize } from 'lodash';
import { QueryClient, useQuery, useMutation } from 'react-query';

/**
 * Performance optimizations for admin dashboards
 */
export const AdminPerformanceOptimizer = {
  
  // 1. Dashboard data caching
  dashboardQueryClient: new QueryClient({
    defaultOptions: {
      queries: {
        staleTime: 30000, // 30 seconds
        cacheTime: 300000, // 5 minutes
        refetchOnWindowFocus: false,
      },
    },
  }),

  // 2. Optimized data fetching with pagination
  useOptimizedAdminData: (endpoint: string, params: any) => {
    return useQuery(
      [endpoint, params],
      async () => {
        const response = await adminAPI.get(endpoint, { params });
        return response.data;
      },
      {
        keepPreviousData: true,
        refetchInterval: 60000, // 1 minute
      }
    );
  },

  // 3. Debounced search and filters
  createDebouncedSearch: (searchFunction: Function, delay = 300) => {
    return debounce(searchFunction, delay);
  },

  // 4. Optimized table rendering with virtualization
  virtualTableConfig: {
    rowHeight: 48,
    overscan: 5,
    estimatedRowCount: 1000,
    windowScroll: true,
  },

  // 5. Memoized expensive calculations
  memoizedCalculations: {
    calculateRegionalMetrics: memoize((data) => {
      // Heavy calculation for regional performance
      return {
        totalRevenue: data.reduce((sum, item) => sum + item.revenue, 0),
        averageOrderValue: data.reduce((sum, item) => sum + item.aov, 0) / data.length,
        conversionRate: (data.filter(item => item.converted).length / data.length) * 100,
        growthRate: calculateGrowthRate(data),
      };
    }),

    calculateGlobalMetrics: memoize((regionalData) => {
      // Aggregate global metrics from regional data
      const global = regionalData.reduce((acc, region) => {
        acc.totalRevenue += region.metrics.totalRevenue;
        acc.totalUsers += region.metrics.totalUsers;
        acc.totalOrders += region.metrics.totalOrders;
        return acc;
      }, { totalRevenue: 0, totalUsers: 0, totalOrders: 0 });

      return {
        ...global,
        averageRevenuePerRegion: global.totalRevenue / regionalData.length,
        globalConversionRate: (global.totalOrders / global.totalUsers) * 100,
      };
    }),
  },

  // 6. Batch API requests
  batchRequester: {
    queue: [] as any[],
    timer: null as any,

    addRequest: function(request: any) {
      this.queue.push(request);
      
      if (!this.timer) {
        this.timer = setTimeout(() => {
          this.flush();
        }, 100); // Batch requests every 100ms
      }
    },

    flush: async function() {
      if (this.queue.length === 0) return;

      const requests = [...this.queue];
      this.queue = [];
      this.timer = null;

      try {
        const response = await adminAPI.batch(requests);
        // Resolve individual promises
        requests.forEach((req, index) => {
          req.resolve(response[index]);
        });
      } catch (error) {
        // Reject all promises on batch failure
        requests.forEach(req => req.reject(error));
      }
    },
  },

  // 7. Chart performance optimization
  chartOptimizations: {
    // Downsample large datasets for charts
    downsampleData: (data: any[], maxPoints = 100) => {
      if (data.length <= maxPoints) return data;
      
      const step = Math.ceil(data.length / maxPoints);
      return data.filter((_, index) => index % step === 0);
    },

    // Lazy load chart libraries
    loadChartLibrary: async (chartType: string) => {
      switch (chartType) {
        case 'line':
          return import('react-chartjs-2').then(module => module.Line);
        case 'bar':
          return import('react-chartjs-2').then(module => module.Bar);
        case 'pie':
          return import('react-chartjs-2').then(module => module.Pie);
        default:
          throw new Error(`Unknown chart type: ${chartType}`);
      }
    },
  },

  // 8. Optimized list rendering
  optimizedListRenderer: {
    // Virtual scrolling for large lists
    createVirtualList: (items: any[], renderItem: Function) => {
      return {
        items: items,
        renderItem: memoize(renderItem),
        getItemKey: (item: any) => item.id,
        estimatedItemSize: 48,
      };
    },

    // Incremental loading
    useIncrementalLoad: (initialCount = 50, increment = 50) => {
      const [displayCount, setDisplayCount] = useState(initialCount);
      
      const loadMore = useCallback(() => {
        setDisplayCount(prev => prev + increment);
      }, [increment]);

      return { displayCount, loadMore };
    },
  },

  // 9. Memory management
  memoryOptimizations: {
    // Clean up old data from cache
    cleanupCache: () => {
      AdminPerformanceOptimizer.dashboardQueryClient.clear();
    },

    // Unsubscribe from real-time updates when not needed
    manageRealTimeSubscriptions: {
      activeSubscriptions: new Map(),
      
      subscribe: (key: string, callback: Function) => {
        if (!this.activeSubscriptions.has(key)) {
          const subscription = adminWebSocket.subscribe(key, callback);
          this.activeSubscriptions.set(key, subscription);
        }
      },

      unsubscribe: (key: string) => {
        const subscription = this.activeSubscriptions.get(key);
        if (subscription) {
          subscription.unsubscribe();
          this.activeSubscriptions.delete(key);
        }
      },

      cleanup: () => {
        this.activeSubscriptions.forEach(subscription => {
          subscription.unsubscribe();
        });
        this.activeSubscriptions.clear();
      },
    },
  },

  // 10. Analytics performance
  analyticsOptimizations: {
    // Pre-aggregate common metrics
    preAggregateMetrics: async (region: string, period: string) => {
      const cacheKey = `analytics_${region}_${period}`;
      const cached = await cache.get(cacheKey);
      
      if (cached) return cached;

      const metrics = await adminAPI.getAnalytics(region, period);
      const aggregated = {
        ...metrics,
        computed: {
          avgOrderValue: metrics.totalRevenue / metrics.totalOrders,
          conversionRate: (metrics.totalOrders / metrics.totalVisits) * 100,
          customerLifetimeValue: metrics.totalRevenue / metrics.uniqueCustomers,
        },
      };

      await cache.set(cacheKey, aggregated, 3600); // 1 hour cache
      return aggregated;
    },

    // Optimize report generation
    generateOptimizedReport: async (params: any) => {
      return new Promise((resolve) => {
        // Use web worker for heavy calculations
        const worker = new Worker('/workers/report-generator.js');
        
        worker.postMessage(params);
        worker.onmessage = (e) => {
          resolve(e.data);
          worker.terminate();
        };
      });
    },
  },
};

// Helper functions
function calculateGrowthRate(data: any[]): number {
  if (data.length < 2) return 0;
  
  const current = data[data.length - 1].value;
  const previous = data[0].value;
  
  return ((current - previous) / previous) * 100;
}

// Performance monitoring
export const AdminPerformanceMonitor = {
  startTime: performance.now(),
  
  recordMetric: (name: string, duration: number) => {
    console.log(`[PERF] ${name}: ${duration.toFixed(2)}ms`);
    
    // Send to monitoring service
    if (window.analytics) {
      window.analytics.track('admin_performance', {
        metric: name,
        duration: duration,
        timestamp: new Date().toISOString(),
      });
    }
  },
  
  measureComponent: (name: string) => {
    const start = performance.now();
    
    return () => {
      const duration = performance.now() - start;
      AdminPerformanceMonitor.recordMetric(name, duration);
    };
  },
  
  measureAsync: async (name: string, fn: Function) => {
    const start = performance.now();
    
    try {
      const result = await fn();
      const duration = performance.now() - start;
      AdminPerformanceMonitor.recordMetric(name, duration);
      return result;
    } catch (error) {
      const duration = performance.now() - start;
      AdminPerformanceMonitor.recordMetric(`${name}_error`, duration);
      throw error;
    }
  },
};

export default AdminPerformanceOptimizer;
