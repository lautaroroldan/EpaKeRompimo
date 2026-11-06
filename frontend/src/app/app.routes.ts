import { Routes } from '@angular/router';
import { authGuard } from './guards/auth-guard';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { Account } from './pages/account/account';
import { Favorites } from './pages/favorites/favorites';
import { Home } from './pages/home/home';

export const routes: Routes = [
  { path:'', component:Home },
  { path: 'login', component: Login },
  { path: 'registro', component: Register },
  // Requieren estar logueado
  { path: 'cuenta', component: Account, canActivate: [authGuard] },
  { path: 'favoritos', component: Favorites, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
