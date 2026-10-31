package com.refugio.dao;

import com.refugio.config.Database;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * DAO para preferencias_del_adoptante.
 * Devuelve un mapa de preferencia_clave → preferencia_valor para un usuario dado.
 */
public class PreferenciaAdoptanteDAO {

    public Map<String, String> obtenerPorUsuario(int usuarioId) throws Exception {
        Map<String, String> prefs = new HashMap<>();
        String sql = "SELECT preferencia_clave, preferencia_valor " +
                     "FROM preferencias_del_adoptante WHERE usuario_id = ?";
        try (Connection con = Database.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prefs.put(rs.getString("preferencia_clave"),
                              rs.getString("preferencia_valor"));
                }
            }
        }
        return prefs;
    }

    public void guardar(int usuarioId, String clave, String valor) throws Exception {
        String sql = "INSERT INTO preferencias_del_adoptante " +
            "(usuario_id, preferencia_clave, preferencia_valor) VALUES (?,?,?) " +
            "ON DUPLICATE KEY UPDATE preferencia_valor = VALUES(preferencia_valor)";
        try (Connection con = Database.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, clave);
            ps.setString(3, valor);
            ps.executeUpdate();
        }
    }
}
