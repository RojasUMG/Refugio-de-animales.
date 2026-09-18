package com.refugio.estructuras;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabla hash propia (encadenamiento separado) para busqueda rapida O(1) en
 * promedio. No usamos java.util.HashMap a proposito: la idea del curso es
 * mostrar como funciona el hashing por dentro (funcion hash + manejo de
 * colisiones con listas encadenadas), no solo usar la de la libreria estandar.
 *
 * Se usa en AnimalService para indexar animales por id y por nombre.
 */
public class TablaHash<K, V> {

    /** Un "cubo" (bucket) es una lista de nodos que colisionaron en el mismo indice. */
    private static class Nodo<K, V> {
        K clave;
        V valor;
        Nodo<K, V> siguiente;

        Nodo(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }

    private Nodo<K, V>[] cubos;
    private int cantidadElementos;
    private static final double FACTOR_DE_CARGA_MAXIMO = 0.75;

    @SuppressWarnings("unchecked")
    public TablaHash() {
        cubos = new Nodo[16];
        cantidadElementos = 0;
    }

    private int indiceParaClave(K clave, int capacidad) {
        // & 0x7fffffff descarta el signo para que el indice nunca sea negativo.
        return (clave.hashCode() & 0x7fffffff) % capacidad;
    }

    public void insertar(K clave, V valor) {
        if ((double) (cantidadElementos + 1) / cubos.length > FACTOR_DE_CARGA_MAXIMO) {
            redimensionar();
        }
        int indice = indiceParaClave(clave, cubos.length);
        Nodo<K, V> actual = cubos[indice];
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                actual.valor = valor; // clave repetida: se actualiza el valor
                return;
            }
            actual = actual.siguiente;
        }
        Nodo<K, V> nuevo = new Nodo<>(clave, valor);
        nuevo.siguiente = cubos[indice];
        cubos[indice] = nuevo;
        cantidadElementos++;
    }

    public V buscar(K clave) {
        int indice = indiceParaClave(clave, cubos.length);
        Nodo<K, V> actual = cubos[indice];
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                return actual.valor;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    public boolean contiene(K clave) {
        return buscar(clave) != null;
    }

    public List<V> valores() {
        List<V> lista = new ArrayList<>();
        for (Nodo<K, V> cubo : cubos) {
            Nodo<K, V> actual = cubo;
            while (actual != null) {
                lista.add(actual.valor);
                actual = actual.siguiente;
            }
        }
        return lista;
    }

    public int tamano() {
        return cantidadElementos;
    }

    @SuppressWarnings("unchecked")
    private void redimensionar() {
        Nodo<K, V>[] viejos = cubos;
        cubos = new Nodo[viejos.length * 2];
        cantidadElementos = 0;
        for (Nodo<K, V> cubo : viejos) {
            Nodo<K, V> actual = cubo;
            while (actual != null) {
                insertar(actual.clave, actual.valor);
                actual = actual.siguiente;
            }
        }
    }
}
