package com.sigomei.server.model;

import java.time.LocalDate;

public class Equipo {
    private Integer idEquipo;
    private String nombre;
    private String tipo; // Eléctrico, Mecánico, Instrumentación, Hidráulico
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String ubicacionPlanta;
    private LocalDate fechaInstalacion;
    private String estadoOperativo;
    private String criticidad; // Baja, Media, Alta

    // Getters and Setters
    public Integer getIdEquipo() { return idEquipo; }
    public void setIdEquipo(Integer idEquipo) { this.idEquipo = idEquipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
    public String getUbicacionPlanta() { return ubicacionPlanta; }
    public void setUbicacionPlanta(String ubicacionPlanta) { this.ubicacionPlanta = ubicacionPlanta; }
    public LocalDate getFechaInstalacion() { return fechaInstalacion; }
    public void setFechaInstalacion(LocalDate fechaInstalacion) { this.fechaInstalacion = fechaInstalacion; }
    public String getEstadoOperativo() { return estadoOperativo; }
    public void setEstadoOperativo(String estadoOperativo) { this.estadoOperativo = estadoOperativo; }
    public String getCriticidad() { return criticidad; }
    public void setCriticidad(String criticidad) { this.criticidad = criticidad; }
}
