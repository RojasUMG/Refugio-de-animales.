package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.RegistroMedico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RegistroMedicoDAO {

    public List<RegistroMedico> obtenerPorAnimal(int animalId) {
        String sql = "SELECT * FROM registros_medicos WHERE animal_id = ? ORDER BY fecha_de_visita DESC";
        List<RegistroMedico> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, animalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar registros medicos", e);
        }
        return lista;
    }

    /** Todos los registros medicos recientes, usados por la cola de prioridad veterinaria. */
    public List<RegistroMedico> obtenerRecientes(int limite) {
        String sql = "SELECT * FROM registros_medicos ORDER BY fecha_de_visita DESC LIMIT ?";
        List<RegistroMedico> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar registros medicos recientes", e);
        }
        return lista;
    }

    public int crear(RegistroMedico r) {
        String sql = "INSERT INTO registros_medicos (animal_id, veterinario_id, fecha_de_visita, razon, "
                + "diagnostico, tratamiento, observaciones) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.animalId);
            ps.setInt(2, r.veterinarioId);
            ps.setTimestamp(3, Timestamp.valueOf(r.fechaDeVisita));
            ps.setString(4, r.razon);
            ps.setString(5, r.diagnostico);
            ps.setString(6, r.tratamiento);
            ps.setString(7, r.observaciones);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear registro medico", e);
        }
    }

    private RegistroMedico mapear(ResultSet rs) throws SQLException {
        RegistroMedico r = new RegistroMedico();
        r.registroMedicoId = rs.getInt("registro_medico_id");
        r.animalId = rs.getInt("animal_id");
        r.veterinarioId = rs.getInt("veterinario_id");
        r.fechaDeVisita = rs.getTimestamp("fecha_de_visita").toLocalDateTime();
        r.razon = rs.getString("razon");
        r.diagnostico = rs.getString("diagnostico");
        r.tratamiento = rs.getString("tratamiento");
        r.observaciones = rs.getString("observaciones");
        return r;
    }
}
