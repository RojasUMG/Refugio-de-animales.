package com.refugio.servicio;

import com.refugio.dao.AdopcionDAO;
import com.refugio.dao.SolicitudAdopcionDAO;
import com.refugio.estructuras.BusquedaBinaria;
import com.refugio.estructuras.Ordenamiento;
import com.refugio.modelo.Adopcion;
import com.refugio.modelo.SolicitudAdopcion;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Flujo de solicitudes y adopciones. Aqui viven los dos algoritmos clasicos
 * de busqueda/ordenamiento pedidos por el curso: Merge Sort (Ordenamiento) y
 * Busqueda Binaria (BusquedaBinaria), en vez de depender de Collections.sort.
 */
public class SolicitudService {

    private final SolicitudAdopcionDAO solicitudDAO = new SolicitudAdopcionDAO();
    private final AdopcionDAO adopcionDAO = new AdopcionDAO();

    /** Todas las solicitudes (o solo las de un estado) ordenadas por fecha, mas reciente primero. */
    public List<SolicitudAdopcion> listar(String estado) {
        List<SolicitudAdopcion> solicitudes = (estado == null || estado.isBlank())
                ? solicitudDAO.obtenerTodas()
                : solicitudDAO.obtenerPorEstado(estado);
        return Ordenamiento.mergeSort(
                solicitudes,
                Comparator.comparing((SolicitudAdopcion s) -> s.fechaDeSolicitud).reversed());
    }

    /** Busca una solicitud por id con busqueda binaria (primero ordena por id, requisito del algoritmo). */
    public SolicitudAdopcion buscarPorId(int solicitudId) {
        List<SolicitudAdopcion> todas = solicitudDAO.obtenerTodas();
        List<SolicitudAdopcion> ordenadasPorId = Ordenamiento.mergeSort(
                todas, BusquedaBinaria.comparadorPorClave(s -> s.solicitudId));
        return BusquedaBinaria.buscar(ordenadasPorId, solicitudId, s -> s.solicitudId);
    }

    public int crear(SolicitudAdopcion solicitud) {
        solicitud.estado = "pendiente";
        solicitud.fechaDeSolicitud = LocalDateTime.now();
        return solicitudDAO.crear(solicitud);
    }

    public void revisar(int solicitudId, String nuevoEstado, int revisadoPor, String notas) {
        if (!nuevoEstado.equals("aprobada") && !nuevoEstado.equals("rechazada")) {
            throw new IllegalArgumentException("El estado debe ser 'aprobada' o 'rechazada'");
        }
        solicitudDAO.actualizarEstado(solicitudId, nuevoEstado, revisadoPor, notas);
    }

    /** Confirma la adopcion de una solicitud ya aprobada y marca el animal como adoptado. */
    public int confirmarAdopcion(int solicitudId, String notas) {
        SolicitudAdopcion solicitud = solicitudDAO.obtenerPorId(solicitudId);
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud " + solicitudId + " no existe");
        }
        if (!"aprobada".equals(solicitud.estado)) {
            throw new IllegalStateException("Solo se pueden confirmar solicitudes aprobadas");
        }
        Adopcion adopcion = new Adopcion();
        adopcion.solicitudId = solicitud.solicitudId;
        adopcion.animalId = solicitud.animalId;
        adopcion.adoptanteId = solicitud.solicitanteId;
        adopcion.fechaDeAdopcion = LocalDateTime.now();
        adopcion.notas = notas;
        return adopcionDAO.confirmar(adopcion);
    }

    public List<Adopcion> listarAdopciones() {
        return adopcionDAO.obtenerTodas();
    }
}
