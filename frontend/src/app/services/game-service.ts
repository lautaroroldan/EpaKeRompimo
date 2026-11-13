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
    return this.http.get<GameWithPrices[]>(`${this.baseURL}/games`, { params })
  }

  // Id de ITAD del juego de un título (404 si no hay): lo usa la estrella de favoritos del perfil
  findItadId(title: string) {
    const params = new HttpParams().set('title', title)
    return this.http.get<{ itadGameId: string }>(`${this.baseURL}/games/itad-id`, { params })
  }
}
