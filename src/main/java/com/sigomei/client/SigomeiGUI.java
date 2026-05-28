package com.sigomei.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.formdev.flatlaf.FlatDarkLaf;
import com.sigomei.server.model.Equipo;
import com.sigomei.server.model.OrdenMantenimiento;
import com.sigomei.server.model.Tecnico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigomeiGUI extends JFrame {
    private static final String HOST = "localhost";
    private static final int PORT = 8050;
    private final ObjectMapper mapper;
    
    private JTable tableEquipos;
    private JTable tableTecnicos;
    private JTable tableOrdenes;
    
    private DefaultTableModel modelEquipos;
    private DefaultTableModel modelTecnicos;
    private DefaultTableModel modelOrdenes;

    public SigomeiGUI() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        
        setTitle("SIGOMEI - Gestión de Mantenimiento Distribuido");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        refreshAll();
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Equipos", createEquiposPanel());
        tabbedPane.addTab("Técnicos", createTecnicosPanel());
        tabbedPane.addTab("Órdenes", createOrdenesPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.add(new JLabel("Conectado a " + HOST + ":" + PORT));
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel createEquiposPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"ID", "Nombre", "Tipo", "Marca", "Modelo", "Ubicación", "Estado", "Criticidad"};
        modelEquipos = new DefaultTableModel(columns, 0);
        tableEquipos = new JTable(modelEquipos);
        panel.add(new JScrollPane(tableEquipos), BorderLayout.CENTER);
        
        JPanel actions = new JPanel();
        JButton btnAdd = new JButton("Nuevo Equipo");
        btnAdd.addActionListener(e -> showAddEquipoDialog());
        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> refreshEquipos());
        JButton btnDelete = new JButton("Eliminar");
        btnDelete.addActionListener(e -> deleteEquipo());
        
        actions.add(btnAdd);
        JButton btnEdit = new JButton("Modificar");
        btnEdit.addActionListener(e -> showEditEquipoDialog());
        actions.add(btnEdit);
        actions.add(btnDelete);
        actions.add(btnRefresh);
        panel.add(actions, BorderLayout.NORTH);
        
        return panel;
    }

    private JPanel createTecnicosPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"ID", "Nombre", "RFC", "Especialidad", "Nivel", "Estatus"};
        modelTecnicos = new DefaultTableModel(columns, 0);
        tableTecnicos = new JTable(modelTecnicos);
        panel.add(new JScrollPane(tableTecnicos), BorderLayout.CENTER);
        
        JPanel actions = new JPanel();
        JButton btnAdd = new JButton("Nuevo Técnico");
        btnAdd.addActionListener(e -> showAddTecnicoDialog());
        JButton btnDelete = new JButton("Eliminar");
        btnDelete.addActionListener(e -> deleteTecnico());
        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> refreshTecnicos());
        
        actions.add(btnAdd);
        JButton btnEdit = new JButton("Modificar");
        btnEdit.addActionListener(e -> showEditTecnicoDialog());
        actions.add(btnEdit);
        actions.add(btnDelete);
        actions.add(btnRefresh);
        panel.add(actions, BorderLayout.NORTH);
        
        return panel;
    }

    private JPanel createOrdenesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"ID", "Equipo ID", "Técnico ID", "Tipo", "Fecha Prog.", "Estado", "Costo Est."};
        modelOrdenes = new DefaultTableModel(columns, 0);
        tableOrdenes = new JTable(modelOrdenes);
        panel.add(new JScrollPane(tableOrdenes), BorderLayout.CENTER);
        
        JPanel actions = new JPanel();
        JButton btnAdd = new JButton("Programar Orden");
        btnAdd.addActionListener(e -> showAddOrdenDialog());
        JButton btnFinish = new JButton("Finalizar Orden");
        btnFinish.addActionListener(e -> finishOrden());
        JButton btnRefresh = new JButton("Actualizar");
        btnRefresh.addActionListener(e -> refreshOrdenes());
        JButton btnDelete = new JButton("Eliminar");
        btnDelete.addActionListener(e -> deleteOrden());
        
        actions.add(btnAdd);
        JButton btnEdit = new JButton("Modificar");
        btnEdit.addActionListener(e -> showEditOrdenDialog());
        actions.add(btnEdit);
        actions.add(btnFinish);
        actions.add(btnDelete);
        actions.add(btnRefresh);
        panel.add(actions, BorderLayout.NORTH);
        
        return panel;
    }

    private void showAddEquipoDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField txtNombre = new JTextField();
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Mecánico", "Eléctrico", "Electrónico", "Hidráulico"});
        JTextField txtMarca = new JTextField();
        JTextField txtModelo = new JTextField();
        JTextField txtSerie = new JTextField();
        JTextField txtUbicacion = new JTextField();
        JComboBox<String> cbCriticidad = new JComboBox<>(new String[]{"Baja", "Media", "Alta"});
        
        panel.add(new JLabel("Nombre:")); panel.add(txtNombre);
        panel.add(new JLabel("Tipo:")); panel.add(cbTipo);
        panel.add(new JLabel("Marca:")); panel.add(txtMarca);
        panel.add(new JLabel("Modelo:")); panel.add(txtModelo);
        panel.add(new JLabel("N. Serie:")); panel.add(txtSerie);
        panel.add(new JLabel("Ubicación:")); panel.add(txtUbicacion);
        panel.add(new JLabel("Criticidad:")); panel.add(cbCriticidad);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Nuevo Equipo", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("nombre", txtNombre.getText());
            payload.put("tipo", cbTipo.getSelectedItem());
            payload.put("marca", txtMarca.getText());
            payload.put("modelo", txtModelo.getText());
            payload.put("numeroSerie", txtSerie.getText());
            payload.put("ubicacionPlanta", txtUbicacion.getText());
            payload.put("criticidad", cbCriticidad.getSelectedItem());
            payload.put("fechaInstalacion", LocalDate.now().toString());
            payload.put("estadoOperativo", "Operativo");
            
            sendRequest("EQUIPO_CREAR", payload);
            refreshEquipos();
        }
    }

    private void showEditEquipoDialog() {
        int row = tableEquipos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un equipo.");
            return;
        }
        int id = (int) modelEquipos.getValueAt(row, 0);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField txtNombre = new JTextField(modelEquipos.getValueAt(row, 1).toString());
        JComboBox<String> cbTipo = new JComboBox<>(new String[] { "Mecánico", "Eléctrico", "Electrónico", "Hidráulico" });
        cbTipo.setSelectedItem(modelEquipos.getValueAt(row, 2).toString());
        JTextField txtMarca = new JTextField(modelEquipos.getValueAt(row, 3).toString());
        JTextField txtModelo = new JTextField(modelEquipos.getValueAt(row, 4).toString());
        JTextField txtUbicacion = new JTextField(modelEquipos.getValueAt(row, 5).toString());
        JComboBox<String> cbCriticidad = new JComboBox<>(new String[] { "Baja", "Media", "Alta" });
        cbCriticidad.setSelectedItem(modelEquipos.getValueAt(row, 7).toString());

        panel.add(new JLabel("Nombre:")); panel.add(txtNombre);
        panel.add(new JLabel("Tipo:")); panel.add(cbTipo);
        panel.add(new JLabel("Marca:")); panel.add(txtMarca);
        panel.add(new JLabel("Modelo:")); panel.add(txtModelo);
        panel.add(new JLabel("Ubicación:")); panel.add(txtUbicacion);
        panel.add(new JLabel("Criticidad:")); panel.add(cbCriticidad);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modificar Equipo", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("idEquipo", id);
            payload.put("nombre", txtNombre.getText());
            payload.put("tipo", cbTipo.getSelectedItem());
            payload.put("marca", txtMarca.getText());
            payload.put("modelo", txtModelo.getText());
            payload.put("ubicacionPlanta", txtUbicacion.getText());
            payload.put("criticidad", cbCriticidad.getSelectedItem());
            payload.put("estadoOperativo", modelEquipos.getValueAt(row, 6).toString());

            sendRequest("EQUIPO_ACTUALIZAR", payload);
            refreshEquipos();
        }
    }

    private void showAddTecnicoDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField txtNombre = new JTextField();
        JTextField txtRfc = new JTextField();
        JTextField txtTel = new JTextField();
        JTextField txtCorreo = new JTextField();
        JComboBox<String> cbEspecialidad = new JComboBox<>(new String[]{"Mecánico", "Eléctrico", "Electrónico", "Hidráulico"});
        JComboBox<String> cbNivel = new JComboBox<>(new String[]{"I", "II", "III"});
        
        panel.add(new JLabel("Nombre Completo:")); panel.add(txtNombre);
        panel.add(new JLabel("RFC:")); panel.add(txtRfc);
        panel.add(new JLabel("Teléfono:")); panel.add(txtTel);
        panel.add(new JLabel("Correo:")); panel.add(txtCorreo);
        panel.add(new JLabel("Especialidad:")); panel.add(cbEspecialidad);
        panel.add(new JLabel("Nivel:")); panel.add(cbNivel);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Nuevo Técnico", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("nombreCompleto", txtNombre.getText());
            payload.put("rfc", txtRfc.getText());
            payload.put("telefono", txtTel.getText());
            payload.put("correo", txtCorreo.getText());
            payload.put("especialidad", cbEspecialidad.getSelectedItem());
            payload.put("nivelCertificacion", cbNivel.getSelectedItem());
            payload.put("fechaIngreso", LocalDate.now().toString());
            payload.put("estatus", "Activo");
            
            sendRequest("TECNICO_CREAR", payload);
            refreshTecnicos();
        }
    }

    private void showEditTecnicoDialog() {
        int row = tableTecnicos.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un técnico.");
            return;
        }
        int id = (int) modelTecnicos.getValueAt(row, 0);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField txtNombre = new JTextField(modelTecnicos.getValueAt(row, 1).toString());
        JTextField txtRfc = new JTextField(modelTecnicos.getValueAt(row, 2).toString());
        JComboBox<String> cbEspecialidad = new JComboBox<>(new String[] { "Mecánico", "Eléctrico", "Electrónico", "Hidráulico" });
        cbEspecialidad.setSelectedItem(modelTecnicos.getValueAt(row, 3).toString());
        JComboBox<String> cbNivel = new JComboBox<>(new String[] { "I", "II", "III" });
        cbNivel.setSelectedItem(modelTecnicos.getValueAt(row, 4).toString());
        JComboBox<String> cbEstatus = new JComboBox<>(new String[] { "Activo", "Inactivo" });
        cbEstatus.setSelectedItem(modelTecnicos.getValueAt(row, 5).toString());

        panel.add(new JLabel("Nombre Completo:")); panel.add(txtNombre);
        panel.add(new JLabel("RFC:")); panel.add(txtRfc);
        panel.add(new JLabel("Especialidad:")); panel.add(cbEspecialidad);
        panel.add(new JLabel("Nivel:")); panel.add(cbNivel);
        panel.add(new JLabel("Estatus:")); panel.add(cbEstatus);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modificar Técnico", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("idTecnico", id);
            payload.put("nombreCompleto", txtNombre.getText());
            payload.put("rfc", txtRfc.getText());
            payload.put("especialidad", cbEspecialidad.getSelectedItem());
            payload.put("nivelCertificacion", cbNivel.getSelectedItem());
            payload.put("estatus", cbEstatus.getSelectedItem());

            sendRequest("TECNICO_ACTUALIZAR", payload);
            refreshTecnicos();
        }
    }

    private void deleteEquipo() {
        int row = tableEquipos.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableEquipos.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este equipo?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("id_equipo", id);
                sendRequest("EQUIPO_ELIMINAR", payload);
                refreshEquipos();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un equipo.");
        }
    }

    private void showAddOrdenDialog() {
        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField txtIdEq = new JTextField();
        JTextField txtIdTec = new JTextField();
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Preventivo", "Correctivo", "Predictivo"});
        JTextField txtFecha = new JTextField(LocalDate.now().plusWeeks(1).toString());
        JTextField txtDesc = new JTextField();
        JTextField txtCosto = new JTextField("0.0");
        
        panel.add(new JLabel("ID Equipo:")); panel.add(txtIdEq);
        panel.add(new JLabel("ID Técnico:")); panel.add(txtIdTec);
        panel.add(new JLabel("Tipo:")); panel.add(cbTipo);
        panel.add(new JLabel("Fecha Prog (YYYY-MM-DD):")); panel.add(txtFecha);
        panel.add(new JLabel("Descripción:")); panel.add(txtDesc);
        panel.add(new JLabel("Costo Est.:")); panel.add(txtCosto);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Programar Orden", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("idEquipo", Integer.parseInt(txtIdEq.getText()));
                payload.put("idTecnico", Integer.parseInt(txtIdTec.getText()));
                payload.put("tipoMantenimiento", cbTipo.getSelectedItem());
                payload.put("fechaProgramada", txtFecha.getText());
                payload.put("descripcionTrabajo", txtDesc.getText());
                payload.put("costoEstimado", Double.parseDouble(txtCosto.getText()));
                
                sendRequest("ORDEN_CREAR", payload);
                refreshOrdenes();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error en los datos: " + e.getMessage());
            }
        }
    }

    private void showEditOrdenDialog() {
        int row = tableOrdenes.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden.");
            return;
        }
        int id = (int) modelOrdenes.getValueAt(row, 0);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        JTextField txtIdEq = new JTextField(modelOrdenes.getValueAt(row, 1).toString());
        JTextField txtIdTec = new JTextField(modelOrdenes.getValueAt(row, 2).toString());
        JComboBox<String> cbTipo = new JComboBox<>(new String[] { "Preventivo", "Correctivo", "Predictivo" });
        cbTipo.setSelectedItem(modelOrdenes.getValueAt(row, 3).toString());
        JTextField txtFecha = new JTextField(modelOrdenes.getValueAt(row, 4).toString());
        JTextField txtCosto = new JTextField(modelOrdenes.getValueAt(row, 6).toString());

        panel.add(new JLabel("ID Equipo:")); panel.add(txtIdEq);
        panel.add(new JLabel("ID Técnico:")); panel.add(txtIdTec);
        panel.add(new JLabel("Tipo:")); panel.add(cbTipo);
        panel.add(new JLabel("Fecha Prog (YYYY-MM-DD):")); panel.add(txtFecha);
        panel.add(new JLabel("Costo Est.:")); panel.add(txtCosto);

        int result = JOptionPane.showConfirmDialog(this, panel, "Modificar Orden", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("idOrden", id);
                payload.put("idEquipo", Integer.parseInt(txtIdEq.getText()));
                payload.put("idTecnico", Integer.parseInt(txtIdTec.getText()));
                payload.put("tipoMantenimiento", cbTipo.getSelectedItem());
                payload.put("fechaProgramada", txtFecha.getText());
                payload.put("costoEstimado", Double.parseDouble(txtCosto.getText()));
                payload.put("estadoOrden", modelOrdenes.getValueAt(row, 5).toString());

                sendRequest("ORDEN_ACTUALIZAR", payload);
                refreshOrdenes();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error en los datos: " + e.getMessage());
            }
        }
    }

    private void deleteOrden() {
        int row = tableOrdenes.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableOrdenes.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar esta orden?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("id_orden", id);
                sendRequest("ORDEN_ELIMINAR", payload);
                refreshOrdenes();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una orden.");
        }
    }

    private void deleteTecnico() {
        int row = tableTecnicos.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableTecnicos.getValueAt(row, 0);
            Map<String, Object> payload = new HashMap<>();
            payload.put("id_tecnico", id);
            sendRequest("TECNICO_ELIMINAR", payload);
            refreshTecnicos();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un técnico.");
        }
    }

    private void finishOrden() {
        int row = tableOrdenes.getSelectedRow();
        if (row >= 0) {
            int id = (int) tableOrdenes.getValueAt(row, 0);
            String desc = JOptionPane.showInputDialog(this, "Descripción del cierre:");
            String costo = JOptionPane.showInputDialog(this, "Costo Real:", "0.0");
            
            if (desc != null && costo != null) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("idOrden", id);
                payload.put("fechaInicio", LocalDate.now().toString());
                payload.put("fechaCierre", LocalDate.now().toString());
                payload.put("costoReal", Double.parseDouble(costo));
                payload.put("descripcionTrabajo", desc);
                
                sendRequest("ORDEN_FINALIZAR", payload);
                refreshOrdenes();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una orden.");
        }
    }

    private void refreshAll() {
        refreshEquipos();
        refreshTecnicos();
        refreshOrdenes();
    }

    private void refreshEquipos() {
        Object response = sendRequest("EQUIPO_LISTAR", null);
        if (response instanceof List) {
            List<Equipo> list = mapper.convertValue(response, new TypeReference<List<Equipo>>() {});
            modelEquipos.setRowCount(0);
            for (Equipo e : list) {
                modelEquipos.addRow(new Object[]{e.getIdEquipo(), e.getNombre(), e.getTipo(), e.getMarca(), e.getModelo(), e.getUbicacionPlanta(), e.getEstadoOperativo(), e.getCriticidad()});
            }
        }
    }

    private void refreshTecnicos() {
        Object response = sendRequest("TECNICO_LISTAR", null);
        if (response instanceof List) {
            List<Tecnico> list = mapper.convertValue(response, new TypeReference<List<Tecnico>>() {});
            modelTecnicos.setRowCount(0);
            for (Tecnico t : list) {
                modelTecnicos.addRow(new Object[]{t.getIdTecnico(), t.getNombreCompleto(), t.getRfc(), t.getEspecialidad(), t.getNivelCertificacion(), t.getEstatus()});
            }
        }
    }

    private void refreshOrdenes() {
        Object response = sendRequest("ORDEN_LISTAR", null);
        if (response instanceof List) {
            List<OrdenMantenimiento> list = mapper.convertValue(response, new TypeReference<List<OrdenMantenimiento>>() {});
            modelOrdenes.setRowCount(0);
            for (OrdenMantenimiento o : list) {
                modelOrdenes.addRow(new Object[]{o.getIdOrden(), o.getIdEquipo(), o.getIdTecnico(), o.getTipoMantenimiento(), o.getFechaProgramada(), o.getEstadoOrden(), o.getCostoEstimado()});
            }
        }
    }

    private Object sendRequest(String comando, Object payload) {
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Map<String, Object> request = new HashMap<>();
            request.put("comando", comando);
            request.put("payload", payload);

            String json = mapper.writeValueAsString(request);
            out.println(json);

            String responseJson = in.readLine();
            if (responseJson == null) return null;
            
            JsonNode node = mapper.readTree(responseJson);
            if (node.isTextual() && node.asText().startsWith("RES_OK")) {
                JOptionPane.showMessageDialog(this, node.asText());
                return node.asText();
            } else if (node.has("error")) {
                JOptionPane.showMessageDialog(this, "Error: " + node.get("mensaje").asText(), "Regla de Negocio", JOptionPane.WARNING_MESSAGE);
                return null;
            } else {
                return mapper.treeToValue(node, Object.class);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new SigomeiGUI().setVisible(true));
    }
}
