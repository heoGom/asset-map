"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth-provider";

export default function Header() {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <nav className="border-b border-gray-200 bg-white">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
        <Link href="/" className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-lg bg-emerald-600"></div>
          <span className="text-xl font-bold tracking-tight text-gray-900">Asset Map</span>
        </Link>
        <div className="flex items-center gap-6">
          <div className="flex gap-6 text-sm font-medium text-gray-600">
            <Link href="/assets" className="hover:text-emerald-600">자산 대시보드</Link>
            <Link href="/dividends" className="hover:text-emerald-600">배당 대시보드</Link>
            <Link href="/accounts" className="hover:text-emerald-600">계좌 관리</Link>
          </div>
          <div className="flex items-center gap-2 border-l border-gray-200 pl-6">
            {isAuthenticated ? (
              <>
                <span className="text-sm font-medium text-gray-700">{user?.nickname}</span>
                <button
                  type="button"
                  onClick={logout}
                  className="rounded-lg border border-gray-200 px-3 py-1 text-sm font-medium text-gray-600 hover:bg-gray-50"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link href="/login" className="text-sm font-medium text-gray-600 hover:text-emerald-600">로그인</Link>
                <Link href="/signup" className="rounded-lg bg-emerald-600 px-3 py-1 text-sm font-bold text-white hover:bg-emerald-700">회원가입</Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
