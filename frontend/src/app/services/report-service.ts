import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_URL } from '../config/api';
import { MatchReport, MatchReportRequest } from '../interfaces/MatchReport';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private readonly http = inject(HttpClient);

  // El backend toma el usuario del token
  create(request: MatchReportRequest) {
    return this.http.post<MatchReport>(`${API_URL}/reports`, request);
  }

  findMine() {
    return this.http.get<MatchReport[]>(`${API_URL}/reports/mine`);
  }
}
