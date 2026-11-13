import { ChangeDetectionStrategy, Component, afterNextRender, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map } from 'rxjs';
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

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  // La home tiene su propio buscador en el hero.
  protected readonly isHome = computed(() => {
    const [path] = this.currentUrl().split(/[?#]/);
    return path === '/' || path === '';
  });

  constructor() {
    afterNextRender(() => this.ready.set(true));
  }

  protected logout() {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
