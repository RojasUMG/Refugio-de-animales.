package com.refugio.estructuras;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Busqueda binaria clasica O(log n) sobre una lista ya ordenada por la misma
 * clave (por eso SolicitudService siempre ordena con Ordenamiento.mergeSort
 * antes de buscar). Se usa para localizar una solicitud de adopcion por id
 * sin recorrer toda la lista.
 */
public final class BusquedaBinaria {

    private BusquedaBinaria() {
    }

    /**
     * @param lista            lista ordenada ascendentemente por la clave entera que devuelve `extractorDeClave`
     * @param claveBuscada     valor a buscar
     * @param extractorDeClave funcion que obtiene la clave entera de un elemento (p.ej. Solicitud::getId)
     */
    public static <T> T buscar(List<T> lista, int claveBuscada, ToIntFunction<T> extractorDeClave) {
        int izquierda = 0;
        int derecha = lista.size() - 1;
        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            int claveMedio = extractorDeClave.applyAsInt(lista.get(medio));
            if (claveMedio == claveBuscada) {
                return lista.get(medio);
            } else if (claveMedio < claveBuscada) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }
        return null;
    }

    public static <T> Comparator<T> comparadorPorClave(ToIntFunction<T> extractorDeClave) {
        return Comparator.comparingInt(extractorDeClave);
    }
}
