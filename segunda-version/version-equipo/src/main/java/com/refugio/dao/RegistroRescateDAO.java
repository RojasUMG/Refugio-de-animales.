package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.RegistroRescate;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RegistroRescateDAO {

    public List<RegistroRescate> obtenerPorAnimal(int animalId) {
        String sql = "SELECT * FROM registros_de_rescate WHERE animal_id = ? ORDER BY fecha_de_rescate DESC";
        List<RegistroRescate> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, animalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar registros de rescate", e);
        }
        return lista;
    }

    public int crear(RegistroRescate r) {
        String sql = "INSERT INTO registros_de_rescate (animal_id, fecha_de_rescate, ubicacion, descripcion, "
                + "registrado_por) VALUES (?,?,?,?,?)";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.animalId);
            ps.setDate(2, Date.valueOf(r.fechaDeRescate));
            ps.setString(3, r.ubicacion);
            ps.setString(4, r.descripcion);
            ps.setInt(5, r.registradoPor);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear registro de rescate", e);
        }
    }

    private RegistroRescate mapear(ResultSet rs) throws SQLException {
        RegistroRescate r = new RegistroRescate();
        r.registroId = rs.getInt("registro_id");
        r.animalId = rs.getInt("animal_id");
        r.fechaDeRescate = rs.getDate("fecha_de_rescate").toLocalDate();
        r.ubicacion = rs.getString("ubicacion");
        r.descripcion = rs.getString("descripcion");
        r.registradoPor = rs.getInt("registrado_por");
        return r;
    }
}
