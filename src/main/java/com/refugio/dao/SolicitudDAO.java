package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.Solicitud;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para solicitudes_de_adopcion.
 */
public class SolicitudDAO {

    private static final String SELECT_BASE =
        "SELECT s.solicitud_id, s.solicitante_id, s.animal_id, " +
        "       s.fecha_de_solicitud, s.estado, s.motivacion, s.notas, " +
        "       s.revisado_por, s.revisado_el, " +
        "       a.nombre AS animal_nombre, " +
        "       CONCAT(u.primer_nombre, ' ', u.apellido) AS solicitante_nombre " +
        "FROM solicitudes_de_adopcion s " +
        "LEFT JOIN animales  a ON s.animal_id      = a.animal_id " +
        "LEFT JOIN usuarios  u ON s.solicitante_id = u.usuario_id ";

    public List<Solicitud> obtenerTodas() throws Exception {
        List<Solicitud> lista = new ArrayList<>();
        try (Connection con = Database.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + "ORDER BY s.fecha_de_solicitud DESC")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Solicitud> obtenerPorEstado(String estado) throws Exception {
        List<Solicitud> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE s.estado = ? ORDER BY s.fecha_de_solicitud DESC";
        try (Connection con = Database.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Solicitud obtenerPorId(int id) throws Exception {
        String sql = SELECT_BASE + "WHERE s.solicitud_id = ?";
        try (Connection con = Database.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public Solicitud crear(Solicitud s) throws Exception {
        String sql = "INSERT INTO solicitudes_de_adopcion " +
            "(solicitante_id, animal_id, fecha_de_solicitud, estado, motivacion) " +
            "VALUES (?, ?, NOW(), 'pendiente', ?)";
        try (Connection con = Database.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.solicitanteId);
            ps.setInt(2, s.animalId);
            ps.setString(3, s.motivacion);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.solicitudId = keys.getInt(1);
            }
        }
        return obtenerPorId(s.solicitudId);
    }

    public void actualizarEstado(int id, String estado, Integer revisadoPor, String notas) throws Exception {
        String sql = "UPDATE solicitudes_de_adopcion " +
            "SET estado = ?, revisado_por = ?, revisado_el = NOW(), notas = ? " +
            "WHERE solicitud_id = ?";
        try (Connection con = Database.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            if (revisadoPor != null) ps.setInt(2, revisadoPor);
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, notas);
            ps.setInt(4, id);
            ps.executeUpdate();
        }
    }

    private Solicitud mapear(ResultSet rs) throws SQLException {
        Solicitud s = new Solicitud();
        s.solicitudId      = rs.getInt("solicitud_id");
        s.solicitanteId    = rs.getInt("solicitante_id");
        s.animalId         = rs.getInt("animal_id");
        s.estado           = rs.getString("estado");
        s.motivacion       = rs.getString("motivacion");
        s.notas            = rs.getString("notas");
        s.animalNombre     = rs.getString("animal_nombre");
        s.solicitanteNombre = rs.getString("solicitante_nombre");

        Timestamp ts = rs.getTimestamp("fecha_de_solicitud");
        if (ts != null) s.fechaDeSolicitud = ts.toLocalDateTime();
        Timestamp tr = rs.getTimestamp("revisado_el");
        if (tr != null) s.revisadoEl = tr.toLocalDateTime();

        int rev = rs.getInt("revisado_por");
        if (!rs.wasNull()) s.revisadoPor = rev;

        return s;
    }
}
