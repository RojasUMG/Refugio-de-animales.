package com.refugio.estructuras;

import com.refugio.modelo.Animal;
import com.refugio.modelo.PreferenciaAdoptante;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grafo bipartito no dirigido y ponderado: un lado son los adoptantes (con
 * preferencias registradas), el otro los animales disponibles. El peso de
 * cada arista es un puntaje de compatibilidad 0-100 calculado a partir de
 * preferencias_del_adoptante. Se representa con lista de adyacencia
 * (Map<usuarioId, List<AristaCompatibilidad>>), la forma clasica de guardar
 * un grafo disperso.
 *
 * "Sugerir animales para un adoptante" es recorrer su lista de adyacencia y
 * ordenarla por peso descendente: los animales mas compatibles primero.
 */
public class GrafoCompatibilidad {

    private final Map<Integer, List<AristaCompatibilidad>> listaDeAdyacencia = new HashMap<>();

    public static GrafoCompatibilidad construir(List<PreferenciaAdoptante> preferencias, List<Animal> animalesDisponibles) {
        GrafoCompatibilidad grafo = new GrafoCompatibilidad();
        for (PreferenciaAdoptante pref : preferencias) {
            List<AristaCompatibilidad> aristas = new ArrayList<>();
            for (Animal animal : animalesDisponibles) {
                int peso = calcularPeso(pref, animal);
                if (peso > 0) {
                    aristas.add(new AristaCompatibilidad(animal, peso));
                }
            }
            List<AristaCompatibilidad> ordenadas = Ordenamiento.mergeSort(
                    aristas, (a, b) -> b.peso - a.peso); // descendente: mejor puntaje primero
            grafo.listaDeAdyacencia.put(pref.usuarioId, ordenadas);
        }
        return grafo;
    }

    /** Las mejores `maximo` sugerencias de animales para un adoptante, ya ordenadas por compatibilidad. */
    public List<AristaCompatibilidad> sugerirParaAdoptante(int usuarioId, int maximo) {
        List<AristaCompatibilidad> aristas = listaDeAdyacencia.getOrDefault(usuarioId, new ArrayList<>());
        return aristas.subList(0, Math.min(maximo, aristas.size()));
    }

    private static int calcularPeso(PreferenciaAdoptante pref, Animal animal) {
        if (animal.especiesId != pref.especiePreferida) {
            return 0; // la especie es un filtro obligatorio: sin coincidencia no hay arista
        }
        int peso = 40;

        if (pref.tamanoPreferido == null || pref.tamanoPreferido.equalsIgnoreCase(animal.tamano)) {
            peso += 20;
        }
        if (pref.sexoPreferido == null || pref.sexoPreferido.equalsIgnoreCase(animal.sexo)) {
            peso += 10;
        }

        Integer edad = edadEnAnios(animal.fechaDeNacimiento);
        int edadMinima = pref.edadMinima != null ? pref.edadMinima : 0;
        int edadMaxima = pref.edadMaxima != null ? pref.edadMaxima : Integer.MAX_VALUE;
        if (edad == null || (edad >= edadMinima && edad <= edadMaxima)) {
            peso += 20;
        }

        // Heuristica sencilla: animales grandes piden vivienda con espacio y dueno activo.
        if ("Grande".equalsIgnoreCase(animal.tamano)) {
            if ("Casa".equalsIgnoreCase(pref.tipoDeVivienda)) {
                peso += 5;
            }
            if ("Alto".equalsIgnoreCase(pref.nivelDeActividad)) {
                peso += 5;
            }
        }

        return Math.min(peso, 100);
    }

    private static Integer edadEnAnios(LocalDate fechaDeNacimiento) {
        if (fechaDeNacimiento == null) {
            return null;
        }
        return Period.between(fechaDeNacimiento, LocalDate.now()).getYears();
    }
}
