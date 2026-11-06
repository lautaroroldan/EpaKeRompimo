import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Estas pantallas dependen de la sesión (que vive en el navegador): no se pre-renderizan
  {
    path: 'cuenta',
    renderMode: RenderMode.Client
  },
  {
    path: 'favoritos',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
