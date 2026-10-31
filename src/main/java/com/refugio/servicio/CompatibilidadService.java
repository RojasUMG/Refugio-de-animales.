package com.refugio.servicio;

import com.refugio.dao.AnimalDAO;
import com.refugio.dao.PreferenciaAdoptanteDAO;
import com.refugio.estructuras.GrafoCompatibilidad;
import com.refugio.modelo.Animal;

import java.util.List;
import java.util.Map;

/**
 * Capa de negocio para sugerencias de compatibilidad.
 *
 * Estructura de datos utilizada:
 *  - GrafoCompatibilidad → grafo ponderado no dirigido donde los nodos son
 *    animales disponibles y las aristas tienen peso = score de compatibilidad
 *    con el adoptante (0-100).
 *
 * El servicio:
 *  1. Lee las preferencias del adoptante desde la BD.
 *  2. Lee todos los animales disponibles.
 *  3. Construye el grafo calculando el peso de cada arista.
 *  4. Devuelve los `maximo` animales con mayor peso.
 */
public class CompatibilidadService {

    private final AnimalDAO dao                   = new AnimalDAO();
    private final PreferenciaAdoptanteDAO prefDao = new PreferenciaAdoptanteDAO();

    /**
     * Sugiere animales compatibles para un usuario registrado.
     *
     * @param usuarioId ID del usuario adoptante
     * @param maximo    cuántos candidatos devolver (p. ej. 3)
     */
    public List<GrafoCompatibilidad.Arista> sugerirAnimales(int usuarioId, int maximo) throws Exception {
        // 1. Preferencias del adoptante
        Map<String, String> preferencias = prefDao.obtenerPorUsuario(usuarioId);

        // 2. Animales disponibles
        List<Animal> disponibles = dao.obtenerPorEstado("Disponible");

        // 3. Construir el grafo
        GrafoCompatibilidad grafo = new GrafoCompatibilidad();
        for (Animal a : disponibles) {
            grafo.agregarArista(a, preferencias);
        }

        // 4. Ordenar por compatibilidad y limitar
        List<GrafoCompatibilidad.Arista> ordenadas = grafo.obtenerOrdenadas();
        return ordenadas.size() > maximo ? ordenadas.subList(0, maximo) : ordenadas;
    }
}
