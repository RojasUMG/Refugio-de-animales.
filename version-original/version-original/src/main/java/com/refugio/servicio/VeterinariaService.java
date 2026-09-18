package com.refugio.servicio;

import com.refugio.dao.AnimalDAO;
import com.refugio.estructuras.ColaPrioridadVeterinaria;
import com.refugio.estructuras.NivelUrgencia;
import com.refugio.estructuras.SolicitudAtencion;
import com.refugio.modelo.Animal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Arma la cola de prioridad de atencion veterinaria a partir del campo
 * estado_de_salud de cada animal. No existe una columna "urgencia" en el
 * esquema, asi que se clasifica por palabras clave: es una heuristica de
 * demostracion que el equipo puede reemplazar despues por un campo explicito
 * en registros_medicos si lo agregan.
 */
public class VeterinariaService {

    private final AnimalDAO animalDAO = new AnimalDAO();

    private static final List<String> PALABRAS_URGENTE =
            List.of("urgente", "critico", "crítico", "emergencia", "grave");
    private static final List<String> PALABRAS_MODERADO =
            List.of("enfermo", "tratamiento", "observacion", "observación", "recuperacion", "recuperación", "lesion", "lesión");
    private static final List<String> PALABRAS_SIN_PENDIENTE =
            List.of("sano", "saludable", "ninguno", "ninguna", "n/a");

    public List<SolicitudAtencion> obtenerColaDeAtencion() {
        ColaPrioridadVeterinaria cola = new ColaPrioridadVeterinaria();
        for (Animal animal : animalDAO.obtenerTodos()) {
            if (noNecesitaAtencion(animal.estadoDeSalud)) {
                continue;
            }
            NivelUrgencia urgencia = clasificarUrgencia(animal.estadoDeSalud);
            LocalDateTime referencia = animal.creadoEl != null ? animal.creadoEl : LocalDateTime.now();
            cola.agregar(new SolicitudAtencion(animal, urgencia, animal.estadoDeSalud, referencia));
        }
        return cola.ordenDeAtencion();
    }

    private boolean noNecesitaAtencion(String estadoDeSalud) {
        if (estadoDeSalud == null || estadoDeSalud.isBlank()) {
            return true;
        }
        String texto = estadoDeSalud.toLowerCase(Locale.ROOT);
        return PALABRAS_SIN_PENDIENTE.stream().anyMatch(texto::contains);
    }

    public NivelUrgencia clasificarUrgencia(String estadoDeSalud) {
        if (estadoDeSalud == null) {
            return NivelUrgencia.NORMAL;
        }
        String texto = estadoDeSalud.toLowerCase(Locale.ROOT);
        if (PALABRAS_URGENTE.stream().anyMatch(texto::contains)) {
            return NivelUrgencia.URGENTE;
        }
        if (PALABRAS_MODERADO.stream().anyMatch(texto::contains)) {
            return NivelUrgencia.MODERADO;
        }
        return NivelUrgencia.NORMAL;
    }
}
