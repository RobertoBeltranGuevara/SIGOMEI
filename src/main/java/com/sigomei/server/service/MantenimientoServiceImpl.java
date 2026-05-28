package com.sigomei.server.service;

import java.util.List;

import com.sigomei.server.model.Equipo;
import com.sigomei.server.model.OrdenMantenimiento;
import com.sigomei.server.model.Tecnico;
import com.sigomei.server.persistence.MantenimientoRepository;
import com.sigomei.server.service.exception.BusinessRuleException;

public class MantenimientoServiceImpl implements MantenimientoService {

    private final MantenimientoRepository repository;

    public MantenimientoServiceImpl(MantenimientoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Integer crearEquipo(Equipo equipo) {
        return repository.saveEquipo(equipo);
    }

    @Override
    public Equipo obtenerEquipo(Integer id) {
        return repository.findEquipoById(id);
    }

    @Override
    public List<Equipo> listarEquipos() {
        return repository.findAllEquipos();
    }

    @Override
    public Integer crearTecnico(Tecnico tecnico) {
        return repository.saveTecnico(tecnico);
    }

    @Override
    public void eliminarTecnico(Integer id) {
        // RN-04: No se permite eliminar un Técnico con órdenes registradas.
        List<OrdenMantenimiento> ordenes = repository.findOrdenesByTecnico(id);
        if (!ordenes.isEmpty()) {
            throw new BusinessRuleException("RN-04",
                    "No se puede eliminar el técnico porque tiene órdenes registradas.");
        }
        repository.deleteTecnico(id);
    }

    @Override
    public Tecnico obtenerTecnico(Integer id) {
        return repository.findTecnicoById(id);
    }

    @Override
    public List<Tecnico> listarTecnicos() {
        return repository.findAllTecnicos();
    }

    @Override
    public Integer programarOrden(OrdenMantenimiento orden) {
        Equipo equipo = repository.findEquipoById(orden.getIdEquipo());
        Tecnico tecnico = repository.findTecnicoById(orden.getIdTecnico());

        if (equipo == null || tecnico == null) {
            throw new BusinessRuleException("ERR", "Equipo o Técnico no encontrado.");
        }

        // RN-01: La especialidad del técnico debe coincidir con el tipo del equipo
        if (!tecnico.getEspecialidad().equals(equipo.getTipo())) {
            throw new BusinessRuleException("RN-01", "La especialidad del técnico no coincide con el tipo de equipo.");
        }

        // RN-02: Un equipo no puede tener dos órdenes activas en la misma fecha
        List<OrdenMantenimiento> activas = repository.findOrdenesByEquipo(orden.getIdEquipo());
        for (OrdenMantenimiento o : activas) {
            if (o.getFechaProgramada().equals(orden.getFechaProgramada())) {
                throw new BusinessRuleException("RN-02", "El equipo ya tiene una orden activa para esta fecha.");
            }
        }

        // RN-03: Un técnico con estatus Inactivo no puede ser asignado
        if ("Inactivo".equals(tecnico.getEstatus())) {
            throw new BusinessRuleException("RN-03", "El técnico asignado está inactivo.");
        }

        // RN-07: Los equipos de criticidad Alta requieren técnicos de certificación II
        // o III
        if ("Alta".equals(equipo.getCriticidad())) {
            if ("I".equals(tecnico.getNivelCertificacion())) {
                throw new BusinessRuleException("RN-07",
                        "Equipos de criticidad Alta requieren técnicos nivel II o III.");
            }
        }

        orden.setEstadoOrden("Programada");
        return repository.saveOrden(orden);
    }

    @Override
    public void actualizarEstadoOrden(Integer idOrden, String nuevoEstado) {
        OrdenMantenimiento orden = repository.findOrdenById(idOrden);
        if (orden == null)
            throw new BusinessRuleException("ERR", "Orden no encontrada.");

        // RN-08: Transiciones permitidas: Programada -> En ejecución -> Finalizada.
        String actual = orden.getEstadoOrden();
        if ("Programada".equals(actual) && "Finalizada".equals(nuevoEstado)) {
            throw new BusinessRuleException("RN-08", "Transición inválida: debe pasar por 'En ejecución'.");
        }

        orden.setEstadoOrden(nuevoEstado);
        repository.updateOrden(orden);
    }

    @Override
    public void finalizarOrden(OrdenMantenimiento ordenFinalizada) {
        OrdenMantenimiento existente = repository.findOrdenById(ordenFinalizada.getIdOrden());
        if (existente == null)
            throw new BusinessRuleException("ERR", "Orden no encontrada.");

        // RN-05: fecha_cierre >= fecha_inicio >= fecha_programada
        if (ordenFinalizada.getFechaCierre() != null && ordenFinalizada.getFechaInicio() != null) {
            if (ordenFinalizada.getFechaCierre().isBefore(ordenFinalizada.getFechaInicio())) {
                throw new BusinessRuleException("RN-05", "La fecha de cierre no puede ser anterior a la de inicio.");
            }
        }

        // RN-06: Solo las órdenes en estado Finalizada deben tener costo_real y
        // fecha_cierre
        if (ordenFinalizada.getCostoReal() == null) {
            throw new BusinessRuleException("RN-06", "El costo real es obligatorio para finalizar la orden.");
        }

        ordenFinalizada.setEstadoOrden("Finalizada");
        repository.updateOrden(ordenFinalizada);
    }

    @Override
    public List<OrdenMantenimiento> listarOrdenes() {
        return repository.findAllOrdenes();
    }
}
