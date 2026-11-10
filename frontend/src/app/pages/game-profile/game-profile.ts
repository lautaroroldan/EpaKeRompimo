import { HttpClient } from '@angular/common/http';
import { Component, inject, input } from '@angular/core';
import { GameDetail } from '../../interfaces/GameProfile';
import { API_URL } from '../../config/api';

@Component({
  selector: 'app-game-profile',
  imports: [],
  templateUrl: './game-profile.html',
  styleUrl: './game-profile.css',
})
export class GameProfile {
  title = input.required<string>();

  http = inject(HttpClient);

  ngOnInit() {
    this.http.get<GameDetail>(`${API_URL}/details?title=${encodeURIComponent(this.title())}`).subscribe((gameDetail) => {
      this.gameDetail = gameDetail;
    });
  }

  gameDetail: GameDetail | null = null;
}
