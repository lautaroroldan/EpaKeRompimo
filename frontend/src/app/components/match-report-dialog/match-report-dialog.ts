import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  afterNextRender,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatchReportType } from '../../interfaces/MatchReport';
import { ReportService } from '../../services/report-service';
import { errorMessage } from '../../utils/http-error';

/**
 * Diálogo para que un usuario reporte un matcheo equivocado. Se muestra solo
 * si el usuario tiene sesión (los padres chequean eso antes de abrirlo).
 */
@Component({
  selector: 'app-match-report-dialog',
  imports: [ReactiveFormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './match-report-dialog.html',
  host: {
    '(document:keydown.escape)': 'close()',
  },
})
export class MatchReportDialog {
  readonly type = input.required<MatchReportType>();
  readonly gameTitle = input.required<string>();
  readonly igdbGameId = input<number | null>(null);
  readonly itadGameId = input<string | null>(null);
  readonly storeName = input<string | null>(null);
  readonly currentUrl = input<string | null>(null);

  readonly closed = output<void>();
  readonly saved = output<void>();

  private readonly fb = inject(NonNullableFormBuilder);
  private readonly reports = inject(ReportService);
  private readonly messageInput = viewChild<ElementRef<HTMLTextAreaElement>>('message');

  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly sent = signal(false);

  protected readonly form = this.fb.group({
    message: ['', [Validators.maxLength(1000)]],
  });

  constructor() {
    afterNextRender(() => this.messageInput()?.nativeElement.focus());
  }

  protected get isStoreLink(): boolean {
    return this.type() === 'STORE_LINK_MISMATCH';
  }

  protected submit() {
    if (this.saving() || this.sent()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);

    const message = this.form.controls.message.value.trim();
    this.reports
      .create({
        type: this.type(),
        gameTitle: this.gameTitle(),
        igdbGameId: this.igdbGameId(),
        itadGameId: this.itadGameId(),
        storeName: this.storeName(),
        currentUrl: this.currentUrl(),
        message: message.length ? message : null,
      })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.sent.set(true);
          this.saved.emit();
        },
        error: (err) => {
          this.error.set(errorMessage(err, 'No pudimos enviar el reporte. Probá de nuevo.'));
          this.saving.set(false);
        },
      });
  }

  protected close() {
    this.closed.emit();
  }

  // El clic en el fondo cierra; el clic dentro del panel no.
  protected onBackdropClick(event: MouseEvent) {
    if (event.target === event.currentTarget) {
      this.close();
    }
  }
}
