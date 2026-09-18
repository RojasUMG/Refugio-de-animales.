package com.refugio;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.refugio.config.Database;
import com.refugio.dao.AdopcionDAO;
import com.refugio.dao.AnimalDAO;
import com.refugio.dao.EspecieDAO;
import com.refugio.dao.PreferenciaAdoptanteDAO;
import com.refugio.dao.RegistroMedicoDAO;
import com.refugio.dao.RegistroRescateDAO;
import com.refugio.dao.UsuarioDAO;
import com.refugio.dao.VacunaDAO;
import com.refugio.modelo.Animal;
import com.refugio.modelo.PreferenciaAdoptante;
import com.refugio.modelo.RegistroMedico;
import com.refugio.modelo.RegistroRescate;
import com.refugio.modelo.SolicitudAdopcion;
import com.refugio.modelo.Usuario;
import com.refugio.modelo.Vacuna;
import com.refugio.servicio.AnimalService;
import com.refugio.servicio.CompatibilidadService;
import com.refugio.servicio.SolicitudService;
import com.refugio.servicio.VeterinariaService;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinJackson;

/**
 * Arranca el servidor web (Javalin) y define todas las rutas de la API REST.
 * Cada ruta es delgada a proposito: solo traduce HTTP <-> el metodo del
 * service/DAO correspondiente. La logica de negocio vive en com.refugio.servicio,
 * el SQL vive en com.refugio.dao y los algoritmos del curso viven en com.refugio.estructuras.
 */
public class Main {

    private static final AnimalService animalService = new AnimalService();
    private static final VeterinariaService veterinariaService = new VeterinariaService();
    private static final SolicitudService solicitudService = new SolicitudService();
    private static final CompatibilidadService compatibilidadService = new CompatibilidadService();

    private static final EspecieDAO especieDAO = new EspecieDAO();
    private static final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private static final PreferenciaAdoptanteDAO preferenciaDAO = new PreferenciaAdoptanteDAO();
    private static final RegistroMedicoDAO registroMedicoDAO = new RegistroMedicoDAO();
    private static final RegistroRescateDAO registroRescateDAO = new RegistroRescateDAO();
    private static final VacunaDAO vacunaDAO = new VacunaDAO();
    private static final AdopcionDAO adopcionDAO = new AdopcionDAO();

