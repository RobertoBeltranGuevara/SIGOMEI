package com.sigomei.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sigomei.server.model.Equipo;
import com.sigomei.server.model.OrdenMantenimiento;
import com.sigomei.server.model.Tecnico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigomeiClient extends JFrame {
    private String host = "localhost";
    private static final int PORT = 8050;
    private final ObjectMapper mapper;
    private final JTextArea logArea;
    private final DefaultTableModel modelEquipos, modelTecnicos, modelOrdenes;

    public SigomeiClient() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());

        // Preguntar IP al inicio
        String inputIp = JOptionPane.showInputDialog(null, "Ingrese la IP del Servidor (Deje vacío para localhost):", "Configuración de Red", JOptionPane.QUESTION_MESSAGE);
        if (inputIp != null && !inputIp.trim().isEmpty()) {
            this.host = inputIp.trim();
        }
        
        setTitle("SISTEMA SIGOMEI - Panel de Gestión Profesional");
        setSize(1000, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.addTab("Registro Equipos", crearPanelEquipos());
        mainTabs.addTab("Registro Técnicos", crearPanelTecnicos());
        mainTabs.addTab("Órdenes de Servicio", crearPanelOrdenes());
        
        // --- PESTAÑA DE CONSULTAS ---
        modelEquipos = new DefaultTableModel(new String[]{"ID", "Nombre", "Tipo", "N/S", "Estado", "Criticidad"}, 0);
        modelTecnicos = new DefaultTableModel(new String[]{"ID", "Nombre", "RFC", "Especialidad", "Nivel", "Estatus"}, 0);
        modelOrdenes = new DefaultTableModel(new String[]{"ID", "ID Eq", "ID Tec", "Descripción", "Costo", "Estado"}, 0);
        
        mainTabs.addTab("Visualización de Datos", crearPanelConsultas());
        
        logArea = new JTextArea(8, 20);
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.CYAN);
        JScrollPane scrollLog = new JScrollPane(logArea);
        
        setLayout(new BorderLayout());
        add(mainTabs, BorderLayout.CENTER);
        add(scrollLog, BorderLayout.SOUTH);
    }

    private JPanel crearPanelConsultas() {
        JPanel p = new JPanel(new BorderLayout());
        JTabbedPane subTabs = new JTabbedPane();
        
        JTable tableEq = new JTable(modelEquipos);
        JTable tableTec = new JTable(modelTecnicos);
        JTable tableOrd = new JTable(modelOrdenes);

        subTabs.addTab("Equipos", new JScrollPane(tableEq));
        subTabs.addTab("Técnicos", new JScrollPane(tableTec));
        subTabs.addTab("Órdenes", new JScrollPane(tableOrd));

        JPanel commandPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        
        JButton btnModificar = new JButton("MODIFICAR SELECCIONADO");
        estilizarBoton(btnModificar, new Color(255, 160, 0));
        btnModificar.addActionListener(ae -> {
            int idx = subTabs.getSelectedIndex();
            if (idx == 0) showEditEquipoDialog(tableEq);
            else if (idx == 1) showEditTecnicoDialog(tableTec);
            else if (idx == 2) showEditOrdenDialog(tableOrd);
        });

        JButton btnEliminar = new JButton("ELIMINAR SELECCIONADO");
        estilizarBoton(btnEliminar, new Color(211, 47, 47));
        btnEliminar.addActionListener(ae -> {
            int idx = subTabs.getSelectedIndex();
            if (idx == 0) deleteEquipo(tableEq);
            else if (idx == 1) deleteTecnico(tableTec);
            else if (idx == 2) deleteOrden(tableOrd);
        });

        JButton btnRefrescar = new JButton("REFRESCAR DATOS");
        estilizarBoton(btnRefrescar, new Color(33, 150, 243));
        btnRefrescar.addActionListener(ae -> refrescarTablas());

        commandPanel.add(btnRefrescar);
        commandPanel.add(btnModificar);
        commandPanel.add(btnEliminar);

        p.add(subTabs, BorderLayout.CENTER);
        p.add(commandPanel, BorderLayout.SOUTH);
        return p;
    }

    private void refrescarTablas() {
        try {
            // Equipos
            String resEq = sendDirectRequest("EQUIPO_LISTAR", null);
            List<Equipo> eqs = mapper.readValue(resEq, new TypeReference<List<Equipo>>(){});
            modelEquipos.setRowCount(0);
            for(Equipo e : eqs) modelEquipos.addRow(new Object[]{e.getIdEquipo(), e.getNombre(), e.getTipo(), e.getNumeroSerie(), e.getEstadoOperativo(), e.getCriticidad()});

            // Técnicos
            String resTec = sendDirectRequest("TECNICO_LISTAR", null);
            List<Tecnico> tecs = mapper.readValue(resTec, new TypeReference<List<Tecnico>>(){});
            modelTecnicos.setRowCount(0);
            for(Tecnico t : tecs) modelTecnicos.addRow(new Object[]{t.getIdTecnico(), t.getNombreCompleto(), t.getRfc(), t.getEspecialidad(), t.getNivelCertificacion(), t.getEstatus()});

            // Órdenes
            String resOrd = sendDirectRequest("ORDEN_LISTAR", null);
            List<OrdenMantenimiento> ords = mapper.readValue(resOrd, new TypeReference<List<OrdenMantenimiento>>(){});
            modelOrdenes.setRowCount(0);
            for(OrdenMantenimiento o : ords) modelOrdenes.addRow(new Object[]{o.getIdOrden(), o.getIdEquipo(), o.getIdTecnico(), o.getDescripcionTrabajo(), o.getCostoEstimado(), o.getEstadoOrden()});
            
            logArea.append("[CONSULTA] Tablas actualizadas con el estado actual de la BD.\n");
        } catch (Exception ex) { logArea.append("Error al refrescar: " + ex.getMessage() + "\n"); }
    }

    private void showEditEquipoDialog(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un equipo."); return; }
        int id = (int) table.getValueAt(row, 0);
        
        try {
            // Obtener el objeto completo del servidor
            Map<String, Object> reqB = new HashMap<>(); reqB.put("id_equipo", id);
            String fullJson = sendDirectRequest("EQUIPO_BUSCAR", reqB);
            Equipo fullEq = mapper.readValue(fullJson, Equipo.class);

            JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
            JTextField txtNom = new JTextField(fullEq.getNombre());
            JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Eléctrico", "Mecánico", "Instrumentación", "Hidráulico"});
            cbTipo.setSelectedItem(fullEq.getTipo());
            JTextField txtMar = new JTextField(fullEq.getMarca());
            JTextField txtMod = new JTextField(fullEq.getModelo());
            JTextField txtSer = new JTextField(fullEq.getNumeroSerie());
            JTextField txtUbi = new JTextField(fullEq.getUbicacionPlanta());
            JTextField txtFec = new JTextField(fullEq.getFechaInstalacion() != null ? fullEq.getFechaInstalacion().toString() : "");
            JComboBox<String> cbEst = new JComboBox<>(new String[]{"Operativo", "En reparación"});
            cbEst.setSelectedItem(fullEq.getEstadoOperativo());
            JComboBox<String> cbCri = new JComboBox<>(new String[]{"Baja", "Media", "Alta"});
            cbCri.setSelectedItem(fullEq.getCriticidad());
            
            p.add(new JLabel("Nombre:")); p.add(txtNom); p.add(new JLabel("Tipo:")); p.add(cbTipo);
            p.add(new JLabel("Marca:")); p.add(txtMar); p.add(new JLabel("Modelo:")); p.add(txtMod);
            p.add(new JLabel("N/S:")); p.add(txtSer); p.add(new JLabel("Ubicación:")); p.add(txtUbi);
            p.add(new JLabel("Fecha:")); p.add(txtFec); p.add(new JLabel("Estado:")); p.add(cbEst);
            p.add(new JLabel("Criticidad:")); p.add(cbCri);
            
            if (JOptionPane.showConfirmDialog(this, p, "MODIFICAR EQUIPO", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                fullEq.setNombre(txtNom.getText());
                fullEq.setTipo(cbTipo.getSelectedItem() != null ? cbTipo.getSelectedItem().toString() : fullEq.getTipo());
                fullEq.setMarca(txtMar.getText());
                fullEq.setModelo(txtMod.getText());
                fullEq.setNumeroSerie(txtSer.getText());
                fullEq.setUbicacionPlanta(txtUbi.getText());
                if (!txtFec.getText().isEmpty()) fullEq.setFechaInstalacion(LocalDate.parse(txtFec.getText()));
                fullEq.setEstadoOperativo(cbEst.getSelectedItem() != null ? cbEst.getSelectedItem().toString() : fullEq.getEstadoOperativo());
                fullEq.setCriticidad(cbCri.getSelectedItem() != null ? cbCri.getSelectedItem().toString() : fullEq.getCriticidad());

                sendRequest("EQUIPO_ACTUALIZAR", fullEq);
                refrescarTablas();
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error al obtener datos: " + ex.getMessage()); }
    }

    private void showEditTecnicoDialog(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un técnico."); return; }
        int id = (int) table.getValueAt(row, 0);
        
        try {
            Map<String, Object> reqB = new HashMap<>(); reqB.put("id_tecnico", id);
            String fullJson = sendDirectRequest("TECNICO_BUSCAR", reqB);
            Tecnico fullTec = mapper.readValue(fullJson, Tecnico.class);

            JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
            JTextField txtNom = new JTextField(fullTec.getNombreCompleto());
            JTextField txtRfc = new JTextField(fullTec.getRfc());
            JTextField txtTel = new JTextField(fullTec.getTelefono());
            JTextField txtCor = new JTextField(fullTec.getCorreo());
            JComboBox<String> cbEsp = new JComboBox<>(new String[]{"Eléctrico", "Mecánico", "Instrumentación", "Hidráulico"});
            cbEsp.setSelectedItem(fullTec.getEspecialidad());
            JComboBox<String> cbNiv = new JComboBox<>(new String[]{"I", "II", "III"});
            cbNiv.setSelectedItem(fullTec.getNivelCertificacion());
            JTextField txtFec = new JTextField(fullTec.getFechaIngreso() != null ? fullTec.getFechaIngreso().toString() : "");
            JComboBox<String> cbEst = new JComboBox<>(new String[]{"Activo", "Inactivo"});
            cbEst.setSelectedItem(fullTec.getEstatus());
            
            p.add(new JLabel("Nombre:")); p.add(txtNom); p.add(new JLabel("RFC:")); p.add(txtRfc);
            p.add(new JLabel("Teléfono:")); p.add(txtTel); p.add(new JLabel("Correo:")); p.add(txtCor);
            p.add(new JLabel("Esp:")); p.add(cbEsp); p.add(new JLabel("Nivel:")); p.add(cbNiv);
            p.add(new JLabel("Fecha:")); p.add(txtFec); p.add(new JLabel("Estatus:")); p.add(cbEst);
            
            if (JOptionPane.showConfirmDialog(this, p, "MODIFICAR TÉCNICO", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                fullTec.setNombreCompleto(txtNom.getText());
                fullTec.setRfc(txtRfc.getText());
                fullTec.setTelefono(txtTel.getText());
                fullTec.setCorreo(txtCor.getText());
                fullTec.setEspecialidad(cbEsp.getSelectedItem() != null ? cbEsp.getSelectedItem().toString() : fullTec.getEspecialidad());
                fullTec.setNivelCertificacion(cbNiv.getSelectedItem() != null ? cbNiv.getSelectedItem().toString() : fullTec.getNivelCertificacion());
                if (!txtFec.getText().isEmpty()) fullTec.setFechaIngreso(LocalDate.parse(txtFec.getText()));
                fullTec.setEstatus(cbEst.getSelectedItem() != null ? cbEst.getSelectedItem().toString() : fullTec.getEstatus());

                sendRequest("TECNICO_ACTUALIZAR", fullTec);
                refrescarTablas();
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void showEditOrdenDialog(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una orden."); return; }
        int id = (int) table.getValueAt(row, 0);
        
        try {
            Map<String, Object> reqB = new HashMap<>(); reqB.put("id_orden", id);
            String fullJson = sendDirectRequest("ORDEN_BUSCAR", reqB);
            OrdenMantenimiento fullOrd = mapper.readValue(fullJson, OrdenMantenimiento.class);

            JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
            JTextField txtEq = new JTextField(String.valueOf(fullOrd.getIdEquipo()));
            JTextField txtTec = new JTextField(String.valueOf(fullOrd.getIdTecnico()));
            JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Preventivo", "Correctivo"});
            cbTipo.setSelectedItem(fullOrd.getTipoMantenimiento());
            JTextField txtFec = new JTextField(fullOrd.getFechaProgramada() != null ? fullOrd.getFechaProgramada().toString() : "");
            JTextField txtDes = new JTextField(fullOrd.getDescripcionTrabajo());
            JTextField txtCos = new JTextField(String.valueOf(fullOrd.getCostoEstimado()));
            JComboBox<String> cbEst = new JComboBox<>(new String[]{"Programada", "En ejecución", "Finalizada"});
            cbEst.setSelectedItem(fullOrd.getEstadoOrden());
            
            p.add(new JLabel("ID Equipo:")); p.add(txtEq); p.add(new JLabel("ID Técnico:")); p.add(txtTec);
            p.add(new JLabel("Tipo:")); p.add(cbTipo); p.add(new JLabel("Fecha:")); p.add(txtFec);
            p.add(new JLabel("Descripción:")); p.add(txtDes); p.add(new JLabel("Costo Est.:")); p.add(txtCos);
            p.add(new JLabel("Estado:")); p.add(cbEst);
            
            if (JOptionPane.showConfirmDialog(this, p, "MODIFICAR ORDEN", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                fullOrd.setIdEquipo(Integer.parseInt(txtEq.getText()));
                fullOrd.setIdTecnico(Integer.parseInt(txtTec.getText()));
                fullOrd.setTipoMantenimiento(cbTipo.getSelectedItem() != null ? cbTipo.getSelectedItem().toString() : fullOrd.getTipoMantenimiento());
                if (!txtFec.getText().isEmpty()) fullOrd.setFechaProgramada(LocalDate.parse(txtFec.getText()));
                fullOrd.setDescripcionTrabajo(txtDes.getText());
                fullOrd.setCostoEstimado(Double.parseDouble(txtCos.getText()));
                fullOrd.setEstadoOrden(cbEst.getSelectedItem() != null ? cbEst.getSelectedItem().toString() : fullOrd.getEstadoOrden());

                sendRequest("ORDEN_ACTUALIZAR", fullOrd);
                refrescarTablas();
            }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void deleteEquipo(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un equipo."); return; }
        int id = (int) table.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar equipo " + id + "?", "CONFIRMAR", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Map<String, Object> d = new HashMap<>(); d.put("id_equipo", id);
            sendRequest("EQUIPO_ELIMINAR", d);
            refrescarTablas();
        }
    }

    private void deleteTecnico(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione un técnico."); return; }
        int id = (int) table.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar técnico " + id + "?", "CONFIRMAR", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Map<String, Object> d = new HashMap<>(); d.put("id_tecnico", id);
            sendRequest("TECNICO_ELIMINAR", d);
            refrescarTablas();
        }
    }

    private void deleteOrden(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Seleccione una orden."); return; }
        int id = (int) table.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "¿Eliminar orden " + id + "?", "CONFIRMAR", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Map<String, Object> d = new HashMap<>(); d.put("id_orden", id);
            sendRequest("ORDEN_ELIMINAR", d);
            refrescarTablas();
        }
    }

    // El resto de los paneles de registro (Iguales que antes pero consolidados)
    private JPanel crearPanelEquipos() {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField txtNom = new JTextField(); JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Eléctrico", "Mecánico", "Instrumentación", "Hidráulico"});
        JTextField txtMar = new JTextField(); JTextField txtMod = new JTextField();
        JTextField txtSer = new JTextField(); JTextField txtUbi = new JTextField();
        JTextField txtFec = new JTextField(LocalDate.now().toString()); JComboBox<String> cbEst = new JComboBox<>(new String[]{"Operativo", "En reparación"});
        JComboBox<String> cbCri = new JComboBox<>(new String[]{"Baja", "Media", "Alta"});
        p.add(new JLabel("Nombre:")); p.add(txtNom); p.add(new JLabel("Tipo:")); p.add(cbTipo);
        p.add(new JLabel("Marca:")); p.add(txtMar); p.add(new JLabel("Modelo:")); p.add(txtMod);
        p.add(new JLabel("N/S:")); p.add(txtSer); p.add(new JLabel("Ubicación:")); p.add(txtUbi);
        p.add(new JLabel("Fecha:")); p.add(txtFec); p.add(new JLabel("Estado:")); p.add(cbEst); p.add(new JLabel("Criticidad:")); p.add(cbCri);
        JButton btn = new JButton("GUARDAR EQUIPO");
        estilizarBoton(btn, new Color(13, 71, 161));
        btn.addActionListener(ae -> {
            Map<String, Object> d = new HashMap<>(); d.put("nombre", txtNom.getText()); d.put("tipo", cbTipo.getSelectedItem());
            d.put("marca", txtMar.getText()); d.put("modelo", txtMod.getText()); d.put("numeroSerie", txtSer.getText());
            d.put("ubicacionPlanta", txtUbi.getText()); d.put("fechaInstalacion", txtFec.getText());
            d.put("estadoOperativo", cbEst.getSelectedItem()); d.put("criticidad", cbCri.getSelectedItem());
            sendRequest("EQUIPO_CREAR", d);
        });
        p.add(new JLabel("")); p.add(btn); return p;
    }

    private JPanel crearPanelTecnicos() {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField txtNom = new JTextField(); JTextField txtRfc = new JTextField();
        JTextField txtTel = new JTextField(); JTextField txtCor = new JTextField();
        JComboBox<String> cbEsp = new JComboBox<>(new String[]{"Eléctrico", "Mecánico", "Instrumentación", "Hidráulico"});
        JComboBox<String> cbNiv = new JComboBox<>(new String[]{"I", "II", "III"});
        JTextField txtFec = new JTextField(LocalDate.now().toString()); JComboBox<String> cbEst = new JComboBox<>(new String[]{"Activo", "Inactivo"});
        p.add(new JLabel("Nombre:")); p.add(txtNom); p.add(new JLabel("RFC:")); p.add(txtRfc);
        p.add(new JLabel("Teléfono:")); p.add(txtTel); p.add(new JLabel("Correo:")); p.add(txtCor);
        p.add(new JLabel("Esp:")); p.add(cbEsp); p.add(new JLabel("Nivel:")); p.add(cbNiv);
        p.add(new JLabel("Fecha:")); p.add(txtFec); p.add(new JLabel("Estatus:")); p.add(cbEst);
        JButton btn = new JButton("REGISTRAR TÉCNICO");
        estilizarBoton(btn, new Color(0, 77, 64));
        btn.addActionListener(ae -> {
            Map<String, Object> d = new HashMap<>(); d.put("nombreCompleto", txtNom.getText()); d.put("rfc", txtRfc.getText());
            d.put("telefono", txtTel.getText()); d.put("correo", txtCor.getText()); d.put("especialidad", cbEsp.getSelectedItem());
            d.put("nivelCertificacion", cbNiv.getSelectedItem()); d.put("fechaIngreso", txtFec.getText()); d.put("estatus", cbEst.getSelectedItem());
            sendRequest("TECNICO_CREAR", d);
        });
        p.add(new JLabel("")); p.add(btn); return p;
    }

    private JPanel crearPanelOrdenes() {
        JPanel p = new JPanel(new GridLayout(0, 2, 5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField txtEq = new JTextField(); JTextField txtTec = new JTextField();
        JComboBox<String> cbTipo = new JComboBox<>(new String[]{"Preventivo", "Correctivo"});
        JTextField txtFec = new JTextField("2026-06-01"); JTextField txtDes = new JTextField(); JTextField txtCos = new JTextField("0.0");
        p.add(new JLabel("ID Equipo:")); p.add(txtEq); p.add(new JLabel("ID Técnico:")); p.add(txtTec);
        p.add(new JLabel("Tipo:")); p.add(cbTipo); p.add(new JLabel("Fecha:")); p.add(txtFec);
        p.add(new JLabel("Descripción:")); p.add(txtDes); p.add(new JLabel("Costo:")); p.add(txtCos);
        JButton btn = new JButton("GENERAR ORDEN");
        estilizarBoton(btn, new Color(230, 81, 0));
        btn.addActionListener(ae -> {
            Map<String, Object> d = new HashMap<>(); d.put("idEquipo", Integer.parseInt(txtEq.getText()));
            d.put("idTecnico", Integer.parseInt(txtTec.getText())); d.put("tipoMantenimiento", cbTipo.getSelectedItem());
            d.put("fechaProgramada", txtFec.getText()); d.put("descripcionTrabajo", txtDes.getText());
            d.put("costoEstimado", Double.parseDouble(txtCos.getText())); d.put("estadoOrden", "Programada");
            sendRequest("ORDEN_CREAR", d);
        });
        p.add(new JLabel("")); p.add(btn); return p;
    }

    private void estilizarBoton(JButton b, Color bg) { b.setOpaque(true); b.setBorderPainted(false); b.setBackground(bg); b.setForeground(Color.WHITE); }

    private void sendRequest(String cmd, Object pay) {
        logArea.append("[" + cmd + "] Enviando... ");
        String res = sendDirectRequest(cmd, pay);
        logArea.append("Respuesta: " + res + "\n");
    }

    private String sendDirectRequest(String cmd, Object pay) {
        try (Socket s = new Socket(this.host, PORT);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
            Map<String, Object> req = new HashMap<>(); req.put("comando", cmd); req.put("payload", pay);
            out.println(mapper.writeValueAsString(req));
            return in.readLine();
        } catch (Exception e) { return "Error: " + e.getMessage(); }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new SigomeiClient().setVisible(true));
    }
}
