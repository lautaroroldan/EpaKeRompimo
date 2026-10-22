import { ChangeDetectionStrategy, Component, afterNextRender, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { InputGame } from '../input-game/input-game';
import { UserAvatar } from '../user-avatar/user-avatar';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, InputGame, UserAvatar],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './navbar.html',
})
export class Navbar {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  // La página se pre-renderiza en el servidor (sin sesión). Mostramos la zona de usuario recién
  // cuando ya corre en el navegador, para que el HTML del servidor y el del cliente coincidan.
  protected readonly ready = signal(false);

  constructor() {
    afterNextRender(() => this.ready.set(true));
  }

  protected logout() {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
