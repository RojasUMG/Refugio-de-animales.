package com.refugio.modelo;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;

/**
 * Modelo que representa un animal en el refugio.
 * Los campos coinciden con las columnas de la tabla `animales`.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Animal {

    public int    animalId;
    public String nombre;
    public int    especieId;
    public String especieNombre;   // campo calculado del JOIN con especies
    public String raza;
    public LocalDate fechaDeNacimiento;
    public String sexo;            // "Macho" | "Hembra"
    public String color;
    public String tamano;          // "Pequeño" | "Mediano" | "Grande"
    public String descripcion;
    public String estadoDeSalud;
    public String estadoDeAdopcion; // "Disponible" | "En proceso" | "Adoptado"
    public String urlDeImagen;
    public LocalDate fechaDeIngreso;

    public Animal() {}
}
