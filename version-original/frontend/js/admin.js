// Panel interno: tres pestañas que muestran los algoritmos del curso en accion:
// - Solicitudes: Merge Sort (orden por fecha) + Busqueda Binaria (al abrir una solicitud)
// - Cola veterinaria: Cola de Prioridad (heap) segun nivel de urgencia
// - Compatibilidad: Grafo ponderado adoptante-animal

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".pestana").forEach((pestana) => {
        pestana.addEventListener("click", () => cambiarPestana(pestana.dataset.pestana));
    });
    cargarSolicitudes();
});

function cambiarPestana(nombre) {
    document.querySelectorAll(".pestana").forEach((p) => p.classList.toggle("activa", p.dataset.pestana === nombre));
    document.getElementById("panel-solicitudes").hidden = nombre !== "solicitudes";
    document.getElementById("panel-veterinaria").hidden = nombre !== "veterinaria";
    document.getElementById("panel-compatibilidad").hidden = nombre !== "compatibilidad";

    if (nombre === "solicitudes") cargarSolicitudes();
    if (nombre === "veterinaria") cargarColaVeterinaria();
    if (nombre === "compatibilidad") cargarPanelCompatibilidad();
}

// ---------- Solicitudes de adopcion ----------

async function cargarSolicitudes() {
    const panel = document.getElementById("panel-solicitudes");
    panel.innerHTML = `<p class="mensaje-vacio">Cargando solicitudes...</p>`;
    try {
        const solicitudes = await apiGet("/solicitudes-adopcion");
        panel.innerHTML = `
            <div class="tarjeta">
                <label for="input-revisor">ID de usuario staff que revisa (para aprobar/rechazar):</label>
                <input type="number" id="input-revisor" value="1" style="width:80px; margin-left:8px;">
            </div>
            <table class="tabla-datos">
                <thead>
                    <tr><th>ID</th><th>Animal</th><th>Solicitante</th><th>Fecha</th><th>Estado</th><th>Motivación</th><th>Acciones</th></tr>
                </thead>
                <tbody>${solicitudes.map(filaSolicitud).join("")}</tbody>
            </table>
        `;
    } catch (error) {
        panel.innerHTML = `<p class="mensaje-vacio">No se pudieron cargar las solicitudes: ${error.message}</p>`;
    }
}

function filaSolicitud(solicitud) {
    let acciones = "";
    if (solicitud.estado === "pendiente") {
        acciones = `
            <button class="boton-mini aprobar" onclick="revisarSolicitud(${solicitud.solicitudId}, 'aprobada')">Aprobar</button>
            <button class="boton-mini rechazar" onclick="revisarSolicitud(${solicitud.solicitudId}, 'rechazada')">Rechazar</button>
        `;
    } else if (solicitud.estado === "aprobada") {
        acciones = `<button class="boton-mini confirmar" onclick="confirmarAdopcion(${solicitud.solicitudId})">Confirmar adopción</button>`;
    }
    return `
        <tr>
            <td>#${solicitud.solicitudId}</td>
            <td>${solicitud.animalNombre}</td>
            <td>${solicitud.solicitanteNombre}</td>
            <td>${formatearFecha(solicitud.fechaDeSolicitud)}</td>
            <td>${solicitud.estado}</td>
            <td>${solicitud.motivacion || ""}</td>
            <td>${acciones}</td>
        </tr>
    `;
}

async function revisarSolicitud(solicitudId, nuevoEstado) {
    const revisadoPor = Number(document.getElementById("input-revisor").value || 1);
    try {
        await apiEnviar("PATCH", `/solicitudes-adopcion/${solicitudId}`, { estado: nuevoEstado, revisadoPor });
        cargarSolicitudes();
    } catch (error) {
        alert("No se pudo actualizar la solicitud: " + error.message);
    }
}

