package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.SolicitudAdopcion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SolicitudAdopcionDAO {

    private static final String SELECT_BASE =
            "SELECT s.*, a.nombre AS animal_nombre, "
                    + "CONCAT(u.primer_nombre, ' ', u.apellido) AS solicitante_nombre "
                    + "FROM solicitudes_de_adopcion s "
                    + "JOIN animales a ON s.animal_id = a.animal_id "
                    + "JOIN usuarios u ON s.solicitante_id = u.usuario_id ";

    /** Trae TODAS las solicitudes; el ordenamiento final lo hace SolicitudService con su propio algoritmo. */
    public List<SolicitudAdopcion> obtenerTodas() {
        List<SolicitudAdopcion> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar solicitudes de adopcion", e);
        }
        return lista;
    }

    public List<SolicitudAdopcion> obtenerPorEstado(String estado) {
        String sql = SELECT_BASE + " WHERE s.estado = ?";
        List<SolicitudAdopcion> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar solicitudes por estado", e);
        }
        return lista;
    }

    public SolicitudAdopcion obtenerPorId(int solicitudId) {
        String sql = SELECT_BASE + " WHERE s.solicitud_id = ?";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, solicitudId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la solicitud " + solicitudId, e);
        }
    }

    public int crear(SolicitudAdopcion s) {
        String sql = "INSERT INTO solicitudes_de_adopcion (solicitante_id, animal_id, fecha_de_solicitud, "
                + "estado, motivacion, notas, revisado_por) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.solicitanteId);
            ps.setInt(2, s.animalId);
            ps.setTimestamp(3, Timestamp.valueOf(s.fechaDeSolicitud));
            ps.setString(4, s.estado);
            ps.setString(5, s.motivacion);
            ps.setString(6, s.notas);
            // revisado_por es NOT NULL en el esquema; mientras no se revisa, apunta a quien la crea.
            ps.setInt(7, s.revisadoPor != null ? s.revisadoPor : s.solicitanteId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear la solicitud de adopcion", e);
        }
    }

    public void actualizarEstado(int solicitudId, String nuevoEstado, int revisadoPor, String notas) {
        String sql = "UPDATE solicitudes_de_adopcion SET estado = ?, revisado_por = ?, revisado_el = NOW(), "
                + "notas = ? WHERE solicitud_id = ?";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, revisadoPor);
            ps.setString(3, notas);
            ps.setInt(4, solicitudId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la solicitud " + solicitudId, e);
        }
    }

    private SolicitudAdopcion mapear(ResultSet rs) throws SQLException {
        SolicitudAdopcion s = new SolicitudAdopcion();
        s.solicitudId = rs.getInt("solicitud_id");
        s.solicitanteId = rs.getInt("solicitante_id");
        s.animalId = rs.getInt("animal_id");
        s.fechaDeSolicitud = rs.getTimestamp("fecha_de_solicitud").toLocalDateTime();
        s.estado = rs.getString("estado");
        s.motivacion = rs.getString("motivacion");
        s.notas = rs.getString("notas");
        s.revisadoPor = (Integer) rs.getObject("revisado_por");
        Timestamp revisadoEl = rs.getTimestamp("revisado_el");
        s.revisadoEl = revisadoEl != null ? revisadoEl.toLocalDateTime() : null;
        s.animalNombre = rs.getString("animal_nombre");
        s.solicitanteNombre = rs.getString("solicitante_nombre");
        return s;
    }
}