    public static void main(String[] args) {
        Database.probarConexion();

        // Sin esto, Jackson serializa LocalDate/LocalDateTime como arreglos [2026,9,12,...]
        // en vez de texto ISO-8601 ("2026-09-12"), y `new Date(...)` en el navegador no lo entiende.
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(mapper, true));
            config.staticFiles.add(staticFiles -> {
                staticFiles.directory = "frontend";
                staticFiles.location = Location.EXTERNAL;
            });
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost()));
        });

        registrarRutasAnimales(app);
        registrarRutasSalud(app);
        registrarRutasSolicitudesYAdopciones(app);
        registrarRutasUsuarios(app);

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).json(Map.of("error", e.getMessage() != null ? e.getMessage() : "Error interno"));
        });

        int puerto = 7070;
        app.start(puerto);
        System.out.println("Servidor listo en http://localhost:" + puerto);
    }

    private static void registrarRutasAnimales(Javalin app) {
        app.get("/api/especies", ctx -> ctx.json(especieDAO.obtenerTodas()));

        app.get("/api/animales", ctx -> {
            AnimalDAO.Filtros filtros = new AnimalDAO.Filtros();
            filtros.especiesId = ctx.queryParamAsClass("especie", Integer.class).getOrDefault(null);
            filtros.tamano = ctx.queryParam("tamano");
            filtros.sexo = ctx.queryParam("sexo");
            filtros.estadoDeAdopcion = ctx.queryParam("estado");
            ctx.json(animalService.listarConFiltros(filtros));
        });

        app.get("/api/animales/ordenados", ctx -> ctx.json(animalService.listarOrdenadosPorNombre()));

        app.get("/api/animales/buscar", ctx -> {
            String nombre = ctx.queryParam("nombre");
            if (nombre == null || nombre.isBlank()) {
                ctx.status(400).json(Map.of("error", "Debes enviar ?nombre="));
                return;
            }
            Animal animal = animalService.buscarPorNombre(nombre);
            if (animal == null) {
                ctx.status(404).json(Map.of("error", "No se encontro un animal con ese nombre"));
                return;
            }
            ctx.json(animal);
        });

        app.get("/api/animales/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Animal animal = animalService.buscarPorId(id);
            if (animal == null) {
                ctx.status(404).json(Map.of("error", "Animal no encontrado"));
                return;
            }
            ctx.json(animal);
        });

        app.post("/api/animales", ctx -> {
            Animal animal = ctx.bodyAsClass(Animal.class);
            int id = animalService.registrar(animal);
            ctx.status(201).json(Map.of("animalId", id));
        });

        app.patch("/api/animales/{id}/estado", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            animalService.actualizarEstadoDeAdopcion(id, String.valueOf(body.get("estadoDeAdopcion")));
            ctx.status(204);
        });

        app.get("/api/animales/{id}/registros-medicos", ctx ->
                ctx.json(registroMedicoDAO.obtenerPorAnimal(Integer.parseInt(ctx.pathParam("id")))));

        app.get("/api/animales/{id}/vacunas", ctx ->
                ctx.json(vacunaDAO.obtenerPorAnimal(Integer.parseInt(ctx.pathParam("id")))));

        app.get("/api/animales/{id}/registros-rescate", ctx ->
                ctx.json(registroRescateDAO.obtenerPorAnimal(Integer.parseInt(ctx.pathParam("id")))));
    }

    private static void registrarRutasSalud(Javalin app) {
        app.post("/api/registros-medicos", ctx -> {
            RegistroMedico registro = ctx.bodyAsClass(RegistroMedico.class);
            int id = registroMedicoDAO.crear(registro);
            ctx.status(201).json(Map.of("registroMedicoId", id));
        });

        app.post("/api/vacunas", ctx -> {
            Vacuna vacuna = ctx.bodyAsClass(Vacuna.class);
            int id = vacunaDAO.crear(vacuna);
            ctx.status(201).json(Map.of("vacunasId", id));
        });

        app.post("/api/registros-rescate", ctx -> {
            RegistroRescate registro = ctx.bodyAsClass(RegistroRescate.class);
            int id = registroRescateDAO.crear(registro);
            ctx.status(201).json(Map.of("registroId", id));
        });

        app.get("/api/veterinaria/cola", ctx -> ctx.json(veterinariaService.obtenerColaDeAtencion()));
    }

    private static void registrarRutasSolicitudesYAdopciones(Javalin app) {
        app.get("/api/solicitudes-adopcion", ctx ->
                ctx.json(solicitudService.listar(ctx.queryParam("estado"))));

        app.get("/api/solicitudes-adopcion/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            SolicitudAdopcion solicitud = solicitudService.buscarPorId(id);
            if (solicitud == null) {
                ctx.status(404).json(Map.of("error", "Solicitud no encontrada"));
                return;
            }
            ctx.json(solicitud);
        });

        app.post("/api/solicitudes-adopcion", ctx -> {
            SolicitudAdopcion solicitud = ctx.bodyAsClass(SolicitudAdopcion.class);
            int id = solicitudService.crear(solicitud);
            ctx.status(201).json(Map.of("solicitudId", id));
        });

        app.patch("/api/solicitudes-adopcion/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String estado = String.valueOf(body.get("estado"));
            int revisadoPor = Integer.parseInt(String.valueOf(body.get("revisadoPor")));
            String notas = body.get("notas") != null ? String.valueOf(body.get("notas")) : null;
            solicitudService.revisar(id, estado, revisadoPor, notas);
            ctx.status(204);
        });

        app.post("/api/solicitudes-adopcion/{id}/confirmar", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Map<?, ?> body = ctx.bodyAsClass(Map.class);
            String notas = body.get("notas") != null ? String.valueOf(body.get("notas")) : null;
            int adopcionId = solicitudService.confirmarAdopcion(id, notas);
            ctx.status(201).json(Map.of("adopcionId", adopcionId));
        });

        app.get("/api/adopciones", ctx -> ctx.json(adopcionDAO.obtenerTodas()));
    }

    private static void registrarRutasUsuarios(Javalin app) {
        app.get("/api/usuarios", ctx -> ctx.json(usuarioDAO.obtenerTodos()));

        app.post("/api/usuarios", ctx -> {
            Usuario usuario = ctx.bodyAsClass(Usuario.class);
            int id = usuarioDAO.crear(usuario);
            ctx.status(201).json(Map.of("usuarioId", id));
        });

        app.get("/api/usuarios/{id}/preferencias", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            PreferenciaAdoptante preferencia = preferenciaDAO.obtenerPorUsuario(id);
            if (preferencia == null) {
                ctx.status(404).json(Map.of("error", "Este usuario no tiene preferencias guardadas"));
                return;
            }
            ctx.json(preferencia);
        });

        app.post("/api/usuarios/{id}/preferencias", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            PreferenciaAdoptante preferencia = ctx.bodyAsClass(PreferenciaAdoptante.class);
            preferencia.usuarioId = id;
            int preferenciaId = preferenciaDAO.guardar(preferencia);
            ctx.status(201).json(Map.of("preferenciaId", preferenciaId));
        });

        app.get("/api/usuarios/{id}/sugerencias", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            int maximo = ctx.queryParamAsClass("maximo", Integer.class).getOrDefault(5);
            ctx.json(compatibilidadService.sugerirAnimales(id, maximo));
        });
    }
}
