package com.refugio.estructuras;

import java.util.Comparator;
import java.util.List;

/**
 * Búsqueda Binaria implementada desde cero.
 *
 * Se usa en SolicitudService para localizar una solicitud por ID
 * sobre la lista ya ordenada (Merge Sort primero, luego buscar aquí).
 * Complejidad: O(log n).
 *
 * @param <T> tipo de los elementos en la lista
 */
public class BusquedaBinaria<T> {

    /**
     * Busca un elemento en una lista ordenada.
     *
     * @param lista      lista ordenada por el mismo comparador
     * @param objetivo   elemento a buscar
     * @param comparador criterio de comparación
     * @return índice del elemento si se encuentra, -1 si no existe
     */
    public int buscar(List<T> lista, T objetivo, Comparator<T> comparador) {
        int inicio = 0;
        int fin    = lista.size() - 1;

        while (inicio <= fin) {
            int medio = inicio + (fin - inicio) / 2;   // evita desbordamiento
            int cmp   = comparador.compare(lista.get(medio), objetivo);

            if      (cmp == 0) return medio;   // encontrado
            else if (cmp < 0)  inicio = medio + 1;
            else               fin    = medio - 1;
        }

        return -1;   // no encontrado
    }

    /**
     * Versión que devuelve directamente el elemento (o null si no existe).
     */
    public T encontrar(List<T> lista, T objetivo, Comparator<T> comparador) {
        int idx = buscar(lista, objetivo, comparador);
        return idx >= 0 ? lista.get(idx) : null;
    }
}
