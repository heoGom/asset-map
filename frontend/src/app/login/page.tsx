"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useAuth } from "@/lib/auth-provider";

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-gray-50 p-8 text-sm text-gray-500">로그인 화면을 불러오는 중입니다.</div>}>
      <LoginForm />
    </Suspense>
  );
}

function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(false);
    setIsSubmitting(true);
    try {
      await login(email, password);
      router.replace(searchParams.get("next") || "/assets");
    } catch {
      setError(true);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <form onSubmit={handleSubmit} className="mx-auto mt-12 max-w-md space-y-5 rounded-xl border border-gray-100 bg-white p-6 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">로그인</h1>
          <p className="mt-1 text-sm text-gray-500">Asset Map 계정으로 자산 데이터를 관리합니다.</p>
        </div>

        <label className="block text-sm font-medium text-gray-700">
          이메일
          <input
            required
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            className="mt-1 block w-full rounded-lg border border-gray-300 p-2"
          />
        </label>

        <label className="block text-sm font-medium text-gray-700">
          비밀번호
          <input
            required
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            className="mt-1 block w-full rounded-lg border border-gray-300 p-2"
          />
        </label>

        {error && <p className="text-sm text-rose-600">로그인에 실패했습니다. 이메일과 비밀번호를 확인하세요.</p>}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-xl bg-emerald-600 p-2 font-bold text-white hover:bg-emerald-700 disabled:bg-gray-300"
        >
          {isSubmitting ? "로그인 중" : "로그인"}
        </button>

        <p className="text-center text-sm text-gray-500">
          계정이 없으면 <Link href="/signup" className="font-medium text-emerald-700 hover:text-emerald-800">회원가입</Link>
        </p>
      </form>
    </div>
  );
}
