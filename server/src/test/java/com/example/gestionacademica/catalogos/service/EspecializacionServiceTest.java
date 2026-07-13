package com.example.gestionacademica.catalogos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.gestionacademica.catalogos.domain.Especializacion;
import com.example.gestionacademica.catalogos.repository.EspecializacionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link EspecializacionService}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas - EspecializacionService")
class EspecializacionServiceTest {

    @Mock
    private EspecializacionRepository especializacionRepository;

    @InjectMocks
    private EspecializacionService especializacionService;

    /** Verifies that listarTodas returns all specializations. */
    @Test
    @DisplayName("listarTodas: debe retornar lista de especializaciones")
    void listarTodas_debeRetornarLista() {
        Especializacion especializacion = new Especializacion();
        when(especializacionRepository.findAll()).thenReturn(List.of(especializacion));

        List<Especializacion> resultado = especializacionService.listarTodas();

        assertThat(resultado).containsExactly(especializacion);
        verify(especializacionRepository, times(1)).findAll();
    }

    /** Verifies that crear validates non-empty names. */
    @Test
    @DisplayName("crear: debe rechazar nombre vacio")
    void crear_conNombreVacio_debeLanzarExcepcion() {
        Especializacion especializacion = new Especializacion();
        especializacion.setNombre("   ");

        assertThatThrownBy(() -> especializacionService.crear(especializacion))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("nombre");

        verify(especializacionRepository, never()).save(especializacion);
    }

    /** Verifies that crear rejects duplicate names. */
    @Test
    @DisplayName("crear: debe rechazar nombre duplicado")
    void crear_conNombreDuplicado_debeLanzarExcepcion() {
        Especializacion especializacion = new Especializacion();
        especializacion.setNombre("Matemáticas");
        when(especializacionRepository.existsByNombre("Matemáticas")).thenReturn(true);

        assertThatThrownBy(() -> especializacionService.crear(especializacion))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Matemáticas");

        verify(especializacionRepository, never()).save(especializacion);
    }

    /** Verifies that actualizar updates when the new name is unique. */
    @Test
    @DisplayName("actualizar: debe actualizar nombre cuando es unico")
    void actualizar_conNombreUnico_debeActualizar() {
        Especializacion existente = new Especializacion();
        existente.setIdEspecializacion(1);
        existente.setNombre("Historia");

        Especializacion datos = new Especializacion();
        datos.setNombre("Ciencias Sociales");

        when(especializacionRepository.findById(1)).thenReturn(Optional.of(existente));
        when(especializacionRepository.existsByNombre("Ciencias Sociales")).thenReturn(false);
        when(especializacionRepository.save(existente)).thenReturn(existente);

        Especializacion resultado = especializacionService.actualizar(1, datos);

        assertThat(resultado.getNombre()).isEqualTo("Ciencias Sociales");
        verify(especializacionRepository, times(1)).save(existente);
    }
}
