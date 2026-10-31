package com.refugio.servicio;

import com.refugio.dao.AnimalDAO;
import com.refugio.estructuras.ArbolBinarioBusqueda;
import com.refugio.estructuras.TablaHash;
import com.refugio.modelo.Animal;

import java.util.List;

/**
 * Capa de negocio para animales.
 *
 * Estructuras de datos utilizadas:
 *  - TablaHash<Integer, Animal>  → búsqueda O(1) por ID
 *  - TablaHash<String, Animal>   → búsqueda O(1) por nombre
 *  - ArbolBinarioBusqueda<String> → listado en orden alfabético O(log n) inserción
 */
public class AnimalService {

    private final AnimalDAO dao = new AnimalDAO();

    // ── Cache en estructuras de datos ─────────────────────────────────────────

    private TablaHash<Integer, Animal> tablaHashPorId;
    private TablaHash<String,  Animal> tablaHashPorNombre;
    private ArbolBinarioBusqueda<String> arbolNombres;

    /** Carga todos los animales en las estructuras. Llama una vez al inicio. */
    public void inicializar() throws Exception {
        List<Animal> animales = dao.obtenerTodos();

        tablaHashPorId     = new TablaHash<>(animales.size() * 2 + 1);
        tablaHashPorNombre = new TablaHash<>(animales.size() * 2 + 1);
        arbolNombres       = new ArbolBinarioBusqueda<>();

        for (Animal a : animales) {
            tablaHashPorId.insertar(a.animalId, a);
            tablaHashPorNombre.insertar(a.nombre.toLowerCase(), a);
            arbolNombres.insertar(a.nombre.toLowerCase());
        }
    }

    // ── Lectura ───────────────────────────────────────────────────────────────

    /** Devuelve todos los animales directamente de la BD. */
    public List<Animal> listarTodos() throws Exception {
        return dao.obtenerTodos();
    }

    /**
     * Búsqueda por ID usando la Tabla Hash → O(1).
     * Si no está en caché, consulta la BD.
     */
    public Animal buscarPorId(int id) throws Exception {
        if (tablaHashPorId != null && tablaHashPorId.contiene(id)) {
            return tablaHashPorId.obtener(id);
        }
        return dao.obtenerPorId(id);
    }

    /**
     * Búsqueda por nombre usando la Tabla Hash → O(1).
     */
    public Animal buscarPorNombre(String nombre) throws Exception {
        if (tablaHashPorNombre != null) {
            Animal a = tablaHashPorNombre.obtener(nombre.toLowerCase());
            if (a != null) return a;
        }
        // Fallback: buscar en la lista completa
        for (Animal a : dao.obtenerTodos()) {
            if (a.nombre.equalsIgnoreCase(nombre)) return a;
        }
        return null;
    }

    /**
     * Lista animales en orden alfabético usando el recorrido en-orden del BST.
     */
    public List<Animal> listarOrdenadosPorNombre() throws Exception {
        if (arbolNombres == null) inicializar();
        List<String> nombresOrdenados = arbolNombres.enOrden();
        // Recuperar cada animal por nombre en el orden del árbol
        return nombresOrdenados.stream()
            .map(n -> {
                try { return buscarPorNombre(n); }
                catch (Exception e) { return null; }
            })
            .filter(a -> a != null)
            .toList();
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    public Animal crear(Animal a) throws Exception {
        Animal creado = dao.crear(a);
        // Actualizar estructuras en memoria
        if (tablaHashPorId != null) {
            tablaHashPorId.insertar(creado.animalId, creado);
            tablaHashPorNombre.insertar(creado.nombre.toLowerCase(), creado);
            arbolNombres.insertar(creado.nombre.toLowerCase());
        }
        return creado;
    }

    public void actualizarEstado(int id, String nuevoEstado) throws Exception {
        dao.actualizarEstado(id, nuevoEstado);
        // Invalidar entrada en caché
        if (tablaHashPorId != null) {
            Animal actualizado = dao.obtenerPorId(id);
            if (actualizado != null) tablaHashPorId.insertar(id, actualizado);
        }
    }
}
