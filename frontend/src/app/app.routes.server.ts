import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // "Mi cuenta" depende de la sesión (que vive en el navegador): no se pre-renderiza
  {
    path: 'cuenta',
    renderMode: RenderMode.Client
  },
  // El perfil de juego depende de un parámetro dinámico y de datos del backend:
  // se renderiza en el servidor por request, no se puede pre-renderizar.
  {
    path: 'game/:title',
    renderMode: RenderMode.Server
  },
  {
    path: 'company/:id',
    renderMode: RenderMode.Server
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
