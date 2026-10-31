package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.Animal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la tabla `animales`.
 * Usa JDBC directo con PreparedStatement — sin ORM.
 */
public class AnimalDAO {

    private static final String SELECT_BASE =
        "SELECT a.animal_id, a.nombre, a.especie_id, e.nombre_especie, " +
        "       a.raza, a.fecha_de_nacimiento, a.sexo, a.color, a.tamano, " +
        "       a.descripcion, a.estado_de_salud, a.estado_de_adopcion, " +
        "       a.url_de_imagen, a.fecha_de_ingreso " +
        "FROM animales a " +
        "LEFT JOIN especies e ON a.especie_id = e.especie_id ";

    // ── Lectura ──────────────────────────────────────────────────────────────

    public List<Animal> obtenerTodos() throws Exception {
        List<Animal> lista = new ArrayList<>();
        try (Connection con = Database.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE + "ORDER BY a.nombre")) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Animal> obtenerPorEstado(String estado) throws Exception {
        List<Animal> lista = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE a.estado_de_adopcion = ? ORDER BY a.nombre";
        try (Connection con = Database.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Animal obtenerPorId(int id) throws Exception {
        String sql = SELECT_BASE + "WHERE a.animal_id = ?";
        try (Connection con = Database.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    public Animal crear(Animal a) throws Exception {
        String sql = "INSERT INTO animales " +
            "(nombre, especie_id, raza, fecha_de_nacimiento, sexo, color, tamano, " +
            " descripcion, estado_de_salud, estado_de_adopcion, url_de_imagen, fecha_de_ingreso) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = Database.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.nombre);
            ps.setInt(2, a.especieId);
            ps.setString(3, a.raza);
            ps.setObject(4, a.fechaDeNacimiento);
            ps.setString(5, a.sexo);
            ps.setString(6, a.color);
            ps.setString(7, a.tamano);
            ps.setString(8, a.descripcion);
            ps.setString(9, a.estadoDeSalud);
            ps.setString(10, a.estadoDeAdopcion != null ? a.estadoDeAdopcion : "Disponible");
            ps.setString(11, a.urlDeImagen);
            ps.setObject(12, a.fechaDeIngreso != null ? a.fechaDeIngreso : java.time.LocalDate.now());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) a.animalId = keys.getInt(1);
            }
        }
        return obtenerPorId(a.animalId);
    }

    public void actualizarEstado(int id, String nuevoEstado) throws Exception {
        String sql = "UPDATE animales SET estado_de_adopcion = ? WHERE animal_id = ?";
        try (Connection con = Database.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ── Mapeo de ResultSet → Animal ───────────────────────────────────────────

    private Animal mapear(ResultSet rs) throws SQLException {
        Animal a = new Animal();
        a.animalId         = rs.getInt("animal_id");
        a.nombre           = rs.getString("nombre");
        a.especieId        = rs.getInt("especie_id");
        a.especieNombre    = rs.getString("nombre_especie");
        a.raza             = rs.getString("raza");
        a.sexo             = rs.getString("sexo");
        a.color            = rs.getString("color");
        a.tamano           = rs.getString("tamano");
        a.descripcion      = rs.getString("descripcion");
        a.estadoDeSalud    = rs.getString("estado_de_salud");
        a.estadoDeAdopcion = rs.getString("estado_de_adopcion");
        a.urlDeImagen      = rs.getString("url_de_imagen");

        Date fnac = rs.getDate("fecha_de_nacimiento");
        if (fnac != null) a.fechaDeNacimiento = fnac.toLocalDate();
        Date fing = rs.getDate("fecha_de_ingreso");
        if (fing != null) a.fechaDeIngreso = fing.toLocalDate();

        return a;
    }
}
