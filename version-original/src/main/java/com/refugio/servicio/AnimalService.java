package com.refugio.servicio;

import com.refugio.dao.AnimalDAO;
import com.refugio.estructuras.ArbolBinarioBusqueda;
import com.refugio.estructuras.TablaHash;
import com.refugio.modelo.Animal;

import java.util.List;

/**
 * Capa que junta el acceso a datos (AnimalDAO) con las estructuras del curso:
 * una TablaHash para busqueda O(1) por id/nombre, y un ArbolBinarioBusqueda
 * para listar animales ordenados alfabeticamente.
 *
 * El indice se reconstruye en cada llamada porque el catalogo de animales de
 * un refugio es pequeno (decenas o cientos de registros); en un sistema con
 * miles de animales convendria cachear y refrescar el indice solo cuando hay
 * cambios.
 */
public class AnimalService {

    private final AnimalDAO animalDAO = new AnimalDAO();

    public List<Animal> listarConFiltros(AnimalDAO.Filtros filtros) {
        return animalDAO.buscarConFiltros(filtros);
    }

    public int registrar(Animal animal) {
        return animalDAO.crear(animal);
    }

    public void actualizarEstadoDeAdopcion(int animalId, String nuevoEstado) {
        animalDAO.actualizarEstadoDeAdopcion(animalId, nuevoEstado);
    }

    /** Busqueda O(1) por id, usando una tabla hash construida con todos los animales. */
    public Animal buscarPorId(int animalId) {
        TablaHash<Integer, Animal> indice = construirIndicePorId();
        return indice.buscar(animalId);
    }

    /** Busqueda O(1) por nombre exacto (sin distinguir mayusculas/minusculas). */
    public Animal buscarPorNombre(String nombre) {
        TablaHash<String, Animal> indice = construirIndicePorNombre();
        return indice.buscar(nombre.toLowerCase());
    }

    private TablaHash<Integer, Animal> construirIndicePorId() {
        TablaHash<Integer, Animal> tabla = new TablaHash<>();
        for (Animal animal : animalDAO.obtenerTodos()) {
            tabla.insertar(animal.animalId, animal);
        }
        return tabla;
    }

    private TablaHash<String, Animal> construirIndicePorNombre() {
        TablaHash<String, Animal> tabla = new TablaHash<>();
        for (Animal animal : animalDAO.obtenerTodos()) {
            tabla.insertar(animal.nombre.toLowerCase(), animal);
        }
        return tabla;
    }

    /** Lista todos los animales ordenados alfabeticamente, usando un BST y su recorrido in-order. */
    public List<Animal> listarOrdenadosPorNombre() {
        List<Animal> animales = animalDAO.obtenerTodos();
        ArbolBinarioBusqueda<String, Animal> arbol =
                ArbolBinarioBusqueda.construirDesde(animales, a -> a.nombre.toLowerCase() + "#" + a.animalId);
        return arbol.enOrden();
    }
}
