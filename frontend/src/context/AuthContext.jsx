import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Restore authenticated session from backend on app startup
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        try {
          const userData = await authService.getCurrentUser();
          setUser(userData);
          setToken(storedToken);
        } catch (err) {
          // Token invalid or user inactive -> clear state
          logout();
        }
      }
      setLoading(false);
    };
    initAuth();
  }, []);

  const login = async (usernameInput, passwordInput) => {
    setLoading(true);
    try {
      const data = await authService.login({
        username: usernameInput,
        password: passwordInput,
      });

      const authToken = data.token;
      localStorage.setItem('token', authToken);

      setToken(authToken);
      setUser(data);
      return data;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    localStorage.removeItem('userId');

    setToken(null);
    setUser(null);
  };

  const refreshUser = async () => {
    if (token) {
      try {
        const userData = await authService.getCurrentUser();
        setUser(userData);
      } catch (err) {
        logout();
      }
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
        logout,
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
