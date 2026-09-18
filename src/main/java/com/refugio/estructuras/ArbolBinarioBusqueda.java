package com.refugio.estructuras;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Arbol Binario de Busqueda (BST) generico. Se usa para clasificar y ordenar
 * animales por una clave comparable (por ejemplo el nombre): todo lo menor
 * que un nodo cae a su izquierda, todo lo mayor cae a su derecha. Un
 * recorrido "in-order" entrega los elementos ya ordenados, sin usar
 * Collections.sort ni Arrays.sort.
 *
 * @param <K> tipo de la clave de clasificacion (debe ser Comparable), p.ej. String
 * @param <V> tipo del valor guardado, p.ej. Animal
 */
public class ArbolBinarioBusqueda<K extends Comparable<K>, V> {

    private class Nodo {
        K clave;
        V valor;
        Nodo izquierda;
        Nodo derecha;

        Nodo(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }

    private Nodo raiz;
    private int cantidad;

    public void insertar(K clave, V valor) {
        raiz = insertarRecursivo(raiz, clave, valor);
    }

    private Nodo insertarRecursivo(Nodo nodo, K clave, V valor) {
        if (nodo == null) {
            cantidad++;
            return new Nodo(clave, valor);
        }
        int comparacion = clave.compareTo(nodo.clave);
        if (comparacion < 0) {
            nodo.izquierda = insertarRecursivo(nodo.izquierda, clave, valor);
        } else if (comparacion > 0) {
            nodo.derecha = insertarRecursivo(nodo.derecha, clave, valor);
        } else {
            nodo.valor = valor; // clave duplicada: se reemplaza el valor
        }
        return nodo;
    }

    public V buscar(K clave) {
        Nodo actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.clave);
            if (comparacion == 0) {
                return actual.valor;
            }
            actual = comparacion < 0 ? actual.izquierda : actual.derecha;
        }
        return null;
    }

    /** Recorrido in-order: entrega todos los valores ordenados ascendentemente por clave. */
    public List<V> enOrden() {
        List<V> resultado = new ArrayList<>();
        enOrdenRecursivo(raiz, resultado);
        return resultado;
    }

    private void enOrdenRecursivo(Nodo nodo, List<V> acumulador) {
        if (nodo == null) {
            return;
        }
        enOrdenRecursivo(nodo.izquierda, acumulador);
        acumulador.add(nodo.valor);
        enOrdenRecursivo(nodo.derecha, acumulador);
    }

    public int tamano() {
        return cantidad;
    }

    /** Construye un arbol a partir de una lista, usando `extractorDeClave` para saber por que campo ordenar. */
    public static <K extends Comparable<K>, V> ArbolBinarioBusqueda<K, V> construirDesde(
            List<V> elementos, Function<V, K> extractorDeClave) {
        ArbolBinarioBusqueda<K, V> arbol = new ArbolBinarioBusqueda<>();
        for (V elemento : elementos) {
            arbol.insertar(extractorDeClave.apply(elemento), elemento);
        }
        return arbol;
    }
}
