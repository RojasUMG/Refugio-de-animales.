// Pagina de detalle: lee el id del animal desde la URL (?id=) y pinta su ficha,
// historial de vacunas/medico, y un formulario para levantar una solicitud de adopcion.

document.addEventListener("DOMContentLoaded", async () => {
    const idAnimal = new URLSearchParams(window.location.search).get("id");
    const contenedor = document.getElementById("contenido-detalle");

    if (!idAnimal) {
        contenedor.innerHTML = `<p class="mensaje-vacio">No se especifico un animal.</p>`;
        return;
    }

    try {
        const [animal, vacunas, registrosMedicos, usuarios] = await Promise.all([
            apiGet(`/animales/${idAnimal}`),
            apiGet(`/animales/${idAnimal}/vacunas`),
            apiGet(`/animales/${idAnimal}/registros-medicos`),
            apiGet(`/usuarios`),
        ]);
        renderizarFicha(animal, vacunas, registrosMedicos, usuarios);
    } catch (error) {
        contenedor.innerHTML = `<p class="mensaje-vacio">No se pudo cargar el animal: ${error.message}</p>`;
    }
});

function renderizarFicha(animal, vacunas, registrosMedicos, usuarios) {
    const contenedor = document.getElementById("contenido-detalle");

    const filasVacunas = vacunas.length
        ? vacunas.map(v => `<li>${v.nombreDeVacuna} — aplicada el ${v.fechaDeAplicacion}</li>`).join("")
        : "<li>Sin vacunas registradas todavia.</li>";

    const filasMedicas = registrosMedicos.length
        ? registrosMedicos.map(r => `<li>${formatearFecha(r.fechaDeVisita)} — ${r.razon || "Consulta"}: ${r.diagnostico || "Sin diagnostico registrado"}</li>`).join("")
        : "<li>Sin registros medicos todavia.</li>";

    const opcionesUsuarios = usuarios
        .map(u => `<option value="${u.usuarioId}">${u.primerNombre} ${u.apellido} (#${u.usuarioId})</option>`)
        .join("");

    const puedeAdoptar = claseEtiquetaEstado(animal.estadoDeAdopcion) === "disponible";

    contenedor.innerHTML = `
        <div class="detalle-animal">
            <div>
                <img class="foto-grande" src="${animal.urlDeImagen || 'img/sin-foto.svg'}" alt="Foto de ${animal.nombre}"
                     onerror="this.src='img/sin-foto.svg'">
            </div>
            <div>
                <span class="etiqueta ${claseEtiquetaEstado(animal.estadoDeAdopcion)}">${animal.estadoDeAdopcion || "Disponible"}</span>
                <h1>${animal.nombre}</h1>
                <p class="meta">${animal.especieNombre} · ${animal.raza || "Raza mixta"}</p>
                <p>${animal.descripcion || "Este animal todavia no tiene una descripcion."}</p>

                <div class="ficha">
                    <div><span class="etiqueta-campo">Edad</span>${calcularEdadTexto(animal.fechaDeNacimiento)}</div>
                    <div><span class="etiqueta-campo">Sexo</span>${animal.sexo || "No especificado"}</div>
                    <div><span class="etiqueta-campo">Tamaño</span>${animal.tamano || "No especificado"}</div>
                    <div><span class="etiqueta-campo">Color</span>${animal.color || "No especificado"}</div>
                    <div><span class="etiqueta-campo">Estado de salud</span>${animal.estadoDeSalud || "No especificado"}</div>
                    <div><span class="etiqueta-campo">Llegada al refugio</span>${animal.fechaDeLlegada}</div>
                </div>

                <div class="tarjeta">
                    <h2>Solicitar adopción</h2>
                    ${puedeAdoptar ? `
                    <form class="formulario-solicitud" id="formulario-solicitud">
                        <label for="select-solicitante">Adoptante (usuario registrado)</label>
                        <select id="select-solicitante" required>
                            <option value="">Selecciona tu usuario</option>
                            ${opcionesUsuarios}
                        </select>
                        <label for="texto-motivacion">Cuéntanos por qué quieres adoptar a ${animal.nombre}</label>
                        <textarea id="texto-motivacion" required placeholder="Motivación de adopción"></textarea>
                        <button type="submit" class="boton">Enviar solicitud</button>
                        <div id="aviso-solicitud"></div>
                    </form>
                    ` : `<p>Este animal no esta disponible para adopcion en este momento.</p>`}
                </div>
            </div>
        </div>

        <div class="tarjeta">
            <h2>Historial de vacunas</h2>
            <ul>${filasVacunas}</ul>
        </div>

        <div class="tarjeta">
            <h2>Historial médico</h2>
            <ul>${filasMedicas}</ul>
        </div>
    `;

    const formulario = document.getElementById("formulario-solicitud");
    if (formulario) {
        formulario.addEventListener("submit", (evento) => enviarSolicitud(evento, animal.animalId));
    }
}

async function enviarSolicitud(evento, animalId) {
    evento.preventDefault();
    const solicitanteId = document.getElementById("select-solicitante").value;
    const motivacion = document.getElementById("texto-motivacion").value;
    const aviso = document.getElementById("aviso-solicitud");

    try {
        await apiEnviar("POST", "/solicitudes-adopcion", {
            solicitanteId: Number(solicitanteId),
            animalId: animalId,
            motivacion: motivacion,
        });
        aviso.innerHTML = `<p class="aviso exito">Solicitud enviada. El equipo del refugio la revisara pronto.</p>`;
        document.getElementById("formulario-solicitud").reset();
    } catch (error) {
        aviso.innerHTML = `<p class="aviso error">No se pudo enviar la solicitud: ${error.message}</p>`;
    }
}

function formatearFecha(fechaIso) {
    if (!fechaIso) return "Fecha desconocida";
    return new Date(fechaIso).toLocaleString("es-GT");
}
