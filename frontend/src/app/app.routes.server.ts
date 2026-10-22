import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // "Mi cuenta" depende de la sesión (que vive en el navegador): no se pre-renderiza
  {
    path: 'cuenta',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
