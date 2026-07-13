/**
 * =============================================================================
 * MODELOS DE AUTENTICACIÓN
 * =============================================================================
 * 
 * Estos son los tipos/interfaces que definen la estructura de datos
 * usada en el flujo de autenticación.
 */

/**
 * =============================================================================
 * REQUEST DTOs (datos que enviamos al backend)
 * =============================================================================
 */

/**
 * Credenciales de login.
 * 
 * ¿POR QUÉ un DTO específico?
 * - Separación de concerns: el modelo de dominio puede ser diferente
 * - Validación: podemos anotar estos campos con validaciones
 * - Seguridad: no enviamos datos innecesarios (como id, roles, etc.)
 */
export interface LoginRequest {
  /** Email del usuario (único en el sistema) */
  email: string;
  
  /** Contraseña (nunca se guarda ni se logs) */
  password: string;
}

/**
 * Credenciales de registro.
 * 
 * ¿POR QUÉ separarlo de LoginRequest?
 * - El registro puede tener más campos (nombre, apellido, etc.)
 * - Validaciones diferentes (email único, etc.)
 */
export interface RegisterRequest {
  email: string;
  password: string;
  
  // =======================================================================
  // CAMPOS DE USUARIO
  // =======================================================================
  nombre: string;
  apellido: string;
  numeroDocumento: string;
  idTipoDocumento: number;
  
  /**
   * Email personal opcional.
   * Algunos sistemas piden email institucional Y personal.
   */
  emailPersonal?: string;
}


/**
 * =============================================================================
 * RESPONSE DTOs (datos que recibimos del backend)
 * =============================================================================
 */

/**
 * Respuesta de login/refresh exitoso.
 * 
 * ESTRUCTURA DEL JWT (JSON Web Token)
 * ------------------------------------
 * El accessToken es un string con 3 partes separadas por '.':
 * 
 * eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJ...
 * |_______| |______________________________| |________|
 *    ①              ②                       ③
 * 
 * ① Header: metadata del token
 *    - alg: algoritmo de firma (HS256)
 *    - typ: tipo (JWT)
 * 
 * ② Payload: los datos (claims)
 *    - sub: subject (ID del usuario)
 *    - email: email del usuario
 *    - roles: array de roles ['ADMIN', 'DOCENTE']
 *    - iat: issued at (timestamp de creación)
 *    - exp: expiration (timestamp de expiración)
 * 
 * ③ Signature: firma digital
 *    - Verifica que el token no fue alterado
 *    - Solo el backend puede generarla (tiene la clave secreta)
 * 
 * 
 * ¿POR QUÉ EL TOKEN ES UN STRING Y NO UN OBJETO?
 * -----------------------------------------------
 * El token viaja en headers HTTP.
 * Los headers son strings, por eso el JWT se codifica como string.
 * 
 * Para leerlo, necesitas decodificarlo (no desencriptar):
 * - No necesitas la clave secreta para leerlo
 * - Solo el backend puede verificar la firma
 */
export interface AuthResponse {
  /**
   * El JWT Access Token.
   * 
   * CONTENIDO:
   * - userId: en el claim "sub"
   * - email: en el claim "email"
   * - roles: en el claim "roles"
   * 
   * DURACIÓN: 15 minutos (900 segundos)
   * 
   * USO:
   * - Se guarda en MEMORIA (signal) en AuthService
   * - Se envía en el header Authorization: Bearer <token>
   * - Se usa para autenticarse en cada request
   */
  accessToken: string;
  
  /**
   * Tipo de token.
   * Siempre será "Bearer" (estándar OAuth 2.0).
   * 
   * ¿POR QUÉ "Bearer"?
   * El que "porta" (bears) el token tiene acceso.
   * anyone con el token puede usarlo.
   */
  tokenType: 'Bearer';
  
  /**
   * Tiempo hasta expiración en SEGUNDOS.
   * 
   * Ejemplo: 900 = 15 minutos
   * 
   * USO:
   * - Para calcular cuándo hacer auto-refresh
   * - Para mostrar countdown al usuario
   * - Para verificar si el token está por expirar
   */
  expiresIn: number;
}


/**
 * =============================================================================
 * MODELOS DE USUARIO
 * =============================================================================
 */

/**
 * Roles disponibles en el sistema.
 * 
 * ¿POR QUÉ ENUM?
 * - TypeScript lo usa para autocompletado
 * - Evitamos typos (escribir 'ADMON' vs 'ADMIN')
 * - El backend también puede usar enum para validar
 */
