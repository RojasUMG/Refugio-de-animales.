package com.refugio.servicio;

import com.refugio.dao.AnimalDAO;
import com.refugio.estructuras.ColaPrioridadVeterinaria;
import com.refugio.modelo.Animal;

import java.util.List;

/**
 * Capa de negocio para la gestión veterinaria.
 *
 * Estructura de datos utilizada:
 *  - ColaPrioridadVeterinaria (max-heap) → clasifica animales por urgencia médica
 *
 * Prioridades:
 *  3 = URGENTE  (urgente, crítico, emergencia, grave)
 *  2 = MODERADO (enfermo, tratamiento, observación)
 *  1 = NORMAL   (cualquier otro estado)
 */
public class VeterinariaService {

    private final AnimalDAO dao = new AnimalDAO();

    /**
     * Obtiene la cola de atención veterinaria con todos los animales
     * del refugio ordenados por urgencia de mayor a menor.
     */
    public List<ColaPrioridadVeterinaria.ElementoCola> obtenerColaDeAtencion() throws Exception {
        List<Animal> animales = dao.obtenerTodos();

        ColaPrioridadVeterinaria cola = new ColaPrioridadVeterinaria();
        for (Animal a : animales) {
            cola.insertar(a);
        }

        // Devuelve todos en orden de prioridad (URGENTE primero)
        return cola.obtenerOrdenada();
    }
}
