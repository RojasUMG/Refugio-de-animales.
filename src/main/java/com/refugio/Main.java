package com.refugio;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.refugio.dao.AnimalDAO;
import com.refugio.dao.SolicitudDAO;
import com.refugio.estructuras.ColaPrioridadVeterinaria;
import com.refugio.estructuras.GrafoCompatibilidad;
import com.refugio.modelo.Animal;
import com.refugio.modelo.Solicitud;
import com.refugio.modelo.Usuario;
import com.refugio.dao.PreferenciaAdoptanteDAO;
import com.refugio.config.Database;
import com.refugio.servicio.AnimalService;
import com.refugio.servicio.CompatibilidadService;
import com.refugio.servicio.SolicitudService;
import com.refugio.servicio.VeterinariaService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.json.JavalinJackson;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * Punto de entrada del sistema Huellitas.
 *
 * Levanta un servidor Javalin en el puerto 7070 que:
 *  - Sirve el frontend estático desde /frontend
 *  - Expone una API REST bajo /api
 *
 * Rutas disponibles:
 *  GET    /api/animales
 *  GET    /api/animales/{id}
 *  POST   /api/animales
 *  PATCH  /api/animales/{id}/estado
 *
 *  GET    /api/solicitudes
 *  GET    /api/solicitudes/{id}
 *  POST   /api/solicitudes
 *  PATCH  /api/solicitudes/{id}
 *
 *  GET    /api/veterinaria/cola
 *
 *  GET    /api/compatibilidad/{usuarioId}
 *
 *  GET    /api/usuarios
 *  POST   /api/usuarios
 *  POST   /api/usuarios/login
 */
public class Main {

    private static final AnimalService       animalService       = new AnimalService();
    private static final SolicitudService    solicitudService    = new SolicitudService();
    private static final VeterinariaService  veterinariaService  = new VeterinariaService();
    private static final CompatibilidadService compatService     = new CompatibilidadService();

