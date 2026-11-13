import { toSignal } from '@angular/core/rxjs-interop';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { UserUpdateRequest } from '../../interfaces/User';
import { UserAvatar } from '../../components/user-avatar/user-avatar';
import { AuthService } from '../../services/auth-service';
import { UserService } from '../../services/user-service';
import { errorMessage } from '../../utils/http-error';
import { passwordsMatch, usernameAvailable } from '../../validators/user-validators';

@Component({
  selector: 'app-account',
  imports: [ReactiveFormsModule, HlmInputImports, UserAvatar],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './account.html',
})
export class Account {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly users = inject(UserService);
  private readonly router = inject(Router);

  protected readonly session = this.auth.session;

  // Mismas reglas que el backend (UserUpdateRequest). La contraseña es opcional: vacía = no cambia.
  protected readonly form = this.fb.group(
    {
      username: this.fb.control('', {
        validators: [Validators.required, Validators.maxLength(50)],
        // Si deja su propio username, no cuenta como "ya en uso"
        asyncValidators: [usernameAvailable(this.users, () => this.session()?.username ?? null)],
      }),
      icon: ['', [Validators.maxLength(255), Validators.pattern(/^https?:\/\/\S+$/)]],
      email: ['', [Validators.maxLength(254), Validators.email]],
      password: ['', [Validators.minLength(8), Validators.maxLength(72)]],
      confirmPassword: [''],
    },
    { validators: passwordsMatch() },
  );

  protected readonly status = toSignal(this.form.statusChanges, {
    initialValue: this.form.status,
  });

  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);

    // Lo que ya está guardado en el backend (no lo que se está tipeando)
  protected readonly confirmedEmail = signal<string | null>(null);
  protected readonly pendingEmail = signal<string | null>(null);
  protected readonly resending = signal(false);
  protected readonly resendMessage = signal<string | null>(null);

  protected readonly confirmingDelete = signal(false);
  protected readonly deleting = signal(false);
  protected readonly deleteError = signal<string | null>(null);

  constructor() {
    const current = this.session();
    if (!current) {
      this.router.navigate(['/login']);
      return;
    }

    // Trae los datos actuales desde el backend (GET /api/users/{id})
    this.users.findById(current.id).subscribe({
      next: (user) => {
        this.form.patchValue({ username: user.username, icon: user.icon ?? '', email: user.pendingEmail ?? user.email ?? '', });
        this.confirmedEmail.set(user.email);
        this.pendingEmail.set(user.pendingEmail);
        this.auth.updateSession({ username: user.username, icon: user.icon });
        this.loading.set(false);
      },
      error: (err) => {
        // 401 lo maneja el interceptor (vuelve al login)
        this.form.patchValue({ username: current.username, icon: current.icon ?? '' });
        this.error.set(errorMessage(err, 'No se pudieron cargar tus datos.'));
        this.loading.set(false);
      },
    });
  }

  protected save() {
    const current = this.session();
    if (!current) return;

    if (this.form.invalid || this.form.pending) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, icon, password, email } = this.form.getRawValue();
    // OJO: si password va vacía NO se manda. El backend la valida con @Size(min = 8)
    // y rechazaría un "" aunque la intención sea "no cambiar nada".
    const request: UserUpdateRequest = {
      username: username.trim(),
      icon: icon.trim() || null,
      email: email.trim() || null,
      ...(password ? { password } : {}),
    };

    this.saving.set(true);
    this.error.set(null);
    this.success.set(null);

    this.users.update(current.id, request).subscribe({
      next: (user) => {
        const nuevoPendiente = !!user.pendingEmail && user.pendingEmail !== this.pendingEmail();
        this.confirmedEmail.set(user.email);
        this.pendingEmail.set(user.pendingEmail);
        this.resendMessage.set(null);
        this.auth.updateSession({ username: user.username, icon: user.icon });
        this.form.patchValue({ email: user.pendingEmail ?? user.email ?? '',
          password: '',
          confirmPassword: '',});
        this.form.markAsPristine();
        this.success.set( nuevoPendiente
            ? `Tus datos se actualizaron. Te mandamos un mail a ${user.pendingEmail}: se guarda cuando lo confirmes.`
            : 'Tus datos se actualizaron.',);
        this.saving.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err, 'No se pudieron guardar los cambios.'));
        this.saving.set(false);
      },
    });
  }

  protected resendVerification() {
    const current = this.session();
    if (!current) return;

    this.resending.set(true);
    this.resendMessage.set(null);
    this.error.set(null);

    this.users.resendVerification(current.id).subscribe({
      next: () => {
        this.resendMessage.set('Listo, te mandamos el mail de nuevo. Revisá también spam.');
        this.resending.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err, 'No se pudo reenviar el mail.'));
        this.resending.set(false);
      },
    });
  }
  
  protected askDelete() {
    this.deleteError.set(null);
    this.confirmingDelete.set(true);
  }

  protected cancelDelete() {
    this.confirmingDelete.set(false);
  }

  protected confirmDelete() {
    const current = this.session();
    if (!current) return;

    this.deleting.set(true);
    this.deleteError.set(null);

    this.users.delete(current.id).subscribe({
      next: () => {
        this.auth.logout();
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.deleteError.set(errorMessage(err, 'No se pudo eliminar la cuenta.'));
        this.deleting.set(false);
      },
    });
  }
}
