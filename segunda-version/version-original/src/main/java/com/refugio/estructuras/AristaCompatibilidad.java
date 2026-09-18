package com.refugio.estructuras;

import com.refugio.modelo.Animal;

/** Una arista del grafo adoptante-animal, con el puntaje de compatibilidad como peso. */
public class AristaCompatibilidad {
    public Animal animal;
    public int peso; // 0 a 100: que tan compatible es el animal con las preferencias del adoptante

    public AristaCompatibilidad(Animal animal, int peso) {
        this.animal = animal;
        this.peso = peso;
    }
}
