package com.refugio.modelo;

import java.time.LocalDateTime;

public class RegistroMedico {
    public int registroMedicoId;
    public int animalId;
    public int veterinarioId;
    public LocalDateTime fechaDeVisita;
    public String razon;
    public String diagnostico;
    public String tratamiento;
    public String observaciones;
}
