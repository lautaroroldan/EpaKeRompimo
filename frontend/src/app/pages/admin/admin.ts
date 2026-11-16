import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Game } from '../../interfaces/Game';
import { GameDetail } from '../../interfaces/GameProfile';
import {
  MatchReport,
  MatchReportResolveRequest,
  MatchReportStatus,
  MatchReportType,
} from '../../interfaces/MatchReport';
import { AdminService } from '../../services/admin-service';
import { errorMessage } from '../../utils/http-error';
import { formatPrice } from '../../utils/price';
import {
  reportDate,
  reportStatusClasses,
  reportStatusLabel,
  reportTypeLabel,
} from '../../utils/reports';

@Component({
  selector: 'app-admin',
  imports: [RouterLink],
  templateUrl: './admin.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Admin {
  protected readonly admin = inject(AdminService);

  protected readonly statusFilter = signal<MatchReportStatus | ''>('');
  protected readonly typeFilter = signal<MatchReportType | ''>('');

  // Lo que el admin eligió para este reporte
  protected readonly selectedIgdb = signal<GameDetail | null>(null);
  protected readonly selectedItad = signal<Game | null>(null);

  protected readonly igdbQuery = signal('');
  protected readonly itadQuery = signal('');

  protected readonly storeName = signal('');
  protected readonly storeUrl = signal('');
  protected readonly hideOffer = signal(false);
  protected readonly note = signal('');

  protected readonly saving = signal(false);
  protected readonly feedback = signal<string | null>(null);
  protected readonly actionError = signal<string | null>(null);

  protected readonly formatPrice = formatPrice;
  protected readonly typeLabel = reportTypeLabel;
  protected readonly statusLabel = reportStatusLabel;
  protected readonly statusClasses = reportStatusClasses;
  protected readonly formatDate = reportDate;

  constructor() {
    this.reload();
  }

  protected reload() {
    this.admin.loadReports(this.statusFilter() || undefined, this.typeFilter() || undefined);
  }

  protected onStatusFilter(value: string) {
    this.statusFilter.set(value as MatchReportStatus | '');
    this.reload();
  }

  protected onTypeFilter(value: string) {
    this.typeFilter.set(value as MatchReportType | '');
    this.reload();
  }

  protected open(report: MatchReport) {
    this.admin.select(report);
    this.selectedIgdb.set(null);
    this.selectedItad.set(null);
    this.storeName.set(report.storeName ?? '');
    this.storeUrl.set(report.currentUrl ?? '');
    this.hideOffer.set(false);
    this.note.set('');
    this.igdbQuery.set(report.gameTitle);
    this.itadQuery.set(report.gameTitle);
    this.feedback.set(null);
    this.actionError.set(null);
  }

  protected searchIgdb() {
    this.admin.searchIgdb(this.igdbQuery());
  }

  protected searchItad() {
    this.admin.searchItad(this.itadQuery());
  }

  protected pickIgdb(game: GameDetail) {
    this.selectedIgdb.set(game);
  }

  protected pickItad(game: Game) {
    this.selectedItad.set(game);
    this.admin.loadItadPrices(game.id);
  }

  protected resolve() {
    const report = this.admin.selected();
    if (!report || this.saving()) return;

    const igdb = this.selectedIgdb();
    const itad = this.selectedItad();
    const store = this.storeName().trim();
    const hide = this.hideOffer();

    const request: MatchReportResolveRequest = {
      status: 'RESOLVED',
      adminNote: this.note().trim() || null,
      igdbGameId: igdb?.externalId ?? null,
      itadGameId: itad?.id ?? null,
      storeName: store || null,
      storeUrl: hide ? null : this.storeUrl().trim() || null,
      hideOffer: hide && !!store,
    };

    this.save(
      report.id,
      request,
      'Reporte resuelto. La corrección ya se aplica en la ficha del juego.',
    );
  }

  protected reject() {
    const report = this.admin.selected();
    if (!report || this.saving()) return;

    this.save(
      report.id,
      { status: 'REJECTED', adminNote: this.note().trim() || null },
      'Reporte rechazado.',
    );
  }

  private save(id: number, request: MatchReportResolveRequest, message: string) {
    this.saving.set(true);
    this.actionError.set(null);
    this.feedback.set(null);

    this.admin.resolve(id, request).subscribe({
      next: () => {
        this.saving.set(false);
        this.feedback.set(message);
        this.admin.select(null);
        this.reload();
      },
      error: (err) => {
        this.actionError.set(errorMessage(err, 'No se pudo guardar la resolución.'));
        this.saving.set(false);
      },
    });
  }

  protected clearOverride() {
    const report = this.admin.selected();
    if (!report || this.saving()) return;

    this.saving.set(true);
    this.actionError.set(null);
    this.admin.clearOverride(report.gameTitle).subscribe({
      next: () => {
        this.saving.set(false);
        this.admin.loadOverride(report.gameTitle);
        this.feedback.set('Se quitaron las correcciones de este título.');
      },
      error: (err) => {
        this.actionError.set(errorMessage(err, 'No se pudieron quitar las correcciones.'));
        this.saving.set(false);
      },
    });
  }

  protected typeClasses(): string {
    return 'rounded-full border border-border px-2 py-0.5 text-[11px] text-muted-foreground';
  }
}