async function confirmarAdopcion(solicitudId) {
    try {
        await apiEnviar("POST", `/solicitudes-adopcion/${solicitudId}/confirmar`, { notas: "Adopcion confirmada desde el panel" });
        cargarSolicitudes();
    } catch (error) {
        alert("No se pudo confirmar la adopcion: " + error.message);
    }
}

// ---------- Cola de prioridad veterinaria ----------

async function cargarColaVeterinaria() {
    const panel = document.getElementById("panel-veterinaria");
    panel.innerHTML = `<p class="mensaje-vacio">Cargando cola de atención...</p>`;
    try {
        const cola = await apiGet("/veterinaria/cola");
        if (cola.length === 0) {
            panel.innerHTML = `<p class="mensaje-vacio">No hay animales pendientes de atención veterinaria.</p>`;
            return;
        }
        panel.innerHTML = `
            <table class="tabla-datos">
                <thead><tr><th>Orden</th><th>Animal</th><th>Urgencia</th><th>Estado de salud</th></tr></thead>
                <tbody>
                    ${cola.map((item, indice) => `
                        <tr>
                            <td>${indice + 1}</td>
                            <td>${item.animal.nombre}</td>
                            <td><span class="etiqueta ${item.nivelUrgencia.toLowerCase()}">${item.nivelUrgencia}</span></td>
                            <td>${item.motivo || ""}</td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    } catch (error) {
        panel.innerHTML = `<p class="mensaje-vacio">No se pudo cargar la cola veterinaria: ${error.message}</p>`;
    }
}

// ---------- Sugerencias de compatibilidad (grafo) ----------

async function cargarPanelCompatibilidad() {
    const panel = document.getElementById("panel-compatibilidad");
    panel.innerHTML = `<p class="mensaje-vacio">Cargando usuarios...</p>`;
    try {
        const usuarios = await apiGet("/usuarios");
        panel.innerHTML = `
            <div class="tarjeta">
                <label for="select-adoptante">Ver sugerencias para el usuario:</label>
                <select id="select-adoptante" style="margin-left:8px;">
                    <option value="">Selecciona un usuario</option>
                    ${usuarios.map(u => `<option value="${u.usuarioId}">${u.primerNombre} ${u.apellido} (#${u.usuarioId})</option>`).join("")}
                </select>
            </div>
            <div id="resultado-sugerencias"></div>
        `;
        document.getElementById("select-adoptante").addEventListener("change", cargarSugerencias);
    } catch (error) {
        panel.innerHTML = `<p class="mensaje-vacio">No se pudieron cargar los usuarios: ${error.message}</p>`;
    }
}

async function cargarSugerencias() {
    const usuarioId = document.getElementById("select-adoptante").value;
    const resultado = document.getElementById("resultado-sugerencias");
    if (!usuarioId) {
        resultado.innerHTML = "";
        return;
    }
    resultado.innerHTML = `<p class="mensaje-vacio">Calculando compatibilidad...</p>`;
    try {
        const sugerencias = await apiGet(`/usuarios/${usuarioId}/sugerencias?maximo=8`);
        if (sugerencias.length === 0) {
            resultado.innerHTML = `<p class="mensaje-vacio">Este usuario no tiene preferencias guardadas, o no hay animales compatibles disponibles.</p>`;
            return;
        }
        resultado.innerHTML = `
            <table class="tabla-datos">
                <thead><tr><th>Animal</th><th>Especie</th><th>Compatibilidad</th></tr></thead>
                <tbody>
                    ${sugerencias.map(a => `
                        <tr>
                            <td><a href="animal.html?id=${a.animal.animalId}">${a.animal.nombre}</a></td>
                            <td>${a.animal.especieNombre}</td>
                            <td>${a.peso}%</td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    } catch (error) {
        resultado.innerHTML = `<p class="mensaje-vacio">Error al calcular sugerencias: ${error.message}</p>`;
    }
}

function formatearFecha(fechaIso) {
    if (!fechaIso) return "Fecha desconocida";
    return new Date(fechaIso).toLocaleString("es-GT");
}
