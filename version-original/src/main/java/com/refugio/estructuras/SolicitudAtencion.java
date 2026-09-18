package com.refugio.estructuras;

import com.refugio.modelo.Animal;

import java.time.LocalDateTime;

/** Un animal esperando revision veterinaria, con su nivel de urgencia calculado. */
public class SolicitudAtencion {
    public Animal animal;
    public NivelUrgencia nivelUrgencia;
    public String motivo;
    public LocalDateTime registradoEl;

    public SolicitudAtencion(Animal animal, NivelUrgencia nivelUrgencia, String motivo, LocalDateTime registradoEl) {
        this.animal = animal;
        this.nivelUrgencia = nivelUrgencia;
        this.motivo = motivo;
        this.registradoEl = registradoEl;
    }
}
