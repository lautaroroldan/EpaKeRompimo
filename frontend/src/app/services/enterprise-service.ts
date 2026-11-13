import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Enterprise } from '../interfaces/Enterprise';

// Búsqueda de empresas para el buscador. La ruta es pública, por eso no depende de la sesión.
@Injectable({
  providedIn: 'root',
})
export class EnterpriseService {
  private readonly http = inject(HttpClient);
  private readonly api = `${environment.api}/enterprises`;

  // GET /api/enterprises/search?name=...  (devuelve un Observable, igual que GameService.searchGame,
  // porque lo usa el buscador con debounce + switchMap)
  search(name: string) {
    const params = new HttpParams().set('name', name);
    return this.http.get<Enterprise[]>(`${this.api}/search`, { params });
  }
}