export enum RolNombre {
  ADMIN = 'ADMIN',
  DOCENTE = 'DOCENTE',
  ESTUDIANTE = 'ESTUDIANTE'
}

/**
 * Usuario del sistema.
 * 
 * ¿POR QUÉ este modelo en el frontend?
 * - Para mostrar información del usuario en la UI
 * - Para hacer display del perfil
 * - Para cachear datos del usuario (evitar requests repetidas)
 */
export interface Usuario {
  idUsuario: number;
  nombre: string;
  apellido: string;
  email: string;
  emailPersonal?: string;
  numeroDocumento: string;
  estado: boolean;
  roles: Rol[];
  
  // Campos opcionales que pueden venir del backend
  tipoDocumento?: {
    idTipoDocumento: number;
    nombre: string;
  };
  
  fechaCreacion?: string;
  fechaBaja?: string;
}

/**
 * Rol de un usuario.
 */
export interface Rol {
  idRol: number;
  nombre: RolNombre;
}


/**
 * =============================================================================
 * TIPOS AUXILIARES
 * =============================================================================
 */

/**
 * Estado de la autenticación.
 * Útil para manejar estados de carga, éxito, error en la UI.
 */
export type AuthStatus = 
  | 'idle'      // Inicial, sin verificar
  | 'loading'   // Verificando con el backend
  | 'authenticated'  // Usuario logueado
  | 'unauthenticated';  // No logueado

/**
 * Error de autenticación.
 * 
 * El backend puede retornar diferentes códigos de error.
 */
export interface AuthError {
  /** Código de error para manejar programáticamente */
  code: string;
  
  /** Mensaje para mostrar al usuario */
  message: string;
  
  /** Estado HTTP original */
  status: number;
}

/**
 * Configuración de redirect después del login.
 * 
 * ¿POR QUÉ NECESITAMOS ESTO?
 * ----------------------------
 * Cuando un usuario intenta acceder a /admin/dashboard sin estar logueado,
 * el guard lo redirige a /login?returnUrl=/admin/dashboard.
 * 
 * Después de hacer login exitoso, queremos redirigirlo a /admin/dashboard,
 * no siempre al home.
 */
export interface RedirectConfig {
  /** URL a la que el usuario intentaba acceder */
  returnUrl: string;
  
  /** URL fallback si no hay returnUrl */
  defaultUrl: string;
}


/**
 * =============================================================================
 * CONSTANTES DE AUTENTICACIÓN
 * =============================================================================
 */

/**
 * Duración del Access Token en segundos.
 * 15 minutos = 15 * 60 = 900 segundos
 */
export const ACCESS_TOKEN_DURATION = 900;

/**
 * Tiempo antes de la expiración para hacer refresh (segundos).
 * Refresh 1 minuto antes de que expire.
 */
export const REFRESH_BEFORE_EXPIRY = 60;

/**
 * Keys usadas en sessionStorage (para returnUrl principalmente).
 */
export const SESSION_KEYS = {
  RETURN_URL: 'auth_return_url'
} as const;

/**
 * =============================================================================
 * FORGOT / RESET PASSWORD DTOs
 * =============================================================================
 */

/**
 * Solicitud de restablecimiento de contraseña.
 */
export interface ForgotPasswordRequest {
  email: string;
}

/**
 * Restablecimiento de contraseña con token.
 */
export interface ResetPasswordRequest {
  token: string;
  nuevaPassword: string;
}

/**
 * Respuesta genérica del backend con mensaje.
 */
export interface MessageResponse {
  message: string;
}

// ─── Perfil ──────────────────────────────────────────────

/**
 * Respuesta del perfil del usuario autenticado.
 */
export interface PerfilResponse {
  idUsuario: number;
  nombre: string;
  apellido: string;
  email: string;
  emailPersonal: string | null;
  numeroDocumento: string;
  estado: boolean;
  tipoDocumento: string | null;
  roles: string[];
}

/**
 * Solicitud de actualización de perfil.
 */
export interface ActualizarPerfilRequest {
  nombre?: string;
  apellido?: string;
  emailPersonal?: string;
}

/**
 * Solicitud de cambio de contraseña.
 */
export interface CambiarPasswordRequest {
  passwordActual: string;
  nuevaPassword: string;
}

export type CambiarPasswordResponse = MessageResponse;