package com.sigomei.server.service;

import java.util.List;

import com.sigomei.server.model.Equipo;
import com.sigomei.server.model.OrdenMantenimiento;
import com.sigomei.server.model.Tecnico;

public interface MantenimientoService {
    // Equipos
    Integer crearEquipo(Equipo equipo);

    Equipo obtenerEquipo(Integer id);

    List<Equipo> listarEquipos();

    void eliminarEquipo(Integer id);

    void actualizarEquipo(Equipo equipo);

    // Técnicos
    Integer crearTecnico(Tecnico tecnico);

    List<Tecnico> listarTecnicos();

    void eliminarTecnico(Integer id);

    void actualizarTecnico(Tecnico tecnico);

    Tecnico obtenerTecnico(Integer id);

    // Órdenes
    Integer programarOrden(OrdenMantenimiento orden);

    void actualizarEstadoOrden(Integer idOrden, String nuevoEstado);

    void finalizarOrden(OrdenMantenimiento ordenFinalizada);

    void actualizarOrden(OrdenMantenimiento orden);

    void eliminarOrden(Integer id);

    List<OrdenMantenimiento> listarOrdenes();

    OrdenMantenimiento obtenerOrden(Integer id);
}