    public static void main(String[] args) {

        // ── Configurar Jackson con soporte para fechas Java 8 ────────────────
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ── Crear la app Javalin ──────────────────────────────────────────────
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper));
            // Servir archivos estáticos desde /frontend en el classpath
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath   = "/";
                staticFiles.directory    = "/frontend";
                staticFiles.precompress  = false;
            });
            // CORS permisivo para desarrollo local
            config.bundledPlugins.enableCors(cors ->
                cors.addRule(it -> it.anyHost())
            );
        });

        // ── CORS headers manuales (por si acaso) ─────────────────────────────
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin",  "*");
            ctx.header("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });
        app.options("/*", ctx -> ctx.status(204));

        // ── Inicializar cache de estructuras de datos ─────────────────────────
        try {
            animalService.inicializar();
            System.out.println("✅ Estructuras de datos inicializadas (TablaHash + BST)");
        } catch (Exception e) {
            System.err.println("⚠️  No se pudo conectar a la BD: " + e.getMessage());
            System.err.println("   El servidor arrancará igualmente; verifica db.properties");
        }

        // ── Registrar rutas ───────────────────────────────────────────────────
        registrarRutasAnimales(app);
        registrarRutasSolicitudes(app);
        registrarRutasVeterinaria(app);
        registrarRutasCompatibilidad(app);
        registrarRutasUsuarios(app);

        // ── Manejo global de errores ──────────────────────────────────────────
        app.exception(Exception.class, (e, ctx) -> {
            System.err.println("Error en " + ctx.path() + ": " + e.getMessage());
            ctx.status(500).json(Map.of("error", e.getMessage()));
        });

        app.start(7070);
        System.out.println("🐾 Huellitas corriendo en http://localhost:7070");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ANIMALES
    // ─────────────────────────────────────────────────────────────────────────

    private static void registrarRutasAnimales(Javalin app) {

        // GET /api/animales  → todos (o filtrar por ?estado=Disponible)
        app.get("/api/animales", ctx -> {
            String estado = ctx.queryParam("estado");
            List<Animal> lista = (estado != null && !estado.isBlank())
                ? new AnimalDAO().obtenerPorEstado(estado)
                : animalService.listarTodos();
            ctx.json(lista);
        });

        // GET /api/animales/{id}
        app.get("/api/animales/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Animal a = animalService.buscarPorId(id);
            if (a == null) ctx.status(404).json(Map.of("error", "Animal no encontrado"));
            else ctx.json(a);
        });

        // POST /api/animales  → crear
        app.post("/api/animales", ctx -> {
            Animal a = ctx.bodyAsClass(Animal.class);
            ctx.status(201).json(animalService.crear(a));
        });

        // PATCH /api/animales/{id}/estado  → actualizar estado de adopción
        app.patch("/api/animales/{id}/estado", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String nuevoEstado = String.valueOf(body.get("estadoDeAdopcion"));
            animalService.actualizarEstado(id, nuevoEstado);
            ctx.json(Map.of("ok", true));
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SOLICITUDES DE ADOPCIÓN
    // ─────────────────────────────────────────────────────────────────────────

    private static void registrarRutasSolicitudes(Javalin app) {

        // GET /api/solicitudes?estado=pendiente
        app.get("/api/solicitudes", ctx -> {
            String estado = ctx.queryParam("estado");
            ctx.json(solicitudService.listar(estado));
        });

        // GET /api/solicitudes/{id}
        app.get("/api/solicitudes/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Solicitud s = solicitudService.buscarPorId(id);
            if (s == null) ctx.status(404).json(Map.of("error", "Solicitud no encontrada"));
            else ctx.json(s);
        });

        // POST /api/solicitudes  → crear nueva solicitud
        app.post("/api/solicitudes", ctx -> {
            Solicitud s = ctx.bodyAsClass(Solicitud.class);
            ctx.status(201).json(solicitudService.crear(s));
        });

        // PATCH /api/solicitudes/{id}  → aprobar o rechazar
        app.patch("/api/solicitudes/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String nuevoEstado = String.valueOf(body.get("estado"));
            Integer revisadoPor = body.get("revisadoPor") != null
                ? ((Number) body.get("revisadoPor")).intValue() : null;
            String notas = body.get("notas") != null ? String.valueOf(body.get("notas")) : null;

            solicitudService.revisar(id, nuevoEstado, revisadoPor, notas);
            ctx.json(Map.of("ok", true));
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VETERINARIA — Cola de prioridad
    // ─────────────────────────────────────────────────────────────────────────

    private static void registrarRutasVeterinaria(Javalin app) {

        // GET /api/veterinaria/cola
        app.get("/api/veterinaria/cola", ctx -> {
            List<ColaPrioridadVeterinaria.ElementoCola> cola =
                veterinariaService.obtenerColaDeAtencion();

            // Serializar manualmente para incluir el nivel de texto
            var resultado = cola.stream().map(e -> Map.of(
                "animalId",     e.animal.animalId,
                "nombre",       e.animal.nombre,
                "especie",      e.animal.especieNombre != null ? e.animal.especieNombre : "Desconocida",
                "estadoDeSalud", e.animal.estadoDeSalud != null ? e.animal.estadoDeSalud : "Sin registro",
                "prioridad",    e.prioridad,
                "nivelTexto",   e.nivelTexto
            )).toList();

            ctx.json(resultado);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COMPATIBILIDAD — Grafo
    // ─────────────────────────────────────────────────────────────────────────

    private static void registrarRutasCompatibilidad(Javalin app) {

        // GET /api/compatibilidad/{usuarioId}?maximo=3
        app.get("/api/compatibilidad/{usuarioId}", ctx -> {
            int usuarioId = Integer.parseInt(ctx.pathParam("usuarioId"));
            int maximo    = Integer.parseInt(ctx.queryParamAsClass("maximo", String.class)
                .getOrDefault("3"));

            List<GrafoCompatibilidad.Arista> sugerencias =
                compatService.sugerirAnimales(usuarioId, maximo);

            var resultado = sugerencias.stream().map(a -> Map.of(
                "animalId",     a.animal.animalId,
                "nombre",       a.animal.nombre,
                "especie",      a.animal.especieNombre != null ? a.animal.especieNombre : "Otro",
                "descripcion",  a.animal.descripcion != null ? a.animal.descripcion : "",
                "urlDeImagen",  a.animal.urlDeImagen != null ? a.animal.urlDeImagen : "",
                "tamano",       a.animal.tamano != null ? a.animal.tamano : "",
                "score",        a.peso,
                "razones",      a.razones
            )).toList();

            ctx.json(resultado);
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // USUARIOS
    // ─────────────────────────────────────────────────────────────────────────

    private static void registrarRutasUsuarios(Javalin app) {

        // GET /api/usuarios
        app.get("/api/usuarios", ctx -> {
            String sql = "SELECT usuario_id, primer_nombre, apellido, email, " +
                         "       telefono, rol, estado, creado_en FROM usuarios";
            try (Connection con = Database.conectar();
                 Statement st  = con.createStatement();
                 ResultSet rs  = st.executeQuery(sql)) {
                var lista = new java.util.ArrayList<Map<?, ?>>();
                while (rs.next()) {
                    lista.add(Map.of(
                        "usuarioId",     rs.getInt("usuario_id"),
                        "primerNombre",  rs.getString("primer_nombre"),
                        "apellido",      rs.getString("apellido"),
                        "email",         rs.getString("email"),
                        "rol",           rs.getString("rol"),
                        "estado",        rs.getBoolean("estado")
                    ));
                }
                ctx.json(lista);
            }
        });

        // POST /api/usuarios  → registrar nuevo usuario (adoptante anónimo)
        app.post("/api/usuarios", ctx -> {
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String sql = "INSERT INTO usuarios " +
                "(primer_nombre, apellido, email, `contraseña`, telefono, rol, estado) " +
                "VALUES (?,?,?,?,?,?,1)";
            try (Connection con = Database.conectar();
                 PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, str(body, "primerNombre"));
                ps.setString(2, str(body, "apellido"));
                ps.setString(3, str(body, "email"));
                ps.setString(4, str(body, "contrasena")); // se guarda como viene (texto plano en demo)
                Object tel = body.get("telefono");
                if (tel != null) ps.setInt(5, ((Number) tel).intValue());
                else ps.setNull(5, Types.INTEGER);
                ps.setString(6, body.get("rol") != null ? str(body, "rol") : "adoptante");
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    int nuevoId = keys.next() ? keys.getInt(1) : 0;
                    ctx.status(201).json(Map.of("usuarioId", nuevoId));
                }
            }
        });

        // POST /api/usuarios/login  → autenticación
        app.post("/api/usuarios/login", ctx -> {
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String email = str(body, "email");
            String pass  = str(body, "contrasena");

            String sql = "SELECT usuario_id, primer_nombre, apellido, email, rol, estado " +
                         "FROM usuarios WHERE email = ? AND `contraseña` = ? AND estado = 1";
            try (Connection con = Database.conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, pass);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ctx.json(Map.of(
                            "usuarioId",    rs.getInt("usuario_id"),
                            "primerNombre", rs.getString("primer_nombre"),
                            "apellido",     rs.getString("apellido"),
                            "email",        rs.getString("email"),
                            "rol",          rs.getString("rol")
                        ));
                    } else {
                        ctx.status(401).json(Map.of("error", "Correo o contraseña incorrectos"));
                    }
                }
            }
        });
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static String str(Map<?, ?> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "";
    }
}
