package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.Vacuna;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VacunaDAO {

    public List<Vacuna> obtenerPorAnimal(int animalId) {
        String sql = "SELECT * FROM vacunas WHERE animal_id = ? ORDER BY fecha_de_aplicacion DESC";
        List<Vacuna> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, animalId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar vacunas", e);
        }
        return lista;
    }

    public int crear(Vacuna v) {
        String sql = "INSERT INTO vacunas (animal_id, nombre_de_vacuna, fecha_de_aplicacion, siguiente_fecha, "
                + "veterinario_id, notas) VALUES (?,?,?,?,?,?)";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.animalId);
            ps.setString(2, v.nombreDeVacuna);
            ps.setDate(3, Date.valueOf(v.fechaDeAplicacion));
            ps.setDate(4, v.siguienteFecha != null ? Date.valueOf(v.siguienteFecha) : null);
            ps.setInt(5, v.veterinarioId);
            ps.setString(6, v.notas);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear vacuna", e);
        }
    }

    private Vacuna mapear(ResultSet rs) throws SQLException {
        Vacuna v = new Vacuna();
        v.vacunasId = rs.getInt("vacunas_id");
        v.animalId = rs.getInt("animal_id");
        v.nombreDeVacuna = rs.getString("nombre_de_vacuna");
        v.fechaDeAplicacion = rs.getDate("fecha_de_aplicacion").toLocalDate();
        Date siguiente = rs.getDate("siguiente_fecha");
        v.siguienteFecha = siguiente != null ? siguiente.toLocalDate() : null;
        v.veterinarioId = rs.getInt("veterinario_id");
        v.notas = rs.getString("notas");
        return v;
    }
}
