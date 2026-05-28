package com.sigomei.server.network;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sigomei.server.model.Equipo;
import com.sigomei.server.model.OrdenMantenimiento;
import com.sigomei.server.model.Tecnico;
import com.sigomei.server.persistence.MantenimientoRepositoryJDBC;
import com.sigomei.server.service.MantenimientoService;
import com.sigomei.server.service.MantenimientoServiceImpl;
import com.sigomei.server.service.exception.BusinessRuleException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class SigomeiServer {
    private static final int PORT = 8050;
    private static final Logger logger = Logger.getLogger("SigomeiLogger");
    private final MantenimientoService service;
    private final ObjectMapper mapper;

    public SigomeiServer(MantenimientoService service) {
        this.service = service;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        setupLogger();
    }

    private void setupLogger() {
        try {
            FileHandler fh = new FileHandler("sigomei_server.log", true);
            logger.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
            logger.info("Servidor SIGOMEI iniciado.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor SIGOMEI escuchando en el puerto " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            logger.severe("Error en el servidor: " + e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String line;
                while ((line = in.readLine()) != null) {
                    logger.info("Petición recibida: " + line);
                    String response = processRequest(line);
                    logger.info("Respuesta enviada: " + response);
                    out.println(response);
                }
            } catch (IOException e) {
                logger.warning("Conexión con cliente cerrada: " + e.getMessage());
            }
        }

        private String processRequest(String json) {
            try {
                JsonNode root = mapper.readTree(json);
                String command = root.get("comando").asText();
                JsonNode payload = root.get("payload");

                Object result = null;

                switch (command) {
                    case "EQUIPO_CREAR":
                        Equipo e = mapper.treeToValue(payload, Equipo.class);
                        result = "RES_OK: Equipo creado con ID " + service.crearEquipo(e);
                        break;
                    case "TECNICO_CREAR":
                        Tecnico t = mapper.treeToValue(payload, Tecnico.class);
                        result = "RES_OK: Técnico creado con ID " + service.crearTecnico(t);
                        break;
                    case "ORDEN_CREAR":
                        OrdenMantenimiento o = mapper.treeToValue(payload, OrdenMantenimiento.class);
                        result = "RES_OK: Orden programada con ID " + service.programarOrden(o);
                        break;
                    case "TECNICO_ELIMINAR":
                        int idTec = payload.get("id_tecnico").asInt();
                        service.eliminarTecnico(idTec);
                        result = "RES_OK: Técnico eliminado.";
                        break;
                    case "ORDEN_FINALIZAR":
                        OrdenMantenimiento of = mapper.treeToValue(payload, OrdenMantenimiento.class);
                        service.finalizarOrden(of);
                        result = "RES_OK: Orden finalizada.";
                        break;
                    case "EQUIPO_LISTAR":
                        result = service.listarEquipos();
                        break;
                    case "TECNICO_LISTAR":
                        result = service.listarTecnicos();
                        break;
                    case "ORDEN_LISTAR":
                        result = service.listarOrdenes();
                        break;
                    default:
                        result = "ERR: Comando no reconocido.";
                }
                return mapper.writeValueAsString(result);
            } catch (BusinessRuleException e) {
                return "{\"error\": \"" + e.getRuleCode() + "\", \"mensaje\": \"" + e.getMessage() + "\"}";
            } catch (Exception e) {
                return "{\"error\": \"ERR_SYS\", \"mensaje\": \"" + e.getMessage() + "\"}";
            }
        }
    }

    public static void main(String[] args) {
        // En producción se cargaría desde un config, aquí usamos SQLite o MySQL localmente.
        // Para la demo, usaremos un archivo SQLite local.
        String dbUrl = "jdbc:h2:./sigomei_db;DB_CLOSE_DELAY=-1";
        try {
            com.sigomei.server.persistence.DatabaseSchema.createSchema(dbUrl);
            MantenimientoService service = new MantenimientoServiceImpl(new MantenimientoRepositoryJDBC(dbUrl));
            new SigomeiServer(service).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
