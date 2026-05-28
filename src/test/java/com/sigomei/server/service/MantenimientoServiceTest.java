package com.sigomei.server.service;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sigomei.server.model.Equipo;
import com.sigomei.server.model.OrdenMantenimiento;
import com.sigomei.server.model.Tecnico;
import com.sigomei.server.persistence.DatabaseSchema;
import com.sigomei.server.persistence.MantenimientoRepository;
import com.sigomei.server.persistence.MantenimientoRepositoryJDBC;
import com.sigomei.server.service.exception.BusinessRuleException;

public class MantenimientoServiceTest {

    private MantenimientoService service;
    private MantenimientoRepository repository;
    private static final String H2_URL = "jdbc:h2:mem:sigomei_test;DB_CLOSE_DELAY=-1";

    @BeforeEach
    void setUp() throws Exception {
        DatabaseSchema.createSchema(H2_URL);
        repository = new MantenimientoRepositoryJDBC(H2_URL);
        service = new MantenimientoServiceImpl(repository);
    }

    @Test
    @DisplayName("RN-01: (Positivo) Programar orden con especialidad correcta")
    void testRN01_Positivo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        
        OrdenMantenimiento orden = createBaseOrden(eqId, tecId);
        assertDoesNotThrow(() -> service.programarOrden(orden));
    }

    @Test
    @DisplayName("RN-01: (Negativo) Error si especialidad no coincide con tipo de equipo")
    void testRN01_Negativo() {
        Integer eqId = seedEquipo("Eléctrico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        
        OrdenMantenimiento orden = createBaseOrden(eqId, tecId);
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.programarOrden(orden));
        assertEquals("RN-01", ex.getRuleCode());
    }

    @Test
    @DisplayName("RN-02: (Positivo) Equipo con una sola orden activa en una fecha")
    void testRN02_Positivo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        
        OrdenMantenimiento orden = createBaseOrden(eqId, tecId);
        assertDoesNotThrow(() -> service.programarOrden(orden));
    }

    @Test
    @DisplayName("RN-02: (Negativo) Error si equipo ya tiene orden activa en la fecha")
    void testRN02_Negativo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        
        OrdenMantenimiento orden1 = createBaseOrden(eqId, tecId);
        service.programarOrden(orden1);
        
        OrdenMantenimiento orden2 = createBaseOrden(eqId, tecId);
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.programarOrden(orden2));
        assertEquals("RN-02", ex.getRuleCode());
    }

    @Test
    @DisplayName("RN-03: (Positivo) Asignar técnico con estatus Activo")
    void testRN03_Positivo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        OrdenMantenimiento orden = createBaseOrden(eqId, tecId);
        assertDoesNotThrow(() -> service.programarOrden(orden));
    }

    @Test
    @DisplayName("RN-03: (Negativo) Error si el técnico está Inactivo")
    void testRN03_Negativo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Inactivo");
        OrdenMantenimiento orden = createBaseOrden(eqId, tecId);
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.programarOrden(orden));
        assertEquals("RN-03", ex.getRuleCode());
    }

    @Test
    @DisplayName("RN-04: (Positivo) Eliminar técnico sin órdenes")
    void testRN04_Positivo() {
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        assertDoesNotThrow(() -> service.eliminarTecnico(tecId));
    }

    @Test
    @DisplayName("RN-04: (Negativo) Error al eliminar técnico con órdenes registradas")
    void testRN04_Negativo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        service.programarOrden(createBaseOrden(eqId, tecId));
        
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.eliminarTecnico(tecId));
        assertEquals("RN-04", ex.getRuleCode());
    }

    @Test
    @DisplayName("RN-05: (Positivo) Fechas consistentes: cierre >= inicio >= prog")
    void testRN05_Positivo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        Integer ordId = service.programarOrden(createBaseOrden(eqId, tecId));
        
        OrdenMantenimiento orden = new OrdenMantenimiento();
        orden.setIdOrden(ordId);
        orden.setFechaInicio(LocalDate.of(2026, 5, 21));
        orden.setFechaCierre(LocalDate.of(2026, 5, 22));
        orden.setCostoReal(500.0);
        
        assertDoesNotThrow(() -> service.finalizarOrden(orden));
    }

    @Test
    @DisplayName("RN-05: (Negativo) Error si fecha_cierre < fecha_inicio")
    void testRN05_Negativo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        Integer ordId = service.programarOrden(createBaseOrden(eqId, tecId));

        OrdenMantenimiento orden = new OrdenMantenimiento();
        orden.setIdOrden(ordId);
        orden.setFechaInicio(LocalDate.of(2026, 5, 22));
        orden.setFechaCierre(LocalDate.of(2026, 5, 20));
        orden.setCostoReal(500.0);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.finalizarOrden(orden));
        assertEquals("RN-05", ex.getRuleCode());
    }

    @Test
    @DisplayName("RN-06: (Positivo) Orden Finalizada tiene costo_real y fecha_cierre")
    void testRN06_Positivo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        Integer ordId = service.programarOrden(createBaseOrden(eqId, tecId));
        
        OrdenMantenimiento orden = new OrdenMantenimiento();
        orden.setIdOrden(ordId);
        orden.setCostoReal(1500.0);
        orden.setFechaInicio(LocalDate.now());
        orden.setFechaCierre(LocalDate.now());
        
        assertDoesNotThrow(() -> service.finalizarOrden(orden));
    }

    @Test
    @DisplayName("RN-06: (Negativo) Error al finalizar sin registrar costo_real")
    void testRN06_Negativo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        Integer ordId = service.programarOrden(createBaseOrden(eqId, tecId));

        OrdenMantenimiento orden = new OrdenMantenimiento();
        orden.setIdOrden(ordId);
        orden.setCostoReal(null);
        
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.finalizarOrden(orden));
        assertEquals("RN-06", ex.getRuleCode());
    }

    @Test
    @DisplayName("RN-07: (Positivo) Equipo de criticidad Alta con técnico nivel III")
    void testRN07_Positivo() {
        Integer eqId = seedEquipo("Eléctrico", "Alta");
        Integer tecId = seedTecnico("Eléctrico", "III", "Activo");
        OrdenMantenimiento orden = createBaseOrden(eqId, tecId);
        assertDoesNotThrow(() -> service.programarOrden(orden));
    }

    @Test
    @DisplayName("RN-07: (Negativo) Error si equipo Alta criticidad tiene técnico nivel I")
    void testRN07_Negativo() {
        Integer eqId = seedEquipo("Eléctrico", "Alta");
        Integer tecId = seedTecnico("Eléctrico", "I", "Activo");
        OrdenMantenimiento orden = createBaseOrden(eqId, tecId);
        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.programarOrden(orden));
        assertEquals("RN-07", ex.getRuleCode());
    }

    @Test
    @DisplayName("RN-08: (Positivo) Transición Programada -> En ejecución")
    void testRN08_Positivo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        Integer ordId = service.programarOrden(createBaseOrden(eqId, tecId));
        
        assertDoesNotThrow(() -> service.actualizarEstadoOrden(ordId, "En ejecución"));
    }

    @Test
    @DisplayName("RN-08: (Negativo) Error transición inválida Programada -> Finalizada")
    void testRN08_Negativo() {
        Integer eqId = seedEquipo("Mecánico", "Baja");
        Integer tecId = seedTecnico("Mecánico", "I", "Activo");
        Integer ordId = service.programarOrden(createBaseOrden(eqId, tecId));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> service.actualizarEstadoOrden(ordId, "Finalizada"));
        assertEquals("RN-08", ex.getRuleCode());
    }

    private Integer seedEquipo(String tipo, String criticidad) {
        Equipo e = new Equipo();
        e.setNombre("Eq Test");
        e.setTipo(tipo);
        e.setMarca("Generic");
        e.setModelo("X");
        e.setNumeroSerie("SN-" + System.nanoTime());
        e.setUbicacionPlanta("P1");
        e.setFechaInstalacion(LocalDate.now());
        e.setEstadoOperativo("Operativo");
        e.setCriticidad(criticidad);
        return repository.saveEquipo(e);
    }

    private Integer seedTecnico(String especialidad, String nivel, String estatus) {
        Tecnico t = new Tecnico();
        t.setNombreCompleto("Tec Test");
        // RFC de 13 caracteres: 3 letras + 10 dígitos del nanoTime recortado
        String nano = String.valueOf(System.nanoTime());
        t.setRfc("TEC" + nano.substring(nano.length() - 10));
        t.setTelefono("123");
        t.setCorreo("t@test.com");
        t.setEspecialidad(especialidad);
        t.setNivelCertificacion(nivel);
        t.setFechaIngreso(LocalDate.now());
        t.setEstatus(estatus);
        return repository.saveTecnico(t);
    }

    private OrdenMantenimiento createBaseOrden(Integer eqId, Integer tecId) {
        OrdenMantenimiento orden = new OrdenMantenimiento();
        orden.setIdEquipo(eqId);
        orden.setIdTecnico(tecId);
        orden.setFechaProgramada(LocalDate.now());
        orden.setTipoMantenimiento("Preventivo");
        orden.setCostoEstimado(100.0);
        orden.setDescripcionTrabajo("Test");
        return orden;
    }
}
