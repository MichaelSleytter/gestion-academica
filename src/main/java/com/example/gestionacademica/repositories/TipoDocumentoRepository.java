// TipoDocumentoRepository.java
package com.example.gestionacademica.repositories;
import com.example.gestionacademica.entities.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Integer> {
}