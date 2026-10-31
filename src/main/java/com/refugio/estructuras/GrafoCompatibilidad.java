package com.refugio.estructuras;

import com.refugio.modelo.Animal;

import java.util.*;

/**
 * Grafo no dirigido y ponderado que modela la compatibilidad
 * entre un usuario y los animales disponibles.
 *
 * Los nodos son animales; las aristas tienen un peso = score de compatibilidad
 * calculado al comparar las preferencias del adoptante con los atributos del animal.
 *
 * Se usa en CompatibilidadService para sugerir los mejores candidatos.
 */
public class GrafoCompatibilidad {

    /** Arista del grafo: animal destino + peso de compatibilidad. */
    public static class Arista {
        public final Animal animal;
        public final int    peso;   // 0–100
        public final List<String> razones;

        public Arista(Animal animal, int peso, List<String> razones) {
            this.animal  = animal;
            this.peso    = peso;
            this.razones = razones;
        }
    }

    // Lista de adyacencia: nodo "usuario" → lista de aristas hacia animales
    private final List<Arista> aristas = new ArrayList<>();

    // ── Construcción del grafo ────────────────────────────────────────────────

    /**
     * Agrega una arista calculando la compatibilidad entre las preferencias
     * del adoptante y el animal.
     *
     * @param animal      candidato a adopción (debe estar "Disponible")
     * @param preferencias mapa clave→valor de las preferencias del adoptante
     */
    public void agregarArista(Animal animal, Map<String, String> preferencias) {
        int peso = 0;
        List<String> razones = new ArrayList<>();

        // Especie preferida
        String espPref = preferencias.getOrDefault("especie", "Cualquiera");
        if ("Cualquiera".equalsIgnoreCase(espPref) ||
            espPref.equalsIgnoreCase(animal.especieNombre)) {
            peso += 35;
            razones.add("tipo de mascota");
        }

        // Tamaño
        String tamPref = preferencias.getOrDefault("tamano", "");
        if (tamPref.isEmpty() || tamPref.equalsIgnoreCase(animal.tamano)) {
            peso += 20;
            razones.add("tamaño");
        }

        // Compatibilidad con niños (campo en descripcion/estadoDeSalud)
        String ninosPref = preferencias.getOrDefault("ninos", "No");
        // Si el adoptante no tiene niños, el animal siempre es compatible
        if ("No".equalsIgnoreCase(ninosPref)) {
            peso += 15;
            razones.add("no hay niños en casa");
        } else {
            // Con niños: aceptamos al animal si su descripción no indica restricción
            String desc = (animal.descripcion + " " + animal.estadoDeSalud).toLowerCase();
            if (!desc.contains("no apto para niños") && !desc.contains("sin niños")) {
                peso += 15;
                razones.add("convivencia con niños");
            }
        }

        // Compatibilidad con otras mascotas
        String mascPref = preferencias.getOrDefault("otrasMascotas", "No");
        if ("No".equalsIgnoreCase(mascPref)) {
            peso += 15;
            razones.add("no hay otras mascotas");
        } else {
            String desc = (animal.descripcion + " " + animal.estadoDeSalud).toLowerCase();
            if (!desc.contains("no apto con mascotas") && !desc.contains("animal único")) {
                peso += 15;
                razones.add("convivencia con otras mascotas");
            }
        }

        // Actividad (vivienda / tiempo disponible)
        String vivPref = preferencias.getOrDefault("vivienda", "");
        if (!vivPref.isEmpty()) {
            // Animales grandes → mejor con casa
            boolean esGrande = "Grande".equalsIgnoreCase(animal.tamano);
            if (!esGrande || "Casa".equalsIgnoreCase(vivPref)) {
                peso += 15;
                razones.add("tipo de hogar");
            }
        } else {
            peso += 15;
        }

        aristas.add(new Arista(animal, peso, razones));
    }

    // ── Consulta ──────────────────────────────────────────────────────────────

    /**
     * Devuelve las aristas ordenadas de mayor a menor compatibilidad.
     * Usa un ordenamiento por selección para demostrar la estructura del grafo.
     */
    public List<Arista> obtenerOrdenadas() {
        List<Arista> resultado = new ArrayList<>(aristas);

        // Ordenamiento por selección (simple, para demostrar el algoritmo)
        for (int i = 0; i < resultado.size() - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < resultado.size(); j++) {
                if (resultado.get(j).peso > resultado.get(maxIdx).peso) maxIdx = j;
            }
            if (maxIdx != i) {
                Arista tmp = resultado.get(i);
                resultado.set(i, resultado.get(maxIdx));
                resultado.set(maxIdx, tmp);
            }
        }

        return resultado;
    }

    public int cantidadNodos() { return aristas.size(); }
}
