package com.sigomei.server.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSchema {
    
    public static void createSchema(String url) throws Exception {
        try (Connection conn = DriverManager.getConnection(url, "sa", "");
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS equipo (" +
                    "id_equipo INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre VARCHAR(100) NOT NULL, " +
                    "tipo VARCHAR(50) NOT NULL, " +
                    "marca VARCHAR(50) NOT NULL, " +
                    "modelo VARCHAR(50) NOT NULL, " +
                    "numero_serie VARCHAR(50) UNIQUE NOT NULL, " +
                    "ubicacion_planta VARCHAR(100) NOT NULL, " +
                    "fecha_instalacion DATE NOT NULL, " +
                    "estado_operativo VARCHAR(30) NOT NULL, " +
                    "criticidad VARCHAR(10) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS tecnico (" +
                    "id_tecnico INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nombre_completo VARCHAR(150) NOT NULL, " +
                    "rfc VARCHAR(13) UNIQUE NOT NULL, " +
                    "telefono VARCHAR(15) NOT NULL, " +
                    "correo VARCHAR(100) NOT NULL, " +
                    "especialidad VARCHAR(50) NOT NULL, " +
                    "nivel_certificacion VARCHAR(5) NOT NULL, " +
                    "fecha_ingreso DATE NOT NULL, " +
                    "estatus VARCHAR(10) NOT NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS orden_mantenimiento (" +
                    "id_orden INT AUTO_INCREMENT PRIMARY KEY, " +
                    "id_equipo INT NOT NULL, " +
                    "id_tecnico INT NOT NULL, " +
                    "tipo_mantenimiento VARCHAR(30) NOT NULL, " +
                    "fecha_programada DATE NOT NULL, " +
                    "fecha_inicio DATE NULL, " +
                    "fecha_cierre DATE NULL, " +
                    "descripcion_trabajo TEXT NOT NULL, " +
                    "costo_estimado DECIMAL(10,2) NOT NULL, " +
                    "costo_real DECIMAL(10,2) NULL, " +
                    "estado_orden VARCHAR(20) NOT NULL, " +
                    "FOREIGN KEY (id_equipo) REFERENCES equipo(id_equipo), " +
                    "FOREIGN KEY (id_tecnico) REFERENCES tecnico(id_tecnico))");
        }
    }
}
