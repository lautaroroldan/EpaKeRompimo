import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './guards/auth-guard';
import { Home } from './pages/home/home';

export const routes: Routes = [
  { path: '', component: Home },
  {
    path: 'login',
    title: 'Ingresar | EpaKeRompimo',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },
  {
    path: 'registro',
    title: 'Crear cuenta | EpaKeRompimo',
    canActivate: [guestGuard],
    loadComponent: () => import('./pages/register/register').then((m) => m.Register),
  },
  {
    path: 'cuenta',
    title: 'Mi cuenta | EpaKeRompimo',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/account/account').then((m) => m.Account),
  },
  { path: '**', redirectTo: '' },
];
