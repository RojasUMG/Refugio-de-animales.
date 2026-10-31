package com.refugio.servicio;

import com.refugio.dao.SolicitudDAO;
import com.refugio.estructuras.BusquedaBinaria;
import com.refugio.estructuras.Ordenamiento;
import com.refugio.modelo.Solicitud;

import java.util.Comparator;
import java.util.List;

/**
 * Capa de negocio para solicitudes de adopción.
 *
 * Estructuras de datos utilizadas:
 *  - Ordenamiento (Merge Sort) → ordena solicitudes por fecha descendente
 *  - BusquedaBinaria           → localiza una solicitud por ID en la lista ordenada
 */
public class SolicitudService {

    private final SolicitudDAO dao         = new SolicitudDAO();
    private final Ordenamiento<Solicitud> ord = new Ordenamiento<>();
    private final BusquedaBinaria<Solicitud> bs = new BusquedaBinaria<>();

    // ── Comparadores ──────────────────────────────────────────────────────────

    /** Ordenar por fecha de solicitud descendente (más reciente primero). */
    private static final Comparator<Solicitud> POR_FECHA_DESC =
        (a, b) -> {
            if (a.fechaDeSolicitud == null && b.fechaDeSolicitud == null) return 0;
            if (a.fechaDeSolicitud == null) return 1;
            if (b.fechaDeSolicitud == null) return -1;
            return b.fechaDeSolicitud.compareTo(a.fechaDeSolicitud);
        };

    /** Ordenar por ID ascendente (para búsqueda binaria). */
    private static final Comparator<Solicitud> POR_ID_ASC =
        Comparator.comparingInt(s -> s.solicitudId);

    // ── Lectura ───────────────────────────────────────────────────────────────

    /**
     * Lista todas las solicitudes ordenadas por fecha (Merge Sort).
     * @param estado null para todas, o "pendiente"/"aprobada"/"rechazada"
     */
    public List<Solicitud> listar(String estado) throws Exception {
        List<Solicitud> todas = (estado == null || estado.isBlank())
            ? dao.obtenerTodas()
            : dao.obtenerPorEstado(estado);

        return ord.mergeSort(todas, POR_FECHA_DESC);
    }

    /**
     * Busca una solicitud por ID usando Búsqueda Binaria.
     * Ordena primero por ID con Merge Sort, luego aplica Búsqueda Binaria.
     */
    public Solicitud buscarPorId(int solicitudId) throws Exception {
        List<Solicitud> todas      = dao.obtenerTodas();
        List<Solicitud> ordenadas  = ord.mergeSort(todas, POR_ID_ASC);

        Solicitud objetivo = new Solicitud();
        objetivo.solicitudId = solicitudId;

        return bs.encontrar(ordenadas, objetivo, POR_ID_ASC);
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    /** Crea una nueva solicitud con estado "pendiente". */
    public Solicitud crear(Solicitud s) throws Exception {
        return dao.crear(s);
    }

    /**
     * Cambia el estado de una solicitud a "aprobada" o "rechazada".
     */
    public void revisar(int solicitudId, String nuevoEstado, Integer revisadoPor, String notas) throws Exception {
        if (!nuevoEstado.equals("aprobada") && !nuevoEstado.equals("rechazada")) {
            throw new IllegalArgumentException("Estado debe ser 'aprobada' o 'rechazada'");
        }
        Solicitud s = dao.obtenerPorId(solicitudId);
        if (s == null) throw new IllegalArgumentException("Solicitud no encontrada: " + solicitudId);
        dao.actualizarEstado(solicitudId, nuevoEstado, revisadoPor, notas);
    }
}
