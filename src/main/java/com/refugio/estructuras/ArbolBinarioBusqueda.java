package com.refugio.estructuras;

import java.util.ArrayList;
import java.util.List;

/**
 * Árbol Binario de Búsqueda (BST) genérico implementado desde cero.
 *
 * Se usa en AnimalService para listar animales en orden alfabético
 * (recorrido en-orden), con inserción O(log n) promedio.
 *
 * @param <T> tipo comparable que se almacena en el árbol
 */
public class ArbolBinarioBusqueda<T extends Comparable<T>> {

    private Nodo<T> raiz;

    private static class Nodo<T> {
        T dato;
        Nodo<T> izquierdo;
        Nodo<T> derecho;

        Nodo(T dato) { this.dato = dato; }
    }

    // ── Inserción ─────────────────────────────────────────────────────────────

    /** Inserta un elemento en el árbol. O(log n) promedio. */
    public void insertar(T dato) {
        raiz = insertarRecursivo(raiz, dato);
    }

    private Nodo<T> insertarRecursivo(Nodo<T> nodo, T dato) {
        if (nodo == null) return new Nodo<>(dato);

        int cmp = dato.compareTo(nodo.dato);
        if      (cmp < 0) nodo.izquierdo = insertarRecursivo(nodo.izquierdo, dato);
        else if (cmp > 0) nodo.derecho   = insertarRecursivo(nodo.derecho,   dato);
        // si cmp == 0, el elemento ya existe; no se insertan duplicados
        return nodo;
    }

    // ── Búsqueda ──────────────────────────────────────────────────────────────

    /** ¿Existe el elemento en el árbol? O(log n) promedio. */
    public boolean contiene(T dato) {
        Nodo<T> actual = raiz;
        while (actual != null) {
            int cmp = dato.compareTo(actual.dato);
            if      (cmp < 0) actual = actual.izquierdo;
            else if (cmp > 0) actual = actual.derecho;
            else return true;
        }
        return false;
    }

    // ── Recorridos ────────────────────────────────────────────────────────────

    /**
     * Recorrido en-orden (izquierdo → raíz → derecho).
     * Devuelve los elementos ordenados de menor a mayor.
     */
    public List<T> enOrden() {
        List<T> resultado = new ArrayList<>();
        enOrdenRecursivo(raiz, resultado);
        return resultado;
    }

    private void enOrdenRecursivo(Nodo<T> nodo, List<T> resultado) {
        if (nodo == null) return;
        enOrdenRecursivo(nodo.izquierdo, resultado);
        resultado.add(nodo.dato);
        enOrdenRecursivo(nodo.derecho, resultado);
    }

    /** Recorrido pre-orden (raíz → izquierdo → derecho). */
    public List<T> preOrden() {
        List<T> resultado = new ArrayList<>();
        preOrdenRecursivo(raiz, resultado);
        return resultado;
    }

    private void preOrdenRecursivo(Nodo<T> nodo, List<T> resultado) {
        if (nodo == null) return;
        resultado.add(nodo.dato);
        preOrdenRecursivo(nodo.izquierdo, resultado);
        preOrdenRecursivo(nodo.derecho, resultado);
    }

    // ── Eliminación ───────────────────────────────────────────────────────────

    /** Elimina un elemento del árbol. O(log n) promedio. */
    public void eliminar(T dato) {
        raiz = eliminarRecursivo(raiz, dato);
    }

    private Nodo<T> eliminarRecursivo(Nodo<T> nodo, T dato) {
        if (nodo == null) return null;

        int cmp = dato.compareTo(nodo.dato);
        if (cmp < 0) {
            nodo.izquierdo = eliminarRecursivo(nodo.izquierdo, dato);
        } else if (cmp > 0) {
            nodo.derecho = eliminarRecursivo(nodo.derecho, dato);
        } else {
            // Nodo encontrado
            if (nodo.izquierdo == null) return nodo.derecho;
            if (nodo.derecho   == null) return nodo.izquierdo;

            // Reemplazar por el sucesor in-orden (mínimo del subárbol derecho)
            Nodo<T> sucesor = minimoNodo(nodo.derecho);
            nodo.dato   = sucesor.dato;
            nodo.derecho = eliminarRecursivo(nodo.derecho, sucesor.dato);
        }
        return nodo;
    }

    private Nodo<T> minimoNodo(Nodo<T> nodo) {
        while (nodo.izquierdo != null) nodo = nodo.izquierdo;
        return nodo;
    }

    public boolean estaVacio() { return raiz == null; }
}
