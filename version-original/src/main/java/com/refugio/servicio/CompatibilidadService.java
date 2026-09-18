package com.refugio.servicio;

import com.refugio.dao.AnimalDAO;
import com.refugio.dao.PreferenciaAdoptanteDAO;
import com.refugio.estructuras.AristaCompatibilidad;
import com.refugio.estructuras.GrafoCompatibilidad;

import java.util.List;

/**
 * Construye el grafo adoptante-animal (ver GrafoCompatibilidad) y expone la
 * lista de animales sugeridos para un adoptante segun sus preferencias
 * guardadas en preferencias_del_adoptante.
 */
public class CompatibilidadService {

    private final PreferenciaAdoptanteDAO preferenciaDAO = new PreferenciaAdoptanteDAO();
    private final AnimalDAO animalDAO = new AnimalDAO();

    public List<AristaCompatibilidad> sugerirAnimales(int usuarioId, int maximo) {
        AnimalDAO.Filtros soloDisponibles = new AnimalDAO.Filtros();
        soloDisponibles.estadoDeAdopcion = "Disponible";

        GrafoCompatibilidad grafo = GrafoCompatibilidad.construir(
                preferenciaDAO.obtenerTodas(),
                animalDAO.buscarConFiltros(soloDisponibles));

        return grafo.sugerirParaAdoptante(usuarioId, maximo);
    }
}
