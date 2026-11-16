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
import { UserService } from '../../services/user-service';

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
  private readonly users = inject(UserService);

  protected readonly menuOpen = signal(false);
  protected readonly notificationsEnabled = signal(true);
  // false hasta que llega el valor real del backend (así no se muestra un estado equivocado)
  protected readonly notificationsLoaded = signal(false);
  protected readonly notificationsSaving = signal(false);
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

    // Trae del backend si el usuario tiene las notificaciones activas
    const id = this.auth.session()?.id;
    if (id !== undefined) {
      this.users.findById(id).subscribe({
        next: (user) => {
          this.notificationsEnabled.set(user.notificationsEnabled);
          this.notificationsLoaded.set(true);
        },
        error: () => {},
      });
    }
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

  protected toggleNotifications() {
    const id = this.auth.session()?.id;
    if (id === undefined || this.notificationsSaving()) return;

    const next = !this.notificationsEnabled();
    this.notificationsSaving.set(true);
    this.users.setNotificationsEnabled(id, next).subscribe({
      next: () => {
        this.notificationsEnabled.set(next);
        this.notificationsSaving.set(false);
      },
      error: () => this.notificationsSaving.set(false),
    });
  }

  protected logout() {
    this.closeMenu();
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
