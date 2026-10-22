import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';

@Component({
  selector: 'app-user-avatar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { '[class]': 'hostClasses()' },
  template: `
    @if (showImage()) {
      <img
        [src]="icon()"
        [alt]="username()"
        class="size-full object-cover"
        (error)="failedUrl.set(icon())"
      />
    } @else {
      <span>{{ initial() }}</span>
    }
  `,
})
export class UserAvatar {
  readonly icon = input<string | null | undefined>(null);
  readonly username = input.required<string>();
  readonly size = input<'sm' | 'lg'>('sm');

  // Si la imagen no carga, mostramos la inicial del usuario
  protected readonly failedUrl = signal<string | null | undefined>(null);
  protected readonly showImage = computed(() => !!this.icon() && this.icon() !== this.failedUrl());
  protected readonly initial = computed(() => this.username().charAt(0));

  protected readonly hostClasses = computed(
    () =>
      'inline-flex shrink-0 items-center justify-center overflow-hidden rounded-full bg-muted font-medium uppercase ' +
      (this.size() === 'lg' ? 'size-16 text-2xl' : 'size-8 text-sm'),
  );
}
