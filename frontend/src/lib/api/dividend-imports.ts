import { fetchApi } from "../api-client";

export interface DividendImportRequest {
  securityItemId?: number;
  fromYear?: number;
  toYear?: number;
}

export interface DividendSkipSummary {
  reason: string;
  count: number;
}

export interface DividendSecurityImportResult {
  securityItemId: number;
  securityName: string;
  searchTerms: string[];
  httpStatus?: number;
  resultCode?: string;
  resultMsg?: string;
  totalCount: number;
  itemCount: number;
  importedCount: number;
  skippedCount: number;
  generatedPaymentCount: number;
  status: string;
  message: string;
  skipReasons: DividendSkipSummary[];
}

export interface DividendImportResult {
  targetSecurityCount: number;
  importedEventCount: number;
  skippedEventCount: number;
  generatedPaymentCount: number;
  failedSecurityCount: number;
  securities: DividendSecurityImportResult[];
}

export const importMyStockDividends = (request: DividendImportRequest) =>
  fetchApi<DividendImportResult>("/api/admin/dividends/import/public-data/stock/my-securities", {
    method: "POST",
    body: JSON.stringify(request),
  });

export const importOneStockDividend = (request: DividendImportRequest) =>
  fetchApi<DividendImportResult>("/api/admin/dividends/import/public-data/stock", {
    method: "POST",
    body: JSON.stringify(request),
  });
