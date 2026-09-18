package com.refugio.estructuras;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Merge Sort escrito a mano (no Collections.sort) para ordenar listas segun
 * cualquier criterio, usado por SolicitudService para mostrar las solicitudes
 * de adopcion ordenadas por fecha. Complejidad O(n log n) garantizada, la
 * razon por la que se prefiere sobre un ordenamiento burbuja/insercion aqui.
 */
public final class Ordenamiento {

    private Ordenamiento() {
    }

    public static <T> List<T> mergeSort(List<T> lista, Comparator<T> comparador) {
        if (lista.size() <= 1) {
            return new ArrayList<>(lista);
        }
        int medio = lista.size() / 2;
        List<T> izquierda = mergeSort(lista.subList(0, medio), comparador);
        List<T> derecha = mergeSort(lista.subList(medio, lista.size()), comparador);
        return combinar(izquierda, derecha, comparador);
    }

    private static <T> List<T> combinar(List<T> izquierda, List<T> derecha, Comparator<T> comparador) {
        List<T> resultado = new ArrayList<>(izquierda.size() + derecha.size());
        int i = 0;
        int j = 0;
        while (i < izquierda.size() && j < derecha.size()) {
            if (comparador.compare(izquierda.get(i), derecha.get(j)) <= 0) {
                resultado.add(izquierda.get(i++));
            } else {
                resultado.add(derecha.get(j++));
            }
        }
        while (i < izquierda.size()) {
            resultado.add(izquierda.get(i++));
        }
        while (j < derecha.size()) {
            resultado.add(derecha.get(j++));
        }
        return resultado;
    }
}
