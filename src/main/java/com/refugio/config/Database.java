package com.refugio.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Clase de configuración de la base de datos.
 * Lee las credenciales desde src/main/resources/db.properties.
 */
public class Database {

    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = Database.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            props.load(in);
            url      = props.getProperty("db.url");
            user     = props.getProperty("db.user");
            password = props.getProperty("db.password");
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer db.properties: " + e.getMessage(), e);
        }
    }

    /** Devuelve una nueva conexión a MySQL. */
    public static Connection conectar() throws Exception {
        return DriverManager.getConnection(url, user, password);
    }
}
