import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';

import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import AttendancePage from './pages/AttendancePage';
import LeavesPage from './pages/LeavesPage';
import NotificationsPage from './pages/NotificationsPage';
import UsersPage from './pages/UsersPage';
import DepartmentsPage from './pages/DepartmentsPage';
import DesignationsPage from './pages/DesignationsPage';
import ShiftsPage from './pages/ShiftsPage';
import PayrollsPage from './pages/PayrollsPage';
import ReportsPage from './pages/ReportsPage';
import RolesPage from './pages/RolesPage';

// Main Application Layout Shell
const Layout = () => {
  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar />
        <Outlet />
      </div>
    </div>
  );
};

const App = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public Route */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Routes inside App Layout */}
          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/attendance" element={<AttendancePage />} />
              <Route path="/leaves" element={<LeavesPage />} />
              <Route path="/notifications" element={<NotificationsPage />} />

              {/* Admin-only Protected Modules */}
              <Route element={<ProtectedRoute adminOnly={true} />}>
                <Route path="/users" element={<UsersPage />} />
                <Route path="/departments" element={<DepartmentsPage />} />
                <Route path="/designations" element={<DesignationsPage />} />
                <Route path="/shifts" element={<ShiftsPage />} />
                <Route path="/payrolls" element={<PayrollsPage />} />
                <Route path="/reports" element={<ReportsPage />} />
                <Route path="/roles" element={<RolesPage />} />
              </Route>

              {/* Fallback */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Route>
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
