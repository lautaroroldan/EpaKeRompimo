import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { DEFAULT_COUNTRY, API_URL } from '../config/api';
import { GameDeal } from '../interfaces/Deal';

@Injectable({
  providedIn: 'root',
})
export class DealService {
  /** ITAD caps the deals page at 200; that is also our whole homepage dataset. */
  static readonly pageSize = 200;

  private readonly http = inject(HttpClient);

  findDeals(offset = 0, limit = DealService.pageSize, sort = '-cut') {
    const params = new HttpParams()
      .set('country', DEFAULT_COUNTRY)
      .set('offset', offset)
      .set('limit', limit)
      .set('sort', sort);

    return this.http.get<GameDeal[]>(`${API_URL}/deals`, { params });
  }
}
