package com.example.gestionacademica.services;

import com.example.gestionacademica.entities.Carrera;
import com.example.gestionacademica.entities.TipoDocumento;
import com.example.gestionacademica.repositories.CarreraRepository;
import com.example.gestionacademica.repositories.TipoDocumentoRepository;
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
