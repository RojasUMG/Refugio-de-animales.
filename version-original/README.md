# Refugio de Animales — Sistema completo

Proyecto final del curso de Algoritmos (UMG). Backend en Java 21 + Javalin conectado
por JDBC a MySQL, y frontend web en HTML/CSS/JS puro, estilo Petfinder.

## Estructura del proyecto

```
refugio-animales/
  pom.xml                          -> dependencias y build (Maven)
  mvnw.cmd / mvnw                  -> Maven Wrapper (no necesitas instalar Maven)
  src/main/resources/db.properties -> credenciales de MySQL (creala a partir de db.properties.example)
  src/main/java/com/refugio/
    Main.java                      -> arranca el servidor y define las rutas HTTP
    config/Database.java           -> conexion JDBC a MySQL
    modelo/                        -> una clase por tabla (Animal, Usuario, etc.)
    dao/                           -> el SQL: un DAO por tabla, con JDBC puro
    estructuras/                   -> los algoritmos del curso (ver abajo)
    servicio/                      -> logica de negocio, conecta dao + estructuras
  frontend/
    index.html                     -> pagina publica: lista de animales con filtros
    animal.html                    -> ficha de un animal + formulario de solicitud
    admin.html                     -> panel interno: solicitudes, cola veterinaria, compatibilidad
    css/, js/
```

## 1. Configura tu contraseña de MySQL

1. Copia `src/main/resources/db.properties.example` a `src/main/resources/db.properties`
   (si no existe ya).
2. Abre `db.properties` y reemplaza `CAMBIA_ESTA_CONTRASENA` por tu contraseña real de
   MySQL (usuario `root`, puerto `3306`, base `refugio_de_animales`).

Este archivo NO se sube a git (esta en `.gitignore`) porque contiene tu contraseña.

## 2. Compilar y ejecutar

Abre una terminal (PowerShell) en la carpeta `refugio-animales` y ejecuta:

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target\refugio-animales.jar
```

Si ves el mensaje `Conexion a MySQL exitosa` y luego `Servidor listo en
http://localhost:7070`, todo esta funcionando.

> Nota: si `mvnw.cmd` se queja de `JAVA_HOME no encontrado`, corre esto una vez en esa
> terminal (ya quedo configurado de forma permanente en tu usuario de Windows, pero una
> terminal que ya estaba abierta no lo ve hasta que la cierras y abres una nueva):
> ```powershell
> $env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"
> ```

## 3. Abrir el sitio

Con el servidor corriendo, abre en tu navegador:

- **http://localhost:7070/** — pagina publica (estilo Petfinder): tarjetas de animales,
  filtros por especie/tamaño/sexo/nombre, y ficha de detalle con formulario de solicitud
  de adopcion.
- **http://localhost:7070/admin.html** — panel interno del refugio: revisar/aprobar
  solicitudes, confirmar adopciones, ver la cola de atencion veterinaria y las
  sugerencias de compatibilidad adoptante-animal.

No necesitas abrir los archivos `.html` directamente con doble clic: el mismo servidor
Javalin los sirve, para evitar problemas de CORS.

## 4. Volver a compilar tras cambiar el codigo Java

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target\refugio-animales.jar
```

Los archivos de `frontend/` (HTML/CSS/JS) NO necesitan recompilar: solo guarda el
archivo y refresca el navegador (F5).

## Donde esta cada algoritmo del curso

| Algoritmo requerido            | Archivo                                             | Donde se usa |
|---------------------------------|------------------------------------------------------|--------------|
| Arbol Binario de Busqueda       | `estructuras/ArbolBinarioBusqueda.java`              | `AnimalService.listarOrdenadosPorNombre()` |
| Tabla hash (propia)             | `estructuras/TablaHash.java`                         | `AnimalService.buscarPorId()` / `buscarPorNombre()` |
| Cola de prioridad (heap)        | `estructuras/ColaPrioridadVeterinaria.java`          | `VeterinariaService.obtenerColaDeAtencion()`, visible en `/admin.html` pestaña "Cola veterinaria" |
| Grafo ponderado de compatibilidad | `estructuras/GrafoCompatibilidad.java`             | `CompatibilidadService.sugerirAnimales()`, visible en `/admin.html` pestaña "Sugerencias de compatibilidad" |
| Ordenamiento (Merge Sort)        | `estructuras/Ordenamiento.java`                     | `SolicitudService.listar()` (ordena solicitudes por fecha) |
| Busqueda binaria                | `estructuras/BusquedaBinaria.java`                   | `SolicitudService.buscarPorId()` |

## Decisiones tecnicas (y por que)

- **Javalin en vez de Spring Boot:** para un proyecto de este tamano, Spring Boot agrega
  mucha "magia" (anotaciones, inyeccion de dependencias, auto-configuracion) que es dificil
  de explicar en una defensa de tesis si apenas estan aprendiendo Java. Javalin es una
  libreria delgada: `app.get("/ruta", ctx -> ...)` — cada linea es explicita y facil de
  seguir paso a paso.
- **JDBC puro, sin ORM (Hibernate/JPA):** se ve exactamente que SQL se ejecuta en cada
  DAO, sin generacion automatica de queries. Mas verboso, pero mas transparente para el curso.
- **Campos publicos en los modelos (sin getters/setters):** Jackson (la libreria de JSON)
  los lee directamente. Menos codigo repetitivo para alguien que recien esta aprendiendo Java.
- **Sin sistema de login:** el enfoque del curso es la logica de datos/algoritmos, no
  autenticacion. Las solicitudes de adopcion piden elegir un `usuario_id` ya existente en
  la tabla `usuarios` (puedes crear usuarios de prueba directamente en MySQL Workbench).
