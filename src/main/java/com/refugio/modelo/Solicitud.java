package com.refugio.modelo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Modelo para una solicitud de adopción.
 * Los campos con "Nombre" son calculados por JOIN en el DAO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Solicitud {

    public int    solicitudId;
    public int    solicitanteId;
    public int    animalId;
    public LocalDateTime fechaDeSolicitud;
    public String estado;          // "pendiente" | "aprobada" | "rechazada"
    public String motivacion;
    public String notas;
    public Integer revisadoPor;
    public LocalDateTime revisadoEl;

    // Campos calculados por JOIN (solo lectura)
    public String animalNombre;
    public String solicitanteNombre;

    public Solicitud() {}
}
