"use client";

import React, { createContext, useContext, useState, useEffect } from 'react';

interface AuthContextType {
  userId: number;
  setUserId: (id: number) => void;
}

const AuthContext = createContext<AuthContextType>({
  userId: 1,
  setUserId: () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [userId, setUserId] = useState<number>(1);

  useEffect(() => {
    const savedUserId = localStorage.getItem('mock_userId');
    if (savedUserId) {
      setUserId(Number(savedUserId));
    }
  }, []);

  const handleSetUserId = (id: number) => {
    setUserId(id);
    localStorage.setItem('mock_userId', String(id));
  };

  return (
    <AuthContext.Provider value={{ userId, setUserId: handleSetUserId }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
