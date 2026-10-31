package com.refugio.estructuras;

import com.refugio.modelo.Animal;

import java.util.ArrayList;
import java.util.List;

/**
 * Cola de Prioridad implementada con un max-heap binario.
 *
 * Se usa en VeterinariaService para clasificar animales según urgencia:
 *   Prioridad 3 (URGENTE)   — palabras clave: urgente, crítico, emergencia, grave
 *   Prioridad 2 (MODERADO)  — palabras clave: enfermo, tratamiento, observación
 *   Prioridad 1 (NORMAL)    — todos los demás
 *
 * El animal con mayor prioridad siempre está en la cima del heap.
 */
public class ColaPrioridadVeterinaria {

    /** Elemento de la cola: animal + su nivel de prioridad calculado. */
    public static class ElementoCola {
        public final Animal animal;
        public final int    prioridad;   // 1=NORMAL, 2=MODERADO, 3=URGENTE
        public final String nivelTexto;

        public ElementoCola(Animal animal) {
            this.animal     = animal;
            this.prioridad  = calcularPrioridad(animal.estadoDeSalud);
            this.nivelTexto = switch (prioridad) {
                case 3  -> "URGENTE";
                case 2  -> "MODERADO";
                default -> "NORMAL";
            };
        }

        private static int calcularPrioridad(String estadoDeSalud) {
            if (estadoDeSalud == null) return 1;
            String s = estadoDeSalud.toLowerCase();
            if (s.contains("urgente") || s.contains("crítico") ||
                s.contains("critico")  || s.contains("emergencia") ||
                s.contains("grave"))    return 3;
            if (s.contains("enfermo") || s.contains("tratamiento") ||
                s.contains("observaci")) return 2;
            return 1;
        }
    }

    // ── Max-heap con ArrayList ────────────────────────────────────────────────

    private final List<ElementoCola> heap = new ArrayList<>();

    /** Inserta un animal en la cola. O(log n). */
    public void insertar(Animal animal) {
        heap.add(new ElementoCola(animal));
        subirUltimo();
    }

    /** Extrae el animal con mayor prioridad. O(log n). */
    public ElementoCola extraerMaximo() {
        if (heap.isEmpty()) return null;
        ElementoCola maximo = heap.get(0);
        ElementoCola ultimo = heap.remove(heap.size() - 1);
        if (!heap.isEmpty()) {
            heap.set(0, ultimo);
            bajarRaiz(0);
        }
        return maximo;
    }

    /** Devuelve (sin extraer) todos los elementos ordenados por prioridad. */
    public List<ElementoCola> obtenerOrdenada() {
        // Copiamos el heap y extraemos para obtener orden correcto
        ColaPrioridadVeterinaria copia = new ColaPrioridadVeterinaria();
        for (ElementoCola e : heap) copia.insertar(e.animal);

        List<ElementoCola> resultado = new ArrayList<>(heap.size());
        ElementoCola e;
        while ((e = copia.extraerMaximo()) != null) resultado.add(e);
        return resultado;
    }

    public int tamanio() { return heap.size(); }
    public boolean estaVacia() { return heap.isEmpty(); }

    // ── Operaciones del heap ──────────────────────────────────────────────────

    private void subirUltimo() {
        int i = heap.size() - 1;
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (heap.get(i).prioridad > heap.get(padre).prioridad) {
                intercambiar(i, padre);
                i = padre;
            } else break;
        }
    }

    private void bajarRaiz(int i) {
        int n = heap.size();
        while (true) {
            int mayor    = i;
            int izq      = 2 * i + 1;
            int der      = 2 * i + 2;

            if (izq < n && heap.get(izq).prioridad > heap.get(mayor).prioridad) mayor = izq;
            if (der < n && heap.get(der).prioridad > heap.get(mayor).prioridad) mayor = der;

            if (mayor == i) break;
            intercambiar(i, mayor);
            i = mayor;
        }
    }

    private void intercambiar(int a, int b) {
        ElementoCola tmp = heap.get(a);
        heap.set(a, heap.get(b));
        heap.set(b, tmp);
    }
}
