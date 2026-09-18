package com.refugio.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

public class Usuario {
    public int usuarioId;
    public String primerNombre;
    public String apellido;
    public String email;

    // @JsonIgnore evita que la contrasena viaje en las respuestas JSON de la API.
    @JsonIgnore
    public String contrasena;

    // La columna real "teléfono" es INT en la base de datos (no VARCHAR), asi que
    // no admite espacios, guiones ni un "+" de codigo de pais.
    public Integer telefono;
    public String rol;
    public boolean estado;
    public LocalDateTime creadoEn;
}
