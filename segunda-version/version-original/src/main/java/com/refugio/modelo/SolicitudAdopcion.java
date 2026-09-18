package com.refugio.modelo;

import java.time.LocalDateTime;

public class SolicitudAdopcion {
    public int solicitudId;
    public int solicitanteId;
    public int animalId;
    public LocalDateTime fechaDeSolicitud;
    public String estado; // "pendiente", "aprobada", "rechazada"
    public String motivacion;
    public String notas;
    public Integer revisadoPor;
    public LocalDateTime revisadoEl;

    // Campos extra para que el frontend no tenga que hacer joins manuales
    public String animalNombre;
    public String solicitanteNombre;
}
