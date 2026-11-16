export type MatchReportType = 'DESCRIPTION_MISMATCH' | 'STORE_LINK_MISMATCH';
export type MatchReportStatus = 'PENDING' | 'RESOLVED' | 'REJECTED';

// POST /api/reports
export interface MatchReportRequest {
  type: MatchReportType;
  gameTitle: string;
  igdbGameId?: number | null;
  itadGameId?: string | null;
  storeName?: string | null;
  currentUrl?: string | null;
  message?: string | null;
}

// Lo que devuelve el backend (también en /api/reports/mine y /api/admin/reports)
export interface MatchReport {
  id: number;
  userId: number;
  username: string;
  type: MatchReportType;
  status: MatchReportStatus;
  gameTitle: string;
  igdbGameId: number | null;
  itadGameId: string | null;
  storeName: string | null;
  currentUrl: string | null;
  message: string | null;
  adminNote: string | null;
  createdAt: string;
  resolvedAt: string | null;
  resolvedBy: string | null;
}

// PATCH /api/admin/reports/{id}
export interface MatchReportResolveRequest {
  status: 'RESOLVED' | 'REJECTED';
  adminNote?: string | null;
  igdbGameId?: number | null;
  itadGameId?: string | null;
  storeName?: string | null;
  storeUrl?: string | null;
  hideOffer?: boolean;
}

export interface StoreLinkOverride {
  storeName: string;
  url: string | null;
  hidden: boolean;
  updatedAt: string;
}

// GET /api/admin/overrides?title=
export interface MatchOverride {
  displayTitle: string;
  igdbGameId: number | null;
  itadGameId: string | null;
  updatedAt: string | null;
  storeLinks: StoreLinkOverride[];
}
