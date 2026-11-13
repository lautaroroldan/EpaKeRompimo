import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { UserService } from '../../services/user-service';
import { errorMessage } from '../../utils/http-error';

@Component({
  selector: 'app-verify-email',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './verify-email.html',
})
export class VerifyEmail {
  private readonly route = inject(ActivatedRoute);
  private readonly users = inject(UserService);
  private readonly auth = inject(AuthService);

  protected readonly isLoggedIn = this.auth.isLoggedIn;
  protected readonly status = signal<'loading' | 'ok' | 'error'>('loading');
  protected readonly error = signal<string | null>(null);

  constructor() {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.error.set('El link no es válido.');
      this.status.set('error');
      return;
    }

    this.users.verifyEmail(token).subscribe({
      next: () => this.status.set('ok'),
      error: (err) => {
        this.error.set(errorMessage(err, 'No se pudo confirmar el mail.'));
        this.status.set('error');
      },
    });
  }
}