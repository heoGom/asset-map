import { fetchApi } from "../api-client";

export interface DividendImportRequest {
  securityItemId?: number;
  fromYear?: number;
  toYear?: number;
}

export interface DividendImportResult {
  targetSecurityCount: number;
  importedEventCount: number;
  skippedEventCount: number;
  generatedPaymentCount: number;
  failedSecurityCount: number;
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
