import { inject, Injectable } from '@angular/core';
import { GameWithPrices } from '../interfaces/Game';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class GameService {
  http = inject(HttpClient)
  baseURL = 'http://localhost:8080/api';

  searchGame(input: string, country: string) {
    const params = new HttpParams().set('title', input).set('country', country)
    return this.http.get<GameWithPrices[]>(`${this.baseURL}/games/`, { params })
  }
}
