package com.example.gestionacademica.catalogos.service;

import com.example.gestionacademica.catalogos.domain.Carrera;
import com.example.gestionacademica.catalogos.domain.TipoDocumento;
import com.example.gestionacademica.catalogos.repository.CarreraRepository;
import com.example.gestionacademica.catalogos.repository.TipoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final CarreraRepository carreraRepository;
    private final TipoDocumentoRepository tipoDocumentoRepository;

    public Carrera buscarCarreraPorId(Integer idCarrera) {
        return carreraRepository
            .findById(idCarrera)
            .orElseThrow(() ->
                new RuntimeException(
                    "Carrera no encontrada con ID: " + idCarrera
                )
            );
    }

    public TipoDocumento buscarTipoDocumentoPorId(Integer idTipoDocumento) {
        return tipoDocumentoRepository
            .findById(idTipoDocumento)
            .orElseThrow(() ->
                new RuntimeException(
                    "Tipo de documento no encontrado con ID: " + idTipoDocumento
                )
            );
    }
}
