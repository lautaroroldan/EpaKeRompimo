import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { InputGame } from './components/input-game/input-game';
import { HeaderComponent } from './components/header/header.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, InputGame, HeaderComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
