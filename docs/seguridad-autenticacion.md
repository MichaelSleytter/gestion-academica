# Seguridad y Autenticación

## 2.3.1 Autenticación

### Login mediante usuario y contraseña

El sistema implementa un endpoint de autenticación que valida las credenciales del usuario contra la base de datos.

```
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "usuario@ejemplo.com",
  "password": "miPassword123"
}
```

**Flujo:**
1. El `AuthenticationManager` de Spring Security valida email y password
2. Se buscan los datos del usuario en la base de datos
3. Se verifica que el usuario esté activo (`estado = true`)

---

### Generación de JWT al autenticarse

Upon successful authentication, the system generates two tokens:

**Access Token (JWT)**
- Validez: 15 minutos
- Incluido en el cuerpo de la respuesta
- Contiene: `userId`, `email`, `nombre`, `apellido`, `roles`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Refresh Token (UUID)**
- Validez: 7 días
- Almacenado en la base de datos
- Guardado en una cookie `HttpOnly` para prevenir ataques XSS

---

## 2.3.2 Autorización

### Acceso a endpoints según rol

Los roles se definen mediante la relación `ManyToMany` en la entidad `Usuario`:

```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(name = "usuario_rol",
    joinColumns = @JoinColumn(name = "id_usuario"),
    inverseJoinColumns = @JoinColumn(name = "id_rol"))
private List<Rol> roles = new ArrayList<>();
```

El sistema utiliza `SimpleGrantedAuthority` para mapear los roles de la base de datos a autoridades de Spring Security:

```java
List<SimpleGrantedAuthority> authorities = roles.stream()
    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
    .toList();
```

### Anotaciones de seguridad a nivel de método

El proyecto tiene habilitada la seguridad a nivel de método mediante:

```java
@EnableMethodSecurity
```

**Nota:** Actualmente no se utilizan anotaciones `@PreAuthorize`, `@Secured` ni `@RolesAllowed` en los controllers. La autorización se maneja a nivel de endpoint en `SecurityConfig` y mediante el filtro JWT.

---

## 2.3.3 Protección de Endpoints

### Endpoints públicos

Los siguientes endpoints no requieren autenticación:

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/v1/auth/login` | POST | Login de usuario |
| `/api/v1/auth/register` | POST | Registro de nuevo usuario |
| `/api/v1/auth/refresh` | POST | Renovación de access token |
| `/api/v1/auth/logout` | POST | Cerrar sesión |
| `/swagger-ui/**` | * | Documentación API |
| `/v3/api-docs/**` | * | Especificación OpenAPI |

### Endpoints protegidos (requieren JWT)

Todos los demás endpoints de la API requieren un JWT válido en el header de autorización:

```
Authorization: Bearer <access_token>
```

| Módulo | Path Base |
|--------|-----------|
| **Carreras** | `/api/v1/carreras/**` |
| **Cursos** | `/api/v1/cursos/**` |
| **Ciclos Académicos** | `/api/v1/ciclos-academicos/**` |
| **Secciones** | `/api/v1/secciones/**` |
| **Horarios** | `/api/v1/horarios/**` |
| **Estudiantes** | `/api/v1/estudiantes/**` |
| **Docentes** | `/api/v1/docentes/**` |
| **Docentes-Secciones** | `/api/v1/docentes-secciones/**` |
| **Notas** | `/api/v1/notas/**` |
| **Evaluaciones** | `/api/v1/evaluaciones/**` |
| **Historial Académico** | `/api/v1/historial-academico/**` |
| **Matrículas** | `/api/v1/matriculas/**` |
| **Grados Académicos** | `/api/v1/grados-academicos/**` |
| **Tipos de Documento** | `/api/v1/tipos-documento/**` |
| **Usuarios** | `/api/v1/usuarios/**` |

---

## 2.3.4 Gestión de Contraseñas

### Almacenamiento en base de datos

La contraseña se almacena en la tabla `usuario` como un hash:

```java
@Column(name = "password", nullable = false, length = 255)
@JsonIgnore
private String password;
```

### Encriptación con PasswordEncoder

El sistema utiliza **BCrypt** con factor de costo 10:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```

**Características de BCrypt:**
- Hash unidireccional (no reversible)
- Factor de costo configurable (10 = ~10 iteraciones)
- Incluye "salt" automático para prevenir ataques rainbow table
- Resistente a fuerza bruta gracias a su velocidad intentionally lenta

---

## 2.3.5 Arquitectura del filtro JWT

```
Request → JwtAuthenticationFilter → SecurityContext → Controller

Flujo de validación:
1. Extraer header "Authorization: Bearer <token>"
2. Validar token (firma, expiración)
3. Extraer userId del token
4. Verificar usuario existe y está activo en BD
5. Extraer roles y crear Authentication token
6. Configurar SecurityContextHolder
```

---

## 2.3.6 Seguridad adicional implementada

| Característica | Implementación |
|----------------|----------------|
| **CSRF** | Deshabilitado (API stateless) |
| **Sesiones** | Stateless (STATELESS policy) |
| **XSS** | Cookies `HttpOnly` para refresh tokens |
| **Replay Attack** | Refresh tokens marcados como "usados" tras cada uso |
| **Revocación** | Refresh tokens revocables desde BD |