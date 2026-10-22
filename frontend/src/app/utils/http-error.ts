import { HttpErrorResponse } from '@angular/common/http';

// El backend responde los errores como ProblemDetail: { status, detail, ... }
export function errorMessage(
  err: unknown,
  fallback = 'Ocurrió un error inesperado. Probá de nuevo.',
): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'No se pudo conectar con el servidor.';
    }
    const detail = err.error?.detail;
    if (typeof detail === 'string' && detail.length > 0) {
      return detail;
    }
  }
  return fallback;
}
