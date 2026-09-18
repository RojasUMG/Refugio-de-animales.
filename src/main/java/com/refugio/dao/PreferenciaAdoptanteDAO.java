package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.PreferenciaAdoptante;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PreferenciaAdoptanteDAO {

    public List<PreferenciaAdoptante> obtenerTodas() {
        String sql = "SELECT * FROM preferencias_del_adoptante";
        List<PreferenciaAdoptante> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar preferencias", e);
        }
        return lista;
    }

    public PreferenciaAdoptante obtenerPorUsuario(int usuarioId) {
        String sql = "SELECT * FROM preferencias_del_adoptante WHERE usuario_id = ?";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar preferencias del usuario " + usuarioId, e);
        }
    }

    public int guardar(PreferenciaAdoptante p) {
        String sql = "INSERT INTO preferencias_del_adoptante (usuario_id, especie_preferida, tamano_preferido, "
                + "sexo_preferido, edad_minima, edad_maxima, tiene_ninos, otras_mascotas, nivel_de_actividad, "
                + "tipo_de_vivienda) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.usuarioId);
            ps.setInt(2, p.especiePreferida);
            ps.setString(3, p.tamanoPreferido);
            ps.setString(4, p.sexoPreferido);
            ps.setObject(5, p.edadMinima);
            ps.setObject(6, p.edadMaxima);
            ps.setObject(7, p.tieneNinos);
            ps.setObject(8, p.otrasMascotas);
            ps.setString(9, p.nivelDeActividad);
            ps.setString(10, p.tipoDeVivienda);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar preferencias", e);
        }
    }

    private PreferenciaAdoptante mapear(ResultSet rs) throws SQLException {
        PreferenciaAdoptante p = new PreferenciaAdoptante();
        p.preferenciaId = rs.getInt("preferencia_id");
        p.usuarioId = rs.getInt("usuario_id");
        p.especiePreferida = rs.getInt("especie_preferida");
        p.tamanoPreferido = rs.getString("tamano_preferido");
        p.sexoPreferido = rs.getString("sexo_preferido");
        p.edadMinima = (Integer) rs.getObject("edad_minima");
        p.edadMaxima = (Integer) rs.getObject("edad_maxima");
        p.tieneNinos = (Boolean) rs.getObject("tiene_ninos");
        p.otrasMascotas = (Boolean) rs.getObject("otras_mascotas");
        p.nivelDeActividad = rs.getString("nivel_de_actividad");
        p.tipoDeVivienda = rs.getString("tipo_de_vivienda");
        return p;
    }
}
