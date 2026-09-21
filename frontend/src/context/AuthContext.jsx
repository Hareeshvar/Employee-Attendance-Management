import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authService } from '../services/authService';
import { setAccessToken } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setTokenState] = useState(null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const updateToken = (newToken) => {
    setAccessToken(newToken);
    setTokenState(newToken);
  };

  const handleLogout = useCallback(async () => {
    try {
      await authService.logout();
    } catch (e) {
      // Ignore logout errors
    } finally {
      localStorage.removeItem('has_session');
      updateToken(null);
      setUser(null);
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('role');
      localStorage.removeItem('userId');
    }
  }, []);

  // Silent authentication restoration on application startup via HttpOnly refresh cookie
  useEffect(() => {
    const initAuth = async () => {
      const hasSession = localStorage.getItem('has_session') === 'true' || !!localStorage.getItem('token');
      if (!hasSession) {
        setLoading(false);
        return;
      }

      try {
        const data = await authService.refresh();
        localStorage.setItem('has_session', 'true');
        updateToken(data.token);
        setUser(data);
      } catch (err) {
        // Silent refresh failed -> User is unauthenticated (guest)
        localStorage.removeItem('has_session');
        updateToken(null);
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    initAuth();
  }, []);

  // Listen to 401 unhandled refresh failures from api.js interceptor
  useEffect(() => {
    const onUnauthorized = () => {
      handleLogout();
    };
    window.addEventListener('auth:unauthorized', onUnauthorized);
    return () => {
      window.removeEventListener('auth:unauthorized', onUnauthorized);
    };
  }, [handleLogout]);

  const login = async (usernameInput, passwordInput) => {
    setLoading(true);
    try {
      const data = await authService.login({
        username: usernameInput,
        password: passwordInput,
      });

      localStorage.setItem('has_session', 'true');
      updateToken(data.token);
      setUser(data);
      return data;
    } finally {
      setLoading(false);
    }
  };

  const refreshUser = async () => {
    try {
      const userData = await authService.getCurrentUser();
      setUser(userData);
    } catch (err) {
      handleLogout();
    }
  };

  const cleanRole = user?.role ? user.role.replace('ROLE_', '').toUpperCase() : '';
  const permissions = user?.permissions || [];

  const isAdmin = cleanRole === 'ADMIN';
  const isHr = cleanRole === 'HR';
  const isManager = cleanRole === 'MANAGER';
  const isEmployee = cleanRole === 'EMPLOYEE';

  const hasPermission = (permission) => permissions.includes(permission);
  const hasRole = (roles) => {
    if (!Array.isArray(roles)) roles = [roles];
    return roles.includes(cleanRole);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        userId: user?.userId || null,
        username: user?.username || null,
        email: user?.email || null,
        firstName: user?.firstName || null,
        lastName: user?.lastName || null,
        role: cleanRole,
        departmentId: user?.departmentId || null,
        departmentName: user?.departmentName || null,
        designationName: user?.designationName || null,
        permissions,
        isAuthenticated: !!token && !!user,
        isAdmin,
        isHr,
        isManager,
        isEmployee,
        hasPermission,
        hasRole,
        login,
        logout: handleLogout,
        refreshUser,
        loading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
