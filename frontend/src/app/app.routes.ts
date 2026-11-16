import { Routes } from '@angular/router';
import { adminGuard, authGuard, guestGuard } from './guards/auth-guard';
import { Home } from './pages/home/home';
import { GameProfile } from './pages/game-profile/game-profile';
import { CompanyProfile } from './pages/company/company';

export const routes: Routes = [
  { path: '', component: Home, title: 'EpaKeRompimo | Compará precios de juegos' },
  { path: 'game/:title', component: GameProfile },
  { path: 'company/:id', component: CompanyProfile },
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
  {
    path: 'favoritos',
    title: 'Mis favoritos | EpaKeRompimo',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/favorites/favorites').then((m) => m.Favorites),
  },
    {
    path: 'verificar-email',
    title: 'Confirmar mail | EpaKeRompimo',
    loadComponent: () => import('./pages/verify-email/verify-email').then((m) => m.VerifyEmail),
  },
    {
    path: 'desvincular-email',
    title: 'Desvincular mail | EpaKeRompimo',
    loadComponent: () => import('./pages/unlink-email/unlink-email').then((m) => m.UnlinkEmail),
  },
  {
    path: 'admin',
    title: 'Panel de administración | EpaKeRompimo',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin/admin').then((m) => m.Admin),
  },

  { path: '**', redirectTo: '' },
];
