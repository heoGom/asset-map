import { fetchApi } from "../api-client";

export interface SecurityClassificationResponse {
  id: number;
  securityItemId: number;
  ticker: string;
  securityName: string;
  countryGroup: string;
  assetGroup: string;
  sector: string;
  strategyType: string;
  theme?: string;
  listingCountry?: string;
  exposureCountry?: string;
  exposureRegion?: string;
  tradingCurrency?: string;
  currencyExposure?: string;
  underlyingIndex?: string;
  hedged?: boolean;
}

export interface SecurityClassificationCreateRequest {
  securityItemId: number;
  countryGroup: string;
  assetGroup: string;
  sector: string;
  strategyType: string;
  theme?: string;
  listingCountry?: string;
  exposureCountry?: string;
  exposureRegion?: string;
  tradingCurrency?: string;
  currencyExposure?: string;
  underlyingIndex?: string;
  hedged?: boolean;
}

export const getSecurityClassifications = () =>
  fetchApi<SecurityClassificationResponse[]>("/api/security-classifications");

export const createSecurityClassification = (request: SecurityClassificationCreateRequest) =>
  fetchApi<SecurityClassificationResponse>("/api/security-classifications", {
    method: "POST",
    body: JSON.stringify(request),
  });

export const updateSecurityClassification = (
  classificationId: number,
  request: Partial<Omit<SecurityClassificationCreateRequest, "securityItemId">>
) =>
  fetchApi<SecurityClassificationResponse>(`/api/security-classifications/${classificationId}`, {
    method: "PATCH",
    body: JSON.stringify(request),
  });
