"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { getMe, login, signup, UserResponse } from "@/lib/api/auth";
import { clearAccessToken, getAccessToken, setAccessToken } from "@/lib/api-client";

interface AuthContextType {
  user: UserResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  signup: (email: string, password: string, nickname: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  isAuthenticated: false,
  isLoading: true,
  login: async () => {},
  signup: async () => {},
  logout: () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const restoreUser = async () => {
      const token = getAccessToken();
      if (!token) {
        setIsLoading(false);
        return;
      }
      try {
        setUser(await getMe());
      } catch {
        clearAccessToken();
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };

    restoreUser();
  }, []);

  const handleLogin = async (email: string, password: string) => {
    const response = await login({ email, password });
    setAccessToken(response.accessToken);
    setUser(response.user);
  };

  const handleSignup = async (email: string, password: string, nickname: string) => {
    const response = await signup({ email, password, nickname });
    setAccessToken(response.accessToken);
    setUser(response.user);
  };

  const handleLogout = () => {
    clearAccessToken();
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: Boolean(user),
        isLoading,
        login: handleLogin,
        signup: handleSignup,
        logout: handleLogout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
