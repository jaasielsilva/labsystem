import { Usuario } from './usuario.model';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  usuario: Usuario;
}
