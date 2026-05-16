"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth-provider";

export default function AuthGate({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const { isAuthenticated, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
    }
  }, [isAuthenticated, isLoading, pathname, router]);

  if (isLoading) {
    return <div className="min-h-screen bg-gray-50 p-8 text-sm text-gray-500">인증 정보를 확인하는 중입니다.</div>;
  }

  if (!isAuthenticated) {
    return <div className="min-h-screen bg-gray-50 p-8 text-sm text-gray-500">로그인이 필요합니다.</div>;
  }

  return <>{children}</>;
}
