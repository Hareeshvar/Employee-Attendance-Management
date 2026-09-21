import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';

import LandingPage from './pages/LandingPage';
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
import ProfilePage from './pages/ProfilePage';
import AuditLogsPage from './pages/AuditLogsPage';
import WorkplacesPage from './pages/WorkplacesPage';

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

const IndexRoute = () => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return null;
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }
  return <LandingPage />;
};

const LoginRoute = () => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return null;
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }
  return <LoginPage />;
};

const App = () => {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<IndexRoute />} />
          <Route path="/login" element={<LoginRoute />} />

          <Route element={<ProtectedRoute />}>
            <Route element={<Layout />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/attendance" element={<AttendancePage />} />
              <Route path="/leaves" element={<LeavesPage />} />
              <Route path="/notifications" element={<NotificationsPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/workplaces" element={<WorkplacesPage />} />

              <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'HR', 'MANAGER']} />}>
                <Route path="/users" element={<UsersPage />} />
              </Route>

              <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'HR']} />}>
                <Route path="/departments" element={<DepartmentsPage />} />
                <Route path="/designations" element={<DesignationsPage />} />
                <Route path="/shifts" element={<ShiftsPage />} />
                <Route path="/audit-logs" element={<AuditLogsPage />} />
              </Route>

              <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'HR', 'EMPLOYEE']} />}>
                <Route path="/payrolls" element={<PayrollsPage />} />
              </Route>

              <Route element={<ProtectedRoute allowedRoles={['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE']} />}>
                <Route path="/reports" element={<ReportsPage />} />
              </Route>

              <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                <Route path="/roles" element={<RolesPage />} />
              </Route>

              <Route path="*" element={<Navigate to="/" replace />} />
            </Route>
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;
