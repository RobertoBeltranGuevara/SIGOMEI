package com.sigomei.server.service;

import com.sigomei.server.model.Equipo;
import com.sigomei.server.model.OrdenMantenimiento;
import com.sigomei.server.model.Tecnico;
import java.util.List;

public interface MantenimientoService {
    // Equipos
    Integer crearEquipo(Equipo equipo);

    Equipo obtenerEquipo(Integer id);

    List<Equipo> listarEquipos();

    // Técnicos
    Integer crearTecnico(Tecnico tecnico);

    List<Tecnico> listarTecnicos();

    void eliminarTecnico(Integer id);

    Tecnico obtenerTecnico(Integer id);

    // Órdenes
    Integer programarOrden(OrdenMantenimiento orden);

    void actualizarEstadoOrden(Integer idOrden, String nuevoEstado);

    void finalizarOrden(OrdenMantenimiento ordenFinalizada);

    List<OrdenMantenimiento> listarOrdenes();
}
