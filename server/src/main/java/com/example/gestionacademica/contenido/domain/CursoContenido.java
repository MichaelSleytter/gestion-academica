package com.example.gestionacademica.contenido.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un archivo de contenido subido para una sección de curso.
 * Los archivos se almacenan en InsForge Storage (bucket: curso-contenido)
 * y esta tabla guarda la metadata asociada.
 */
@Entity
@Table(name = "curso_contenido")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CursoContenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenido")
    private Long idContenido;

    @Column(name = "id_seccion", nullable = false)
    private Integer idSeccion;

    @Column(name = "nombre_original", nullable = false, length = 500)
    private String nombreOriginal;

    @Column(name = "\"key\"", nullable = false, length = 500)
    private String key;

    @Column(name = "url", nullable = false, length = 1000)
    private String url;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "semana")
    private Integer semana = 1;

    @Column(name = "subido_por")
    private Integer subidoPor;

    @Column(name = "fecha_subida")
    private LocalDateTime fechaSubida;

    @Column(name = "activo")
    private Boolean activo = true;
}
