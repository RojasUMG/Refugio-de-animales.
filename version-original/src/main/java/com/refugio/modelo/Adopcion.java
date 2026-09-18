package com.refugio.modelo;

import java.time.LocalDateTime;

public class Adopcion {
    public int adopcionId;
    public int solicitudId;
    public int animalId;
    public int adoptanteId;
    public LocalDateTime fechaDeAdopcion;
    public String notas;

    public String animalNombre;
    public String adoptanteNombre;
}
