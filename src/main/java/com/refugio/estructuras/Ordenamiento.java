package com.refugio.estructuras;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Algoritmo de ordenamiento Merge Sort implementado desde cero.
 *
 * Se usa en SolicitudService para ordenar solicitudes por fecha.
 * Complejidad: O(n log n) en todos los casos.
 *
 * @param <T> tipo de los elementos a ordenar
 */
public class Ordenamiento<T> {

    /**
     * Ordena la lista usando Merge Sort y el comparador dado.
     * @param lista      lista de elementos a ordenar
     * @param comparador criterio de ordenamiento
     * @return nueva lista ordenada (la original no se modifica)
     */
    public List<T> mergeSort(List<T> lista, Comparator<T> comparador) {
        if (lista.size() <= 1) return new ArrayList<>(lista);
        return dividir(lista, comparador);
    }

    // ── Fase de división ──────────────────────────────────────────────────────

    private List<T> dividir(List<T> lista, Comparator<T> cmp) {
        if (lista.size() <= 1) return new ArrayList<>(lista);

        int medio = lista.size() / 2;
        List<T> izquierda = dividir(lista.subList(0, medio), cmp);
        List<T> derecha   = dividir(lista.subList(medio, lista.size()), cmp);

        return combinar(izquierda, derecha, cmp);
    }

    // ── Fase de combinación (merge) ───────────────────────────────────────────

    private List<T> combinar(List<T> izquierda, List<T> derecha, Comparator<T> cmp) {
        List<T> resultado = new ArrayList<>(izquierda.size() + derecha.size());
        int i = 0, j = 0;

        while (i < izquierda.size() && j < derecha.size()) {
            if (cmp.compare(izquierda.get(i), derecha.get(j)) <= 0) {
                resultado.add(izquierda.get(i++));
            } else {
                resultado.add(derecha.get(j++));
            }
        }

        // Agregar los elementos restantes
        while (i < izquierda.size()) resultado.add(izquierda.get(i++));
        while (j < derecha.size())   resultado.add(derecha.get(j++));

        return resultado;
    }
}
