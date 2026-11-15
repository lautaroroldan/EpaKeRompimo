import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  effect,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../../services/auth-service';
import { UserAvatar } from '../user-avatar/user-avatar';

// Menú desplegable del usuario: favoritos, perfil y cerrar sesión en un solo lugar.
// Es autónomo: lee la sesión, navega y cierra sesión por su cuenta.
@Component({
  selector: 'app-user-menu',
  imports: [RouterLink, UserAvatar],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './user-menu.html',
  host: {
    // El menú se cierra al hacer clic afuera o con Escape
    '(document:click)': 'onDocumentClick($event)',
    '(document:keydown.escape)': 'closeMenu(true)',
  },
})
export class UserMenu {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly menuOpen = signal(false);
  private readonly menuButton = viewChild<ElementRef<HTMLButtonElement>>('menuButton');

  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  constructor() {
    // Al cambiar de página se cierra el menú (cubre también atrás/adelante del navegador)
    effect(() => {
      this.currentUrl();
      this.menuOpen.set(false);
    });
  }

  protected toggleMenu() {
    this.menuOpen.update((open) => !open);
  }

  protected closeMenu(returnFocus = false) {
    if (!this.menuOpen()) return;

    this.menuOpen.set(false);
    if (returnFocus) {
      this.menuButton()?.nativeElement.focus();
    }
  }

  // Los clics dentro del menú (y en el botón que lo abre) no lo cierran
  protected onDocumentClick(event: Event) {
    if (!this.menuOpen()) return;

    const target = event.target as Node | null;
    if (target && this.host.nativeElement.contains(target)) return;

    this.menuOpen.set(false);
  }

  protected logout() {
    this.closeMenu();
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
