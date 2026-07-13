package com.example.gestionacademica.contenido.service;

import com.example.gestionacademica.contenido.domain.CursoContenido;
import com.example.gestionacademica.contenido.dto.CursoContenidoRequest;
import com.example.gestionacademica.contenido.repository.CursoContenidoRepository;
import com.example.gestionacademica.docentes.repository.DocenteSeccionRepository;
import com.example.gestionacademica.matriculas.repository.MatriculaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio de negocio para la gestión de contenido de cursos.
 */
@Service
public class CursoContenidoService {

    private static final String STORAGE_BUCKET = "curso-contenido";

    private final CursoContenidoRepository repository;
    private final DocenteSeccionRepository docenteSeccionRepository;
    private final MatriculaRepository matriculaRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String storageBaseUrl;
    private final String storageAnonKey;

    public CursoContenidoService(
            CursoContenidoRepository repository,
            DocenteSeccionRepository docenteSeccionRepository,
            MatriculaRepository matriculaRepository,
            ObjectMapper objectMapper,
            @Value("${app.insforge.storage.base-url:https://3auan78u.us-east.insforge.app}") String storageBaseUrl,
            @Value("${app.insforge.storage.anon-key:}") String storageAnonKey) {
        this.repository = repository;
        this.docenteSeccionRepository = docenteSeccionRepository;
        this.matriculaRepository = matriculaRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.storageBaseUrl = storageBaseUrl;
        this.storageAnonKey = storageAnonKey;
    }

    /**
     * Lista el contenido activo de una sección.
     *
     * @param idSeccion identificador de la sección
     * @return contenido activo ordenado por fecha descendente
     */
    public List<CursoContenido> listarPorSeccion(Integer idSeccion, Authentication authentication) {
        requireCanList(idSeccion, authentication);
        return repository.findByIdSeccionAndActivoTrueOrderBySemanaAscFechaSubidaDesc(idSeccion);
    }

    /**
     * Guarda la metadata de un archivo subido a InsForge Storage.
     *
     * @param request metadata del contenido
     * @return contenido guardado con fecha y activo asignados
     */
    @Transactional
    public CursoContenido guardar(CursoContenidoRequest request, Authentication authentication) {
        requireCanWrite(request.idSeccion(), authentication);
        validateSemana(request.semana());
        validateStorageMetadata(request);

        CursoContenido contenido = new CursoContenido();
        contenido.setIdSeccion(request.idSeccion());
        contenido.setNombreOriginal(request.nombreOriginal());
        contenido.setKey(request.key());
        contenido.setUrl(request.url());
        contenido.setMimeType(request.mimeType());
        contenido.setSizeBytes(request.sizeBytes());
        contenido.setSemana(request.semana());
        contenido.setSubidoPor(currentUserId(authentication));
        contenido.setFechaSubida(LocalDateTime.now());
        contenido.setActivo(true);
        return repository.save(contenido);
    }

    @Transactional
    public CursoContenido subir(
            MultipartFile file,
            Integer idSeccion,
            Integer semana,
            Authentication authentication) {
        requireCanWrite(idSeccion, authentication);
        validateSemana(semana);
        StorageUpload uploaded = uploadToStorage(file, idSeccion);

        return guardar(new CursoContenidoRequest(
                idSeccion,
                file.getOriginalFilename(),
                uploaded.key(),
                uploaded.url(),
                uploaded.mimeType(),
                uploaded.size(),
                semana), authentication);
    }

    /**
     * Eliminación lógica (soft delete) de un contenido.
     *
     * @param idContenido identificador del contenido
     */
    @Transactional
    public void eliminar(Long idContenido, Authentication authentication) {
        CursoContenido contenido = repository.findById(idContenido)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado con ID: " + idContenido));
        requireCanWrite(contenido.getIdSeccion(), authentication);
        contenido.setActivo(false);
        repository.save(contenido);
    }

    private void requireCanList(Integer idSeccion, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (hasRole(authentication, "ADMIN")
                || isAssignedDocente(userId, idSeccion)
                || matriculaRepository.existsByEstudiante_IdUsuarioAndSeccion_IdSeccion(userId, idSeccion)) {
            return;
        }
        throw new AccessDeniedException("No tiene permiso para ver el contenido de esta sección.");
    }

    private void requireCanWrite(Integer idSeccion, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (hasRole(authentication, "ADMIN") || isAssignedDocente(userId, idSeccion)) {
            return;
        }
        throw new AccessDeniedException("No tiene permiso para modificar el contenido de esta sección.");
    }

    private boolean isAssignedDocente(Integer userId, Integer idSeccion) {
        return docenteSeccionRepository.existsByDocente_IdUsuarioAndSeccion_IdSeccion(userId, idSeccion);
    }

    private Integer currentUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.example.gestionacademica.auth.domain.Usuario usuario) {
            return usuario.getIdUsuario();
        }
        throw new AccessDeniedException("Usuario autenticado inválido.");
    }

    private boolean hasRole(Authentication authentication, String role) {
        String authority = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    private void validateSemana(Integer semana) {
        if (semana == null || semana < 1 || semana > 18) {
            throw new RuntimeException("La semana debe estar entre 1 y 18.");
        }
    }

    private void validateStorageMetadata(CursoContenidoRequest request) {
        String expectedPrefix = "seccion/" + request.idSeccion() + "/";
        if (!request.key().startsWith(expectedPrefix)) {
            throw new RuntimeException("La ruta del archivo no corresponde a la sección.");
        }

        if (!expectedStorageUrl(request.key()).equals(request.url())) {
            throw new RuntimeException("La URL del archivo no corresponde al objeto subido.");
        }
    }

    private String expectedStorageUrl(String key) {
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8).replace("+", "%20");
        return storageBaseUrl + "/api/storage/buckets/" + STORAGE_BUCKET + "/objects/" + encodedKey;
    }

    private StorageUpload uploadToStorage(MultipartFile file, Integer idSeccion) {
        if (storageAnonKey.isBlank()) {
            throw new RuntimeException("InsForge Storage no está configurado en el backend.");
        }

        String key = "seccion/" + idSeccion + "/" + System.currentTimeMillis() + "-" + safeName(file);

        try {
            String boundary = "----gestion-academica-" + UUID.randomUUID();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(expectedStorageUrl(key)))
                    .header("Authorization", "Bearer " + storageAnonKey)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(multipartBody(boundary, file)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("No se pudo subir el archivo a Storage.");
            }

            JsonNode json = objectMapper.readTree(response.body());
            return new StorageUpload(
                    json.path("key").asText(key),
                    json.path("url").asText(expectedStorageUrl(key)),
                    json.path("mimeType").asText(contentType(file)),
                    json.path("size").asLong(file.getSize()));
        } catch (IOException exception) {
            throw new RuntimeException("No se pudo preparar el archivo para Storage.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("La subida a Storage fue interrumpida.", exception);
        }
    }

    private byte[] multipartBody(String boundary, MultipartFile file) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + safeName(file) + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Type: " + contentType(file) + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.write(file.getBytes());
        body.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return body.toByteArray();
    }

    private String safeName(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            return "archivo";
        }
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String contentType(MultipartFile file) {
        return file.getContentType() == null ? "application/octet-stream" : file.getContentType();
    }

    private record StorageUpload(String key, String url, String mimeType, long size) {}
}
