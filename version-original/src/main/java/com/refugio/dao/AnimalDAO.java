package com.refugio.dao;

import com.refugio.config.Database;
import com.refugio.modelo.Animal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AnimalDAO {

    private static final String SELECT_BASE =
            "SELECT a.animal_id, a.numero_de_identificacion, a.nombre, a.especies_id, "
                    + "a.raza, a.sexo, a.fecha_de_nacimiento, a.fecha_de_llegada, a.tamano, "
                    + "a.color, a.descripcion, a.estado_de_salud, a.estado_de_adopcion, "
                    + "a.url_de_imagen, a.creado_el, e.nombre AS especie_nombre "
                    + "FROM animales a JOIN especies e ON a.especies_id = e.especies_id ";

    /** Filtros opcionales para la vista publica tipo Petfinder (cualquiera puede ser null). */
    public static class Filtros {
        public Integer especiesId;
        public String tamano;
        public String sexo;
        public String estadoDeAdopcion;
    }

    public List<Animal> buscarConFiltros(Filtros filtros) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1 ");
        List<Object> parametros = new ArrayList<>();

        if (filtros.especiesId != null) {
            sql.append(" AND a.especies_id = ? ");
            parametros.add(filtros.especiesId);
        }
        if (filtros.tamano != null && !filtros.tamano.isBlank()) {
            sql.append(" AND a.tamano = ? ");
            parametros.add(filtros.tamano);
        }
        if (filtros.sexo != null && !filtros.sexo.isBlank()) {
            sql.append(" AND a.sexo = ? ");
            parametros.add(filtros.sexo);
        }
        if (filtros.estadoDeAdopcion != null && !filtros.estadoDeAdopcion.isBlank()) {
            sql.append(" AND a.estado_de_adopcion = ? ");
            parametros.add(filtros.estadoDeAdopcion);
        }
        sql.append(" ORDER BY a.creado_el DESC");

        List<Animal> lista = new ArrayList<>();
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar animales", e);
        }
        return lista;
    }

    public List<Animal> obtenerTodos() {
        return buscarConFiltros(new Filtros());
    }

    public Animal obtenerPorId(int animalId) {
        String sql = SELECT_BASE + " WHERE a.animal_id = ?";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, animalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el animal " + animalId, e);
        }
    }

    public int crear(Animal a) {
        String sql = "INSERT INTO animales (numero_de_identificacion, nombre, especies_id, raza, sexo, "
                + "fecha_de_nacimiento, fecha_de_llegada, tamano, color, descripcion, estado_de_salud, "
                + "estado_de_adopcion, url_de_imagen) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.numeroDeIdentificacion);
            ps.setString(2, a.nombre);
            ps.setInt(3, a.especiesId);
            ps.setString(4, a.raza);
            ps.setString(5, a.sexo);
            ps.setDate(6, a.fechaDeNacimiento != null ? Date.valueOf(a.fechaDeNacimiento) : null);
            ps.setDate(7, Date.valueOf(a.fechaDeLlegada));
            ps.setString(8, a.tamano);
            ps.setString(9, a.color);
            ps.setString(10, a.descripcion);
            ps.setString(11, a.estadoDeSalud);
            ps.setString(12, a.estadoDeAdopcion);
            ps.setString(13, a.urlDeImagen);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar el animal", e);
        }
    }

    public void actualizarEstadoDeAdopcion(int animalId, String nuevoEstado) {
        String sql = "UPDATE animales SET estado_de_adopcion = ? WHERE animal_id = ?";
        try (Connection con = Database.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, animalId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado de adopcion", e);
        }
    }

    private Animal mapear(ResultSet rs) throws SQLException {
        Animal a = new Animal();
        a.animalId = rs.getInt("animal_id");
        a.numeroDeIdentificacion = rs.getString("numero_de_identificacion");
        a.nombre = rs.getString("nombre");
        a.especiesId = rs.getInt("especies_id");
        a.raza = rs.getString("raza");
        a.sexo = rs.getString("sexo");
        Date nacimiento = rs.getDate("fecha_de_nacimiento");
        a.fechaDeNacimiento = nacimiento != null ? nacimiento.toLocalDate() : null;
        a.fechaDeLlegada = rs.getDate("fecha_de_llegada").toLocalDate();
        a.tamano = rs.getString("tamano");
        a.color = rs.getString("color");
        a.descripcion = rs.getString("descripcion");
        a.estadoDeSalud = rs.getString("estado_de_salud");
        a.estadoDeAdopcion = rs.getString("estado_de_adopcion");
        a.urlDeImagen = rs.getString("url_de_imagen");
        Timestamp creado = rs.getTimestamp("creado_el");
        a.creadoEl = creado != null ? creado.toLocalDateTime() : null;
        a.especieNombre = rs.getString("especie_nombre");
        return a;
    }
}
