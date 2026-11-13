import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CompanyGame } from '../../interfaces/Company';

@Component({
  selector: 'app-game-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  template: `
    <a [routerLink]="['/game', game().title]" class="group flex flex-col gap-2">
      @if (game().coverImageUrl) {
        <img
          [src]="game().coverImageUrl"
          [alt]="game().title"
          loading="lazy"
          class="aspect-[3/4] w-full rounded-lg object-cover transition-transform group-hover:scale-[1.02]"
        />
      } @else {
        <div
          class="flex aspect-[3/4] w-full items-center justify-center rounded-lg bg-muted text-xs text-muted-foreground"
        >
          Sin portada
        </div>
      }
      <span class="line-clamp-2 text-sm font-medium group-hover:underline">{{ game().title }}</span>
      @if (game().released) {
        <span class="text-xs text-muted-foreground">{{ game().released!.slice(0, 4) }}</span>
      }
    </a>
  `,
})
export class GameCard {
  readonly game = input.required<CompanyGame>();
}
