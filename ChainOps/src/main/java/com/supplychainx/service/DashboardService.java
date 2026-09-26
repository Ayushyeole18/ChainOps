package com.supplychainx.service;

import com.supplychainx.dao.DashboardDao;
import com.supplychainx.dao.DashboardDaoImpl;
import com.supplychainx.model.DashboardMetrics;

public class DashboardService {

    private final DashboardDao dashboardDao;

    public DashboardService() {
        this.dashboardDao = new DashboardDaoImpl();
    }

    public DashboardService(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    public DashboardMetrics getDashboardMetrics() {
        return dashboardDao.fetchDashboardMetrics();
    }
}
