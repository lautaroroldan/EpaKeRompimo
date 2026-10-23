import { AbstractControl, AsyncValidatorFn, ValidationErrors, ValidatorFn } from '@angular/forms';
import { catchError, map, of, switchMap, timer } from 'rxjs';
import { UserService } from '../services/user-service';

// Valida a nivel de formulario que "password" y "confirmPassword" sean iguales
export function passwordsMatch(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const password = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return password === confirm ? null : { passwordsMismatch: true };
  };
}

// Consulta GET /api/users/exists/{username}. Espera 400 ms desde la última tecla para no
// llamar al backend en cada letra (Angular cancela la validación anterior si el valor cambia).
export function usernameAvailable(
  users: UserService,
  currentUsername: () => string | null = () => null,
): AsyncValidatorFn {
  return (control: AbstractControl) => {
    const value = String(control.value ?? '').trim();
    if (!value || value === currentUsername()) {
      return of(null);
    }
    return timer(400).pipe(
      switchMap(() => users.existsByUsername(value)),
      map((exists) => (exists ? { usernameTaken: true } : null)),
      catchError(() => of(null)), // si falla la consulta, que decida el backend al enviar
    );
  };
}
