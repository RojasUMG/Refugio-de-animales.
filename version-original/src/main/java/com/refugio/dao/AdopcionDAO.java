package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.Adopcion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AdopcionDAO {

    private static final String SELECT_BASE =
            "SELECT ad.*, an.nombre AS animal_nombre, "
                    + "CONCAT(u.primer_nombre, ' ', u.apellido) AS adoptante_nombre "
                    + "FROM adopciones ad "
                    + "JOIN animales an ON ad.animal_id = an.animal_id "
                    + "JOIN usuarios u ON ad.adoptante_id = u.usuario_id ";

    public List<Adopcion> obtenerTodas() {
        List<Adopcion> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "ORDER BY ad.fecha_de_adopcion DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar adopciones", e);
        }
        return lista;
    }

    /**
     * Registra la adopcion y marca el animal como adoptado en una sola transaccion:
     * si algo falla, ninguno de los dos cambios queda a medias.
     */
    public int confirmar(Adopcion a) {
        String insertAdopcion = "INSERT INTO adopciones (solicitud_id, animal_id, adoptante_id, "
                + "fecha_de_adopcion, notas) VALUES (?,?,?,?,?)";
        String updateAnimal = "UPDATE animales SET estado_de_adopcion = 'Adoptado' WHERE animal_id = ?";

        try (Connection con = Database.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int nuevoId;
                try (PreparedStatement ps = con.prepareStatement(insertAdopcion, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, a.solicitudId);
                    ps.setInt(2, a.animalId);
                    ps.setInt(3, a.adoptanteId);
                    ps.setTimestamp(4, Timestamp.valueOf(a.fechaDeAdopcion));
                    ps.setString(5, a.notas);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        nuevoId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(updateAnimal)) {
                    ps.setInt(1, a.animalId);
                    ps.executeUpdate();
                }
                con.commit();
                return nuevoId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al confirmar la adopcion", e);
        }
    }

    private Adopcion mapear(ResultSet rs) throws SQLException {
        Adopcion a = new Adopcion();
        a.adopcionId = rs.getInt("adopcion_id");
        a.solicitudId = rs.getInt("solicitud_id");
        a.animalId = rs.getInt("animal_id");
        a.adoptanteId = rs.getInt("adoptante_id");
        a.fechaDeAdopcion = rs.getTimestamp("fecha_de_adopcion").toLocalDateTime();
        a.notas = rs.getString("notas");
        a.animalNombre = rs.getString("animal_nombre");
        a.adoptanteNombre = rs.getString("adoptante_nombre");
        return a;
    }
}
