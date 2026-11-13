// Lo que devuelve el backend (UserResponse)
export interface UserResponse {
  id: number;
  username: string;
  icon: string | null;
}

// POST /api/users/login
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  idUsuario: number;
  username: string;
  icon: string | null;
}

// POST /api/users
export interface UserRequest {
  username: string;
  password: string;
  icon?: string | null;
}

// PUT /api/users/{id}  (si no se manda password, el backend conserva la actual)
export interface UserUpdateRequest {
  username: string;
  password?: string;
  icon?: string | null;
}

// Lo que guardamos en el navegador mientras la persona está logueada
export interface Session {
  token: string;
  id: number;
  username: string;
  icon: string | null;
}

export interface UserResponse {
  id: number;
  username: string;
  icon: string | null;
  email: string | null;
  emailVerified: boolean;
}

export interface UserRequest {
  username: string;
  password: string;
  icon?: string | null;
  email?: string | null;
}

export interface UserUpdateRequest {
  username: string;
  password?: string;
  icon?: string | null;
  email?: string | null;
}
