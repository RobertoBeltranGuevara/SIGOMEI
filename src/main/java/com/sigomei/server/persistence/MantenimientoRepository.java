package com.sigomei.server.persistence;

import com.sigomei.server.model.Equipo;
import com.sigomei.server.model.OrdenMantenimiento;
import com.sigomei.server.model.Tecnico;
import java.util.List;

public interface MantenimientoRepository {
    Integer saveEquipo(Equipo equipo);

    Equipo findEquipoById(Integer id);

    Integer saveTecnico(Tecnico tecnico);

    void deleteTecnico(Integer id);

    Tecnico findTecnicoById(Integer id);

    Integer saveOrden(OrdenMantenimiento orden);

    void updateOrden(OrdenMantenimiento orden);

    OrdenMantenimiento findOrdenById(Integer id);

    List<OrdenMantenimiento> findAllOrdenes();

    List<Equipo> findAllEquipos();

    List<Tecnico> findAllTecnicos();

    List<OrdenMantenimiento> findOrdenesByEquipo(Integer idEquipo);

    List<OrdenMantenimiento> findOrdenesByTecnico(Integer idTecnico);
}
