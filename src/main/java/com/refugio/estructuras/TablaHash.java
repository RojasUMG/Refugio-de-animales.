package com.refugio.estructuras;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Tabla Hash implementada desde cero con encadenamiento (chaining).
 *
 * Se usa en AnimalService para búsquedas O(1) por ID o nombre.
 * La clave K se convierte a índice mediante hashCode() % capacidad.
 *
 * @param <K> tipo de la clave
 * @param <V> tipo del valor
 */
public class TablaHash<K, V> {

    private static final int CAPACIDAD_DEFECTO = 64;

    private final int capacidad;
    @SuppressWarnings("unchecked")
    private final LinkedList<Entrada<K, V>>[] cubetas;
    private int tamanio;

    /** Par clave-valor almacenado en cada cubeta. */
    private static class Entrada<K, V> {
        K clave;
        V valor;

        Entrada(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }

    public TablaHash() {
        this(CAPACIDAD_DEFECTO);
    }

    @SuppressWarnings("unchecked")
    public TablaHash(int capacidad) {
        this.capacidad = capacidad;
        this.cubetas   = new LinkedList[capacidad];
    }

    // ── Operaciones básicas ───────────────────────────────────────────────────

    /** Inserta o actualiza el par (clave, valor). O(1) amortizado. */
    public void insertar(K clave, V valor) {
        int idx = indice(clave);
        if (cubetas[idx] == null) cubetas[idx] = new LinkedList<>();

        for (Entrada<K, V> e : cubetas[idx]) {
            if (e.clave.equals(clave)) {
                e.valor = valor;   // actualizar
                return;
            }
        }
        cubetas[idx].add(new Entrada<>(clave, valor));
        tamanio++;
    }

    /** Recupera el valor asociado a la clave, o null si no existe. O(1). */
    public V obtener(K clave) {
        int idx = indice(clave);
        if (cubetas[idx] == null) return null;
        for (Entrada<K, V> e : cubetas[idx]) {
            if (e.clave.equals(clave)) return e.valor;
        }
        return null;
    }

    /** Elimina la entrada con la clave dada. O(1). */
    public void eliminar(K clave) {
        int idx = indice(clave);
        if (cubetas[idx] == null) return;
        cubetas[idx].removeIf(e -> e.clave.equals(clave));
    }

    /** ¿Existe la clave en la tabla? */
    public boolean contiene(K clave) {
        return obtener(clave) != null;
    }

    /** Devuelve todos los valores almacenados. */
    public List<V> valores() {
        List<V> resultado = new ArrayList<>(tamanio);
        for (LinkedList<Entrada<K, V>> cubeta : cubetas) {
            if (cubeta != null) {
                for (Entrada<K, V> e : cubeta) resultado.add(e.valor);
            }
        }
        return resultado;
    }

    public int tamanio() { return tamanio; }

    // ── Privado ───────────────────────────────────────────────────────────────

    private int indice(K clave) {
        return Math.abs(clave.hashCode() % capacidad);
    }
}
