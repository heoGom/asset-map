import { AccountResponse, AccountType } from "../api/accounts";

export const mockAccounts: AccountResponse[] = [
  {
    id: 1,
    userId: 1,
    name: "신한 주식 계좌",
    brokerName: "신한투자증권",
    accountType: AccountType.GENERAL,
    currency: "KRW",
    memo: "주력 국내외 주식용",
  },
  {
    id: 2,
    userId: 1,
    name: "KB ISA 계좌",
    brokerName: "KB증권",
    accountType: AccountType.ISA,
    currency: "KRW",
    memo: "절세용 계좌",
  },
  {
    id: 3,
    userId: 1,
    name: "미래에셋 연금",
    brokerName: "미래에셋증권",
    accountType: AccountType.PENSION,
    currency: "KRW",
    memo: "연금저축펀드",
  },
];
