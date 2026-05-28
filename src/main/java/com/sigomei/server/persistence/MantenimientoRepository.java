package com.sigomei.server.persistence;

import java.util.List;

import com.sigomei.server.model.Equipo;
import com.sigomei.server.model.OrdenMantenimiento;
import com.sigomei.server.model.Tecnico;

public interface MantenimientoRepository {
    Integer saveEquipo(Equipo equipo);

    void deleteEquipo(Integer id);

    Equipo findEquipoById(Integer id);

    Integer saveTecnico(Tecnico tecnico);

    void deleteTecnico(Integer id);

    Tecnico findTecnicoById(Integer id);

    Integer saveOrden(OrdenMantenimiento orden);

    void deleteOrden(Integer id);

    void updateOrden(OrdenMantenimiento orden);

    void updateEquipo(Equipo equipo);

    void updateTecnico(Tecnico tecnico);

    OrdenMantenimiento findOrdenById(Integer id);

    List<OrdenMantenimiento> findAllOrdenes();

    List<Equipo> findAllEquipos();

    List<Tecnico> findAllTecnicos();

    List<OrdenMantenimiento> findOrdenesByEquipo(Integer idEquipo);

    List<OrdenMantenimiento> findOrdenesByTecnico(Integer idTecnico);
}
