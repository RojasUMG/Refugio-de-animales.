package com.refugio.estructuras;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Cola de prioridad para decidir en que orden se atiende a los animales:
 * primero los URGENTE, luego MODERADO, luego NORMAL; dentro del mismo nivel,
 * el que lleva mas tiempo esperando (fecha de registro mas antigua) sale primero.
 *
 * Java trae PriorityQueue como heap binario listo para usar; aqui se le da
 * el Comparator con la regla de negocio (urgencia > antiguedad) que pide el curso.
 */
public class ColaPrioridadVeterinaria {

    private final PriorityQueue<SolicitudAtencion> heap = new PriorityQueue<>(
            Comparator.<SolicitudAtencion>comparingInt(s -> s.nivelUrgencia.peso)
                    .thenComparing(s -> s.registradoEl));

    public void agregar(SolicitudAtencion solicitud) {
        heap.add(solicitud);
    }

    public SolicitudAtencion atenderSiguiente() {
        return heap.poll();
    }

    public boolean estaVacia() {
        return heap.isEmpty();
    }

    /** Devuelve el orden de atencion completo sin vaciar la cola original. */
    public List<SolicitudAtencion> ordenDeAtencion() {
        PriorityQueue<SolicitudAtencion> copia = new PriorityQueue<>(heap);
        List<SolicitudAtencion> resultado = new ArrayList<>();
        while (!copia.isEmpty()) {
            resultado.add(copia.poll());
        }
        return resultado;
    }
}
