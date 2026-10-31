package com.refugio.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Modelo que representa a un usuario del sistema.
 * La contraseña se omite en las respuestas JSON por seguridad.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Usuario {

    public int    usuarioId;
    public String primerNombre;
    public String apellido;
    public String email;

    @JsonIgnore
    public String contrasena;      // nunca se serializa a JSON

    public Integer telefono;
    public String  rol;            // "admin" | "veterinario" | "adoptante"
    public boolean estado;         // true = activo
    public LocalDateTime creadoEn;

    public Usuario() {}
}
