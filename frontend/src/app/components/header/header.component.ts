import { Component } from '@angular/core';
import { InputGame } from '../input-game/input-game';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [InputGame],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

}
