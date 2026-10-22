import { createContext, useContext, useEffect, useMemo, useState } from 'react';

const AuthContext = createContext(null);

function readSavedUser() {
  try {
    const saved = sessionStorage.getItem('annraksh.user');
    return saved ? JSON.parse(saved) : null;
  } catch {
    sessionStorage.removeItem('annraksh.user');
    sessionStorage.removeItem('annraksh.token');
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readSavedUser);

  const clearSession = () => {
    sessionStorage.removeItem('annraksh.token');
    sessionStorage.removeItem('annraksh.user');
    setUser(null);
  };

  useEffect(() => {
    window.addEventListener('annraksh:auth-expired', clearSession);
    return () => window.removeEventListener('annraksh:auth-expired', clearSession);
  }, []);

  const value = useMemo(() => ({
    user,
    isAuthenticated: Boolean(user && sessionStorage.getItem('annraksh.token')),
    login: (authResponse) => {
      sessionStorage.setItem('annraksh.token', authResponse.accessToken);
      sessionStorage.setItem('annraksh.user', JSON.stringify(authResponse.farmer));
      setUser(authResponse.farmer);
    },
    updateUser: (updatedUser) => {
      sessionStorage.setItem('annraksh.user', JSON.stringify(updatedUser));
      setUser(updatedUser);
    },
    logout: clearSession,
  }), [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
