"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth-provider";

export default function SignupPage() {
  const router = useRouter();
  const { signup } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [error, setError] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(false);
    setIsSubmitting(true);
    try {
      await signup(email, password, nickname);
      router.replace("/accounts");
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
          <h1 className="text-2xl font-bold text-gray-900">회원가입</h1>
          <p className="mt-1 text-sm text-gray-500">로그인 후 계좌와 거래를 사용자별로 관리합니다.</p>
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
          닉네임
          <input
            required
            type="text"
            value={nickname}
            onChange={(event) => setNickname(event.target.value)}
            className="mt-1 block w-full rounded-lg border border-gray-300 p-2"
          />
        </label>

        <label className="block text-sm font-medium text-gray-700">
          비밀번호
          <input
            required
            minLength={8}
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            className="mt-1 block w-full rounded-lg border border-gray-300 p-2"
          />
        </label>

        {error && <p className="text-sm text-rose-600">회원가입에 실패했습니다. 입력값을 확인하세요.</p>}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-xl bg-emerald-600 p-2 font-bold text-white hover:bg-emerald-700 disabled:bg-gray-300"
        >
          {isSubmitting ? "가입 중" : "회원가입"}
        </button>

        <p className="text-center text-sm text-gray-500">
          이미 계정이 있으면 <Link href="/login" className="font-medium text-emerald-700 hover:text-emerald-800">로그인</Link>
        </p>
      </form>
    </div>
  );
}
