"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth-provider";

export default function Header() {
  const { userId, setUserId } = useAuth();

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
            <span className="text-xs text-gray-400">User:</span>
            <select
              value={userId}
              onChange={(e) => setUserId(Number(e.target.value))}
              className="rounded-lg border border-gray-200 bg-gray-50 px-2 py-1 text-sm font-medium focus:outline-none"
            >
              <option value={1}>User 1</option>
              <option value={2}>User 2</option>
            </select>
          </div>
        </div>
      </div>
    </nav>
  );
}
