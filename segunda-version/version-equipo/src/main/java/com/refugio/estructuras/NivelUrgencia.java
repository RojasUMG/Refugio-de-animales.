package com.refugio.estructuras;

/** Menor numero = mayor urgencia. Se usa como criterio de la cola de prioridad. */
public enum NivelUrgencia {
    URGENTE(1),
    MODERADO(2),
    NORMAL(3);

    public final int peso;

    NivelUrgencia(int peso) {
        this.peso = peso;
    }
}
