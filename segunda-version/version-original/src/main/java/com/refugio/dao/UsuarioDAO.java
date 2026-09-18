package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * OJO: la tabla real usa `contraseña` y `teléfono` (con tilde/ñ) y `STATUS` en vez
 * de "estado" -- son los nombres exactos que tiene la base de datos ya creada, aunque
 * difieren de la convencion "sin tildes" del resto del esquema. Se usan comillas
 * invertidas (backticks) para que MySQL los reconozca sin ambiguedad.
 */
public class UsuarioDAO {

    private static final String SELECT_BASE =
            "SELECT usuario_id, primer_nombre, apellido, email, `teléfono`, rol, STATUS, creado_en "
                    + "FROM usuarios ";

    public List<Usuario> obtenerTodos() {
        String sql = SELECT_BASE + "ORDER BY apellido, primer_nombre";
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios", e);
        }
        return lista;
    }

    public Usuario obtenerPorId(int usuarioId) {
        String sql = SELECT_BASE + "WHERE usuario_id = ?";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el usuario " + usuarioId, e);
        }
    }

    public int crear(Usuario u) {
        String sql = "INSERT INTO usuarios (primer_nombre, apellido, email, `contraseña`, `teléfono`, rol) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.primerNombre);
            ps.setString(2, u.apellido);
            ps.setString(3, u.email);
            ps.setString(4, u.contrasena);
            ps.setInt(5, u.telefono);
            ps.setString(6, u.rol);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear el usuario", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.usuarioId = rs.getInt("usuario_id");
        u.primerNombre = rs.getString("primer_nombre");
        u.apellido = rs.getString("apellido");
        u.email = rs.getString("email");
        u.telefono = (Integer) rs.getObject("teléfono");
        u.rol = rs.getString("rol");
        u.estado = rs.getBoolean("STATUS");
        Timestamp creado = rs.getTimestamp("creado_en");
        u.creadoEn = creado != null ? creado.toLocalDateTime() : null;
        return u;
    }
}
