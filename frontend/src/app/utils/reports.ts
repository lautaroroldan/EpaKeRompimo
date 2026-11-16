import { MatchReportStatus, MatchReportType } from '../interfaces/MatchReport';

// Etiquetas y estilos compartidos entre "Mis reportes" y el panel de admin.

export function reportTypeLabel(type: MatchReportType): string {
  return type === 'DESCRIPTION_MISMATCH' ? 'Descripción' : 'Link de tienda';
}

export function reportStatusLabel(status: MatchReportStatus): string {
  switch (status) {
    case 'PENDING':
      return 'Pendiente';
    case 'RESOLVED':
      return 'Resuelto';
    default:
      return 'Rechazado';
  }
}

export function reportStatusClasses(status: MatchReportStatus): string {
  const base = 'rounded-full px-2 py-0.5 text-[11px] font-medium';
  switch (status) {
    case 'PENDING':
      return `${base} bg-accent text-foreground`;
    case 'RESOLVED':
      return `${base} bg-primary/15 text-foreground`;
    default:
      return `${base} bg-destructive/10 text-destructive`;
  }
}

export function reportDate(iso: string | null): string {
  if (!iso) return '';
  return new Date(iso).toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' });
}
