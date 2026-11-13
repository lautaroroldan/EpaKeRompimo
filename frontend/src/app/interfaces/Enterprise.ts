// Una empresa para mostrar en el buscador (resultado de buscar por nombre).
// El id es el de IGDB.
export interface Enterprise {
  igdbCompanyId: number;
  name: string;
  logoUrl: string | null; // logo de IGDB; null si IGDB no tiene
}
