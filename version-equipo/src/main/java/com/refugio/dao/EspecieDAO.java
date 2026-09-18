package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.Especie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EspecieDAO {

    public List<Especie> obtenerTodas() {
        String sql = "SELECT especies_id, nombre, descripcion FROM especies ORDER BY nombre";
        List<Especie> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar especies", e);
        }
        return lista;
    }

    private Especie mapear(ResultSet rs) throws SQLException {
        Especie especie = new Especie();
        especie.especiesId = rs.getInt("especies_id");
        especie.nombre = rs.getString("nombre");
        especie.descripcion = rs.getString("descripcion");
        return especie;
    }
}
