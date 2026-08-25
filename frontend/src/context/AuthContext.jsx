import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [username, setUsername] = useState(localStorage.getItem('username') || null);
  const [role, setRole] = useState(localStorage.getItem('role') || null);
  const [userId, setUserId] = useState(localStorage.getItem('userId') || null);
  const [loading, setLoading] = useState(false);

  const login = async (usernameInput, passwordInput) => {
    setLoading(true);
    try {
      const data = await authService.login({
        username: usernameInput,
        password: passwordInput,
      });

      const authToken = data.token;
      const authUser = data.username;
      const authRole = data.role; // e.g. "ROLE_ADMIN" or "ROLE_EMPLOYEE"

      localStorage.setItem('token', authToken);
      localStorage.setItem('username', authUser);
      localStorage.setItem('role', authRole);

      setToken(authToken);
      setUsername(authUser);
      setRole(authRole);

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
    setUsername(null);
    setRole(null);
    setUserId(null);
  };

  const saveUserId = (id) => {
    localStorage.setItem('userId', id);
    setUserId(id);
  };

  const cleanRole = role ? role.replace('ROLE_', '').toUpperCase() : '';
  const isAdmin = cleanRole === 'ADMIN';
  const isEmployee = cleanRole === 'EMPLOYEE' || cleanRole === 'USER';

  return (
    <AuthContext.Provider
      value={{
        token,
        username,
        role: cleanRole,
        rawRole: role,
        userId,
        saveUserId,
        isAuthenticated: !!token,
        isAdmin,
        isEmployee,
        login,
        logout,
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
