package com.sigomei.server.persistence;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.sigomei.server.model.Equipo;
import com.sigomei.server.model.OrdenMantenimiento;
import com.sigomei.server.model.Tecnico;

public class MantenimientoRepositoryJDBC implements MantenimientoRepository {
    private final String dbUrl;

    public MantenimientoRepositoryJDBC(String dbUrl) {
        this.dbUrl = dbUrl;
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void log(String msg) {
        try (PrintWriter out = new PrintWriter(new FileWriter("debug_jdbc.log", true))) {
            out.println(msg);
        } catch (Exception e) {
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, "sa", "");
    }

    @Override
    public Integer saveEquipo(Equipo equipo) {
        String sql = "INSERT INTO equipo (nombre, tipo, marca, modelo, numero_serie, ubicacion_planta, fecha_instalacion, estado_operativo, criticidad) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, equipo.getNombre());
            ps.setString(2, equipo.getTipo());
            ps.setString(3, equipo.getMarca());
            ps.setString(4, equipo.getModelo());
            ps.setString(5, equipo.getNumeroSerie());
            ps.setString(6, equipo.getUbicacionPlanta());
            ps.setDate(7, java.sql.Date.valueOf(equipo.getFechaInstalacion()));
            ps.setString(8, equipo.getEstadoOperativo());
            ps.setString(9, equipo.getCriticidad());
            int affected = ps.executeUpdate();
            log("saveEquipo affected: " + affected);
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            log("Error saveEquipo: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteEquipo(Integer id) {
        if (id == null)
            return;
        String sql = "DELETE FROM equipo WHERE id_equipo = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log("Error deleteEquipo: " + e.getMessage());
        }
    }

    @Override
    public Equipo findEquipoById(Integer id) {
        if (id == null)
            return null;
        String sql = "SELECT * FROM equipo WHERE id_equipo = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Equipo e = new Equipo();
                    e.setIdEquipo(rs.getInt("id_equipo"));
                    e.setNombre(rs.getString("nombre"));
                    e.setTipo(rs.getString("tipo"));
                    e.setMarca(rs.getString("marca"));
                    e.setModelo(rs.getString("modelo"));
                    e.setNumeroSerie(rs.getString("numero_serie"));
                    e.setUbicacionPlanta(rs.getString("ubicacion_planta"));
                    e.setFechaInstalacion(rs.getDate("fecha_instalacion").toLocalDate());
                    e.setEstadoOperativo(rs.getString("estado_operativo"));
                    e.setCriticidad(rs.getString("criticidad"));
                    return e;
                }
            }
        } catch (SQLException e) {
            log("Error findEquipo: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Integer saveTecnico(Tecnico tecnico) {
        String sql = "INSERT INTO tecnico (nombre_completo, rfc, telefono, correo, especialidad, nivel_certificacion, fecha_ingreso, estatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tecnico.getNombreCompleto());
            ps.setString(2, tecnico.getRfc());
            ps.setString(3, tecnico.getTelefono());
            ps.setString(4, tecnico.getCorreo());
            ps.setString(5, tecnico.getEspecialidad());
            ps.setString(6, tecnico.getNivelCertificacion());
            ps.setDate(7, java.sql.Date.valueOf(tecnico.getFechaIngreso()));
            ps.setString(8, tecnico.getEstatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            log("Error saveTecnico: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteTecnico(Integer id) {
        if (id == null)
            return;
        String sql = "DELETE FROM tecnico WHERE id_tecnico = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log("Error deleteTecnico: " + e.getMessage());
        }
    }

    @Override
    public Tecnico findTecnicoById(Integer id) {
        if (id == null)
            return null;
        String sql = "SELECT * FROM tecnico WHERE id_tecnico = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tecnico t = new Tecnico();
                    t.setIdTecnico(rs.getInt("id_tecnico"));
                    t.setNombreCompleto(rs.getString("nombre_completo"));
                    t.setEspecialidad(rs.getString("especialidad"));
                    t.setEstatus(rs.getString("estatus"));
                    t.setNivelCertificacion(rs.getString("nivel_certificacion"));
                    t.setRfc(rs.getString("rfc"));
                    t.setTelefono(rs.getString("telefono"));
                    t.setCorreo(rs.getString("correo"));
                    t.setFechaIngreso(rs.getDate("fecha_ingreso").toLocalDate());
                    return t;
                }
            }
        } catch (SQLException e) {
            log("Error findTecnico: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Integer saveOrden(OrdenMantenimiento orden) {
        String sql = "INSERT INTO orden_mantenimiento (id_equipo, id_tecnico, tipo_mantenimiento, fecha_programada, descripcion_trabajo, costo_estimado, estado_orden) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, orden.getIdEquipo());
            ps.setInt(2, orden.getIdTecnico());
            ps.setString(3, orden.getTipoMantenimiento());
            ps.setDate(4, java.sql.Date.valueOf(orden.getFechaProgramada()));
            ps.setString(5, orden.getDescripcionTrabajo());
            ps.setDouble(6, orden.getCostoEstimado());
            ps.setString(7, orden.getEstadoOrden());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            log("Error saveOrden: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteOrden(Integer id) {
        if (id == null)
            return;
        String sql = "DELETE FROM orden_mantenimiento WHERE id_orden = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log("Error deleteOrden: " + e.getMessage());
        }
    }

    @Override
    public void updateOrden(OrdenMantenimiento orden) {
        String sql = "UPDATE orden_mantenimiento SET id_equipo = ?, id_tecnico = ?, tipo_mantenimiento = ?, fecha_programada = ?, estado_orden = ?, fecha_inicio = ?, fecha_cierre = ?, costo_real = ?, descripcion_trabajo = ? WHERE id_orden = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orden.getIdEquipo());
            ps.setInt(2, orden.getIdTecnico());
            ps.setString(3, orden.getTipoMantenimiento());
            ps.setDate(4, java.sql.Date.valueOf(orden.getFechaProgramada()));
            ps.setString(5, orden.getEstadoOrden());
            ps.setObject(6, orden.getFechaInicio() != null ? java.sql.Date.valueOf(orden.getFechaInicio()) : null);
            ps.setObject(7, orden.getFechaCierre() != null ? java.sql.Date.valueOf(orden.getFechaCierre()) : null);
            ps.setObject(8, orden.getCostoReal());
            ps.setString(9, orden.getDescripcionTrabajo());
            ps.setInt(10, orden.getIdOrden());
            ps.executeUpdate();
        } catch (SQLException e) {
            log("Error updateOrden: " + e.getMessage());
        }
    }

    @Override
    public void updateEquipo(Equipo equipo) {
        String sql = "UPDATE equipo SET nombre = ?, tipo = ?, marca = ?, modelo = ?, numero_serie = ?, ubicacion_planta = ?, fecha_instalacion = ?, estado_operativo = ?, criticidad = ? WHERE id_equipo = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, equipo.getNombre());
            ps.setString(2, equipo.getTipo());
            ps.setString(3, equipo.getMarca());
            ps.setString(4, equipo.getModelo());
            ps.setString(5, equipo.getNumeroSerie());
            ps.setString(6, equipo.getUbicacionPlanta());
            ps.setDate(7, java.sql.Date.valueOf(equipo.getFechaInstalacion()));
            ps.setString(8, equipo.getEstadoOperativo());
            ps.setString(9, equipo.getCriticidad());
            ps.setInt(10, equipo.getIdEquipo());
            ps.executeUpdate();
        } catch (SQLException e) {
            log("Error updateEquipo: " + e.getMessage());
        }
    }

    @Override
    public void updateTecnico(Tecnico tecnico) {
        String sql = "UPDATE tecnico SET nombre_completo = ?, rfc = ?, telefono = ?, correo = ?, especialidad = ?, nivel_certificacion = ?, estatus = ? WHERE id_tecnico = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tecnico.getNombreCompleto());
            ps.setString(2, tecnico.getRfc());
            ps.setString(3, tecnico.getTelefono());
            ps.setString(4, tecnico.getCorreo());
            ps.setString(5, tecnico.getEspecialidad());
            ps.setString(6, tecnico.getNivelCertificacion());
            ps.setString(7, tecnico.getEstatus());
            ps.setInt(8, tecnico.getIdTecnico());
            ps.executeUpdate();
        } catch (SQLException e) {
            log("Error updateTecnico: " + e.getMessage());
        }
    }

    @Override
    public OrdenMantenimiento findOrdenById(Integer id) {
        if (id == null)
            return null;
        String sql = "SELECT * FROM orden_mantenimiento WHERE id_orden = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OrdenMantenimiento o = new OrdenMantenimiento();
                    o.setIdOrden(rs.getInt("id_orden"));
                    o.setIdEquipo(rs.getInt("id_equipo"));
                    o.setIdTecnico(rs.getInt("id_tecnico"));
                    o.setEstadoOrden(rs.getString("estado_orden"));
                    o.setFechaProgramada(rs.getDate("fecha_programada").toLocalDate());
                    Date dInicio = rs.getDate("fecha_inicio");
                    if (dInicio != null)
                        o.setFechaInicio(dInicio.toLocalDate());
                    Date dCierre = rs.getDate("fecha_cierre");
                    if (dCierre != null)
                        o.setFechaCierre(dCierre.toLocalDate());
                    o.setCostoReal(rs.getDouble("costo_real"));
                    o.setDescripcionTrabajo(rs.getString("descripcion_trabajo"));
                    return o;
                }
            }
        } catch (SQLException e) {
            log("Error findOrden: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<OrdenMantenimiento> findAllOrdenes() {
        List<OrdenMantenimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM orden_mantenimiento";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                OrdenMantenimiento o = new OrdenMantenimiento();
                o.setIdOrden(rs.getInt("id_orden"));
                o.setIdEquipo(rs.getInt("id_equipo"));
                o.setIdTecnico(rs.getInt("id_tecnico"));
                o.setTipoMantenimiento(rs.getString("tipo_mantenimiento"));
                o.setEstadoOrden(rs.getString("estado_orden"));
                o.setFechaProgramada(rs.getDate("fecha_programada").toLocalDate());
                Date dInicio = rs.getDate("fecha_inicio");
                if (dInicio != null)
                    o.setFechaInicio(dInicio.toLocalDate());
                Date dCierre = rs.getDate("fecha_cierre");
                if (dCierre != null)
                    o.setFechaCierre(dCierre.toLocalDate());
                o.setCostoEstimado(rs.getDouble("costo_estimado"));
                o.setCostoReal(rs.getDouble("costo_real"));
                o.setDescripcionTrabajo(rs.getString("descripcion_trabajo"));
                lista.add(o);
            }
        } catch (SQLException e) {
            log("Error findAllOrdenes: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<Equipo> findAllEquipos() {
        List<Equipo> lista = new ArrayList<>();
        String sql = "SELECT * FROM equipo";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Equipo e = new Equipo();
                e.setIdEquipo(rs.getInt("id_equipo"));
                e.setNombre(rs.getString("nombre"));
                e.setTipo(rs.getString("tipo"));
                e.setMarca(rs.getString("marca"));
                e.setModelo(rs.getString("modelo"));
                e.setNumeroSerie(rs.getString("numero_serie"));
                e.setUbicacionPlanta(rs.getString("ubicacion_planta"));
                e.setFechaInstalacion(rs.getDate("fecha_instalacion").toLocalDate());
                e.setEstadoOperativo(rs.getString("estado_operativo"));
                e.setCriticidad(rs.getString("criticidad"));
                lista.add(e);
            }
        } catch (SQLException e) {
            log("Error findAllEquipos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<Tecnico> findAllTecnicos() {
        List<Tecnico> lista = new ArrayList<>();
        String sql = "SELECT * FROM tecnico";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Tecnico t = new Tecnico();
                t.setIdTecnico(rs.getInt("id_tecnico"));
                t.setNombreCompleto(rs.getString("nombre_completo"));
                t.setRfc(rs.getString("rfc"));
                t.setTelefono(rs.getString("telefono"));
                t.setCorreo(rs.getString("correo"));
                t.setEspecialidad(rs.getString("especialidad"));
                t.setNivelCertificacion(rs.getString("nivel_certificacion"));
                t.setFechaIngreso(rs.getDate("fecha_ingreso").toLocalDate());
                t.setEstatus(rs.getString("estatus"));
                lista.add(t);
            }
        } catch (SQLException e) {
            log("Error findAllTecnicos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<OrdenMantenimiento> findOrdenesByEquipo(Integer idEquipo) {
        List<OrdenMantenimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM orden_mantenimiento WHERE id_equipo = ? AND estado_orden IN ('Programada', 'En ejecución')";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idEquipo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrdenMantenimiento o = new OrdenMantenimiento();
                    o.setFechaProgramada(rs.getDate("fecha_programada").toLocalDate());
                    lista.add(o);
                }
            }
        } catch (SQLException e) {
            log("Error findOrdenesByEq: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<OrdenMantenimiento> findOrdenesByTecnico(Integer idTecnico) {
        List<OrdenMantenimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM orden_mantenimiento WHERE id_tecnico = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTecnico);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrdenMantenimiento o = new OrdenMantenimiento();
                    o.setIdOrden(rs.getInt("id_orden"));
                    lista.add(o);
                }
            }
        } catch (SQLException e) {
            log("Error findOrdenesByTec: " + e.getMessage());
        }
        return lista;
    }
}
