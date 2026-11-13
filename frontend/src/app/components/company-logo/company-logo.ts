import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';

// Logo de una empresa. Si no hay logo (o la imagen no carga), muestra la inicial del nombre.
@Component({
  selector: 'app-company-logo',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '[class]': 'hostClasses()' },
  template: `
    @if (showImage()) {
      <img [src]="logoUrl()" [alt]="name()" loading="lazy" class="size-full object-contain"
        (error)="failedUrl.set(logoUrl())" />
    } @else {
      <span aria-hidden="true">{{ initial() }}</span>
    }
  `,
})
export class CompanyLogo {
  readonly logoUrl = input<string | null | undefined>(null);
  readonly name = input.required<string>();

  protected readonly failedUrl = signal<string | null | undefined>(null);
  protected readonly showImage = computed(() => !!this.logoUrl() && this.logoUrl() !== this.failedUrl());
  protected readonly initial = computed(() => this.name().charAt(0));

  protected readonly hostClasses = computed(
    () =>
      'inline-flex size-9 shrink-0 items-center justify-center overflow-hidden rounded-md text-sm font-medium ' +
      (this.showImage() ? 'bg-white p-1' : 'bg-muted'),
  );
}
