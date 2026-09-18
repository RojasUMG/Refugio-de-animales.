package com.refugio.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto unico de conexion a MySQL. Cada metodo del DAO pide una conexion
 * nueva con Database.obtenerConexion() y la cierra con try-with-resources.
 */
public final class Database {

    private static final Properties CONFIG = cargarConfiguracion();
    private static final String URL = construirUrl();

    private Database() {
    }

    private static Properties cargarConfiguracion() {
        Properties props = new Properties();
        try (InputStream in = Database.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "No se encontro src/main/resources/db.properties. "
                                + "Copia db.properties.example y coloca tu contrasena de MySQL.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Error leyendo db.properties", e);
        }
        return props;
    }

    private static String construirUrl() {
        String host = CONFIG.getProperty("db.host", "localhost");
        String port = CONFIG.getProperty("db.port", "3306");
        String name = CONFIG.getProperty("db.name", "refugio_de_animales");
        return "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
                + "&useUnicode=true&characterEncoding=UTF-8";
    }

    public static Connection obtenerConexion() throws SQLException {
        String usuario = CONFIG.getProperty("db.user", "root");
        String password = CONFIG.getProperty("db.password", "");
        return DriverManager.getConnection(URL, usuario, password);
    }

    /** Prueba rapida de conectividad, usada al arrancar el servidor. */
    public static void probarConexion() {
        try (Connection conexion = obtenerConexion()) {
            System.out.println("Conexion a MySQL exitosa -> " + conexion.getCatalog());
        } catch (SQLException e) {
            throw new RuntimeException(
                    "No se pudo conectar a MySQL. Revisa db.properties (usuario/contrasena/puerto) "
                            + "y que el servicio de MySQL este corriendo.", e);
        }
    }
}
