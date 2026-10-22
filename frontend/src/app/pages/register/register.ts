import { toSignal } from '@angular/core/rxjs-interop';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { switchMap } from 'rxjs';
import { AuthService } from '../../services/auth-service';
import { UserService } from '../../services/user-service';
import { errorMessage } from '../../utils/http-error';
import { passwordsMatch, usernameAvailable } from '../../validators/user-validators';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, HlmInputImports],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './register.html',
})
export class Register {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly users = inject(UserService);
  private readonly router = inject(Router);

  // Mismas reglas que el backend (UserRequest): username <= 50, password 8..72, icon <= 255
  protected readonly form = this.fb.group(
    {
      username: this.fb.control('', {
        validators: [Validators.required, Validators.maxLength(50)],
        asyncValidators: [usernameAvailable(this.users)],
      }),
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmPassword: ['', [Validators.required]],
      icon: ['', [Validators.maxLength(255), Validators.pattern(/^https?:\/\/\S+$/)]],
    },
    { validators: passwordsMatch() },
  );

  // La validación asíncrona (username disponible) termina fuera de un evento del usuario;
  // este signal avisa a la vista que tiene que actualizarse.
  protected readonly status = toSignal(this.form.statusChanges, {
    initialValue: this.form.status,
  });

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit() {
    if (this.form.invalid || this.form.pending) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, password, icon } = this.form.getRawValue();
    const name = username.trim();
    this.loading.set(true);
    this.error.set(null);

    // 1) crea la cuenta  2) inicia sesión con esos datos  3) va a "Mi cuenta"
    this.auth
      .register({ username: name, password, icon: icon.trim() || null })
      .pipe(switchMap(() => this.auth.login({ username: name, password })))
      .subscribe({
        next: () => this.router.navigate(['/cuenta']),
        error: (err) => {
          this.error.set(errorMessage(err, 'No se pudo crear la cuenta.'));
          this.loading.set(false);
        },
      });
  }
}
