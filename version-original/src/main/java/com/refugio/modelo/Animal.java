package com.refugio.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Animal {
    public int animalId;
    public String numeroDeIdentificacion;
    public String nombre;
    public int especiesId;
    public String raza;
    public String sexo;
    public LocalDate fechaDeNacimiento;
    public LocalDate fechaDeLlegada;
    public String tamano;
    public String color;
    public String descripcion;
    public String estadoDeSalud;
    public String estadoDeAdopcion;
    public String urlDeImagen;
    public LocalDateTime creadoEl;

    // Campo extra (no esta en la tabla animales): se llena con un JOIN
    // en AnimalDAO para que el frontend no tenga que pedir el nombre
    // de la especie por separado.
    public String especieNombre;
}
