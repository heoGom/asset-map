import Link from "next/link";

export default function Home() {
  return (
    <main className="min-h-screen bg-gray-50 text-gray-900">
      {/* Navigation */}
      <nav className="border-b border-gray-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <Link href="/" className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-emerald-600"></div>
            <span className="text-xl font-bold tracking-tight">Asset Map</span>
          </Link>
          <div className="flex gap-6 text-sm font-medium text-gray-600">
            <Link href="/assets" className="hover:text-emerald-600">자산 대시보드</Link>
            <Link href="/dividends" className="hover:text-emerald-600">배당 대시보드</Link>
            <Link href="/accounts" className="hover:text-emerald-600">계좌 관리</Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative overflow-hidden px-6 pt-24 pb-32">
        <div className="mx-auto max-w-7xl text-center">
          <div className="inline-flex items-center rounded-full border border-emerald-100 bg-emerald-50 px-3 py-1 text-sm font-medium text-emerald-700 mb-6">
            ✨ 새로운 자산 관리의 시작
          </div>
          <h1 className="text-5xl font-extrabold tracking-tight sm:text-7xl">
            자산의 지도를 그리고,<br />
            <span className="text-emerald-600">미래의 부</span>를 설계하세요.
          </h1>
          <p className="mx-auto mt-8 max-w-2xl text-xl leading-8 text-gray-600">
            흩어져 있는 당신의 계좌와 종목들을 한곳으로 모으세요.<br />
            정교한 시각화 차트와 배당 예측 시스템이 당신의 투자를 돕습니다.
          </p>
          <div className="mt-12 flex items-center justify-center gap-6">
            <Link
              href="/assets"
              className="rounded-xl bg-gray-900 px-8 py-4 text-lg font-bold text-white shadow-xl transition-all hover:bg-gray-800 hover:scale-105 active:scale-95"
            >
              대시보드 시작하기
            </Link>
            <Link
              href="/assets"
              className="rounded-xl border border-gray-200 bg-white px-8 py-4 text-lg font-bold text-gray-900 shadow-sm transition-all hover:bg-gray-50"
            >
              기능 둘러보기
            </Link>
          </div>
        </div>

        {/* Abstract visual element */}
        <div className="absolute top-1/2 left-1/2 -z-10 h-[600px] w-[600px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-emerald-50 opacity-50 blur-3xl"></div>
      </section>

      {/* Features Section */}
      <section className="bg-white py-24">
        <div className="mx-auto max-w-7xl px-6">
          <div className="grid grid-cols-1 gap-12 md:grid-cols-3">
            {/* Feature 1 */}
            <div className="group rounded-3xl border border-gray-100 bg-gray-50 p-8 transition-all hover:border-emerald-100 hover:bg-emerald-50/30">
              <div className="mb-6 flex h-12 w-12 items-center justify-center rounded-xl bg-white shadow-sm transition-transform group-hover:scale-110">
                📊
              </div>
              <h3 className="text-2xl font-bold">다각도 자산 분석</h3>
              <p className="mt-4 text-gray-600 leading-relaxed">
                섹터, 국가, 전략별로 포트폴리오 비중을 시각화하여 특정 자산군에 쏠림이 없는지 한눈에 파악합니다.
              </p>
            </div>

            {/* Feature 2 */}
            <div className="group rounded-3xl border border-gray-100 bg-gray-50 p-8 transition-all hover:border-emerald-100 hover:bg-emerald-50/30">
              <div className="mb-6 flex h-12 w-12 items-center justify-center rounded-xl bg-white shadow-sm transition-transform group-hover:scale-110">
                💰
              </div>
              <h3 className="text-2xl font-bold">배당금 예측 엔진</h3>
              <p className="mt-4 text-gray-600 leading-relaxed">
                보유 종목의 배당 일정을 추적하여 미래의 현금 흐름을 월별 달력 형태로 예측하고 관리합니다.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="group rounded-3xl border border-gray-100 bg-gray-50 p-8 transition-all hover:border-emerald-100 hover:bg-emerald-50/30">
              <div className="mb-6 flex h-12 w-12 items-center justify-center rounded-xl bg-white shadow-sm transition-transform group-hover:scale-110">
                📈
              </div>
              <h3 className="text-2xl font-bold">성장 타임라인</h3>
              <p className="mt-4 text-gray-600 leading-relaxed">
                주기적인 스냅샷 저장 기능을 통해 내 자산이 시간이 지남에 따라 어떻게 우상향하는지 기록합니다.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-gray-100 bg-white py-12 text-center text-sm text-gray-400">
        <div className="mx-auto max-w-7xl px-6">
          <p>© 2026 Asset Map. All rights reserved.</p>
        </div>
      </footer>
    </main>
  );
}
