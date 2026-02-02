"use client";

import { useMemo, useState } from "react";

type Issue = {
  type: string;
  severity: string;
  message: string;
  line?: number | null;
  source?: string | null;
};

type ValidationResponse = {
  decision?: {
    status: string;
    reason?: string | null;
    triggeredRules?: string[] | null;
  };
  analysis?: {
    issues?: Issue[] | null;
    riskScore?: number | null;
    hasBlockingIssues?: boolean | null;
  };
  suggestions?: string[] | null;
};

const defaultSource = `// 예시: 안전하지 않은 SQL 조합
function buildQuery(userId: string) {
  return "SELECT * FROM users WHERE id = " + userId;
}`;

export default function Home() {
  const [sourceCode, setSourceCode] = useState(defaultSource);
  const [filePath, setFilePath] = useState("src/service/user.ts");
  const [context, setContext] = useState("유저 조회 API 핸들러");
  const [language, setLanguage] = useState("TypeScript");
  const [result, setResult] = useState<ValidationResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const apiBase = useMemo(
    () => process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8080",
    []
  );

  const handleSubmit = async () => {
    if (!sourceCode.trim()) {
      setError("검증할 코드를 입력해 주세요.");
      return;
    }

    setIsSubmitting(true);
    setError(null);
    setResult(null);
    const payload: Record<string, string> = {
      sourceCode,
    };
    if (filePath.trim()) {
      payload.filePath = filePath.trim();
    }
    if (context.trim()) {
      payload.context = context.trim();
    }
    if (language.trim()) {
      payload.language = language.trim();
    }

    try {
      const response = await fetch("/api/review", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "API 요청에 실패했습니다.");
      }

      const data = (await response.json()) as ValidationResponse;
      setResult(data);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : "알 수 없는 오류가 발생했습니다.";
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const issues = result?.analysis?.issues ?? [];
  const suggestions = result?.suggestions ?? [];

  return (
    <div className="min-h-screen bg-zinc-50 px-6 py-12 text-zinc-900">
      <main className="mx-auto flex w-full max-w-5xl flex-col gap-8">
        <section className="flex flex-col gap-3">
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-zinc-500">
            Vibe AI Code Guard
          </p>
          <h1 className="text-3xl font-semibold leading-tight">
            AI가 만든 코드를 검증하고 위험 신호를 요약합니다.
          </h1>
          <p className="text-base text-zinc-600">
            백엔드 API(`{apiBase}`)로 프록시 요청을 보내고, 정책·분석 결과를
            대시보드로 확인하세요.
          </p>
        </section>

        <section className="grid gap-6 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm lg:grid-cols-[1.4fr_1fr]">
          <div className="flex flex-col gap-5">
            <div className="grid gap-4 md:grid-cols-2">
              <label className="flex flex-col gap-2 text-sm font-medium">
                언어
                <select
                  className="rounded-lg border border-zinc-200 px-3 py-2 text-sm outline-none focus:border-zinc-400"
                  value={language}
                  onChange={(event) => setLanguage(event.target.value)}
                >
                  <option>TypeScript</option>
                  <option>JavaScript</option>
                  <option>Java</option>
                  <option>Python</option>
                  <option>Go</option>
                  <option>SQL</option>
                </select>
              </label>
              <label className="flex flex-col gap-2 text-sm font-medium">
                파일 경로
                <input
                  className="rounded-lg border border-zinc-200 px-3 py-2 text-sm outline-none focus:border-zinc-400"
                  value={filePath}
                  onChange={(event) => setFilePath(event.target.value)}
                  placeholder="src/service/user.ts"
                />
              </label>
            </div>

            <label className="flex flex-col gap-2 text-sm font-medium">
              컨텍스트
              <input
                className="rounded-lg border border-zinc-200 px-3 py-2 text-sm outline-none focus:border-zinc-400"
                value={context}
                onChange={(event) => setContext(event.target.value)}
                placeholder="기능 설명 또는 배경"
              />
            </label>

            <label className="flex flex-col gap-2 text-sm font-medium">
              코드 입력
              <textarea
                className="min-h-[240px] rounded-lg border border-zinc-200 px-3 py-2 text-sm font-mono outline-none focus:border-zinc-400"
                value={sourceCode}
                onChange={(event) => setSourceCode(event.target.value)}
              />
            </label>

            <button
              className="inline-flex w-fit items-center justify-center rounded-full bg-zinc-900 px-6 py-2 text-sm font-semibold text-white transition hover:bg-zinc-800 disabled:cursor-not-allowed disabled:bg-zinc-400"
              onClick={handleSubmit}
              disabled={isSubmitting}
            >
              {isSubmitting ? "검증 중..." : "코드 검증 요청"}
            </button>

            {error ? (
              <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {error}
              </p>
            ) : null}
          </div>

          <div className="flex flex-col gap-4">
            <div className="rounded-xl border border-zinc-200 bg-zinc-50 p-4">
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-zinc-400">
                결과 요약
              </p>
              {result ? (
                <div className="mt-3 flex flex-col gap-3 text-sm text-zinc-700">
                  <div className="flex items-center justify-between">
                    <span>판단</span>
                    <span className="rounded-full bg-zinc-900 px-3 py-1 text-xs font-semibold text-white">
                      {result.decision?.status ?? "UNKNOWN"}
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>리스크 점수</span>
                    <span className="font-semibold">
                      {result.analysis?.riskScore ?? 0}
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>차단 여부</span>
                    <span>
                      {result.analysis?.hasBlockingIssues ? "YES" : "NO"}
                    </span>
                  </div>
                  {result.decision?.reason ? (
                    <p className="rounded-lg border border-zinc-200 bg-white px-3 py-2 text-xs text-zinc-600">
                      {result.decision.reason}
                    </p>
                  ) : null}
                </div>
              ) : (
                <p className="mt-3 text-sm text-zinc-500">
                  결과가 아직 없습니다.
                </p>
              )}
            </div>

            <div className="rounded-xl border border-zinc-200 bg-white p-4">
              <p className="text-xs font-semibold uppercase tracking-[0.2em] text-zinc-400">
                트리거된 룰
              </p>
              {result?.decision?.triggeredRules?.length ? (
                <ul className="mt-3 flex flex-col gap-2 text-sm text-zinc-700">
                  {result.decision.triggeredRules.map((rule) => (
                    <li key={rule} className="rounded-lg bg-zinc-50 px-3 py-2">
                      {rule}
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="mt-3 text-sm text-zinc-500">
                  적용된 룰이 없습니다.
                </p>
              )}
            </div>
          </div>
        </section>

        <section className="grid gap-6 lg:grid-cols-2">
          <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold">발견된 이슈</h2>
            {issues.length ? (
              <ul className="mt-4 flex flex-col gap-3">
                {issues.map((issue, index) => (
                  <li
                    key={`${issue.type}-${issue.message}-${index}`}
                    className="rounded-xl border border-zinc-200 bg-zinc-50 p-4 text-sm"
                  >
                    <div className="flex flex-wrap items-center gap-2 text-xs font-semibold text-zinc-500">
                      <span className="rounded-full bg-zinc-900 px-2 py-1 text-white">
                        {issue.severity}
                      </span>
                      <span>{issue.type}</span>
                      {issue.line ? <span>Line {issue.line}</span> : null}
                      {issue.source ? <span>{issue.source}</span> : null}
                    </div>
                    <p className="mt-2 text-sm text-zinc-700">
                      {issue.message}
                    </p>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="mt-4 text-sm text-zinc-500">
                이슈가 보고되지 않았습니다.
              </p>
            )}
          </div>

          <div className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
            <h2 className="text-lg font-semibold">개선 제안</h2>
            {suggestions.length ? (
              <ul className="mt-4 flex list-disc flex-col gap-2 pl-5 text-sm text-zinc-700">
                {suggestions.map((suggestion) => (
                  <li key={suggestion}>{suggestion}</li>
                ))}
              </ul>
            ) : (
              <p className="mt-4 text-sm text-zinc-500">
                개선 제안이 아직 없습니다.
              </p>
            )}
          </div>
        </section>
      </main>
    </div>
  );
}
