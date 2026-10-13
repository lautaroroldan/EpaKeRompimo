import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { InputGame } from './components/input-game/input-game';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, InputGame],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
