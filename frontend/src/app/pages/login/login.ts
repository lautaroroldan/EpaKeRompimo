import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { AuthService } from '../../services/auth-service';
import { errorMessage } from '../../utils/http-error';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, HlmInputImports],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login.html',
})
export class Login {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  // El interceptor manda ?expired=1 cuando el token venció
  protected readonly expired = this.route.snapshot.queryParamMap.has('expired');

  protected readonly form = this.fb.group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    password: ['', [Validators.required, Validators.maxLength(72)]],
  });

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, password } = this.form.getRawValue();
    this.loading.set(true);
    this.error.set(null);

    this.auth.login({ username: username.trim(), password }).subscribe({
      next: () => this.router.navigate(['/cuenta']),
      error: (err) => {
        this.error.set(errorMessage(err, 'No se pudo iniciar sesión.'));
        this.loading.set(false);
      },
    });
  }
}
