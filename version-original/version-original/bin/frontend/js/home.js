// Pagina de inicio: carga todos los animales una vez y filtra en el navegador.
// Se filtra en el cliente (no se vuelve a pedir al servidor en cada cambio) porque
// el catalogo de un refugio es pequeno; para miles de animales convendria mandar
// los filtros directo a GET /api/animales?especie=&tamano=&sexo=.

let todosLosAnimales = [];

document.addEventListener("DOMContentLoaded", async () => {
    document.getElementById("formulario-filtros").addEventListener("submit", (evento) => {
        evento.preventDefault();
        aplicarFiltros();
    });
    document.getElementById("boton-limpiar").addEventListener("click", () => {
        document.getElementById("formulario-filtros").reset();
        aplicarFiltros();
    });

    try {
        const [especies, animales] = await Promise.all([
            apiGet("/especies"),
            apiGet("/animales"),
        ]);
        todosLosAnimales = animales;
        llenarSelect("filtro-especie", especies.map(e => ({ valor: e.especiesId, texto: e.nombre })));
        llenarSelect("filtro-tamano", valoresUnicos(animales, "tamano"));
        llenarSelect("filtro-sexo", valoresUnicos(animales, "sexo"));
        aplicarFiltros();
    } catch (error) {
        document.getElementById("rejilla-animales").innerHTML =
            `<p class="mensaje-vacio">No se pudo conectar con el servidor: ${error.message}</p>`;
    }
});

function valoresUnicos(animales, campo) {
    const vistos = new Set();
    const resultado = [];
    for (const animal of animales) {
        const valor = animal[campo];
        if (valor && !vistos.has(valor)) {
            vistos.add(valor);
            resultado.push({ valor, texto: valor });
        }
    }
    return resultado;
}

function llenarSelect(idSelect, opciones) {
    const select = document.getElementById(idSelect);
    for (const opcion of opciones) {
        const elementoOpcion = document.createElement("option");
        elementoOpcion.value = opcion.valor;
        elementoOpcion.textContent = opcion.texto;
        select.appendChild(elementoOpcion);
    }
}

function aplicarFiltros() {
    const especieId = document.getElementById("filtro-especie").value;
    const tamano = document.getElementById("filtro-tamano").value;
    const sexo = document.getElementById("filtro-sexo").value;
    const nombreBuscado = document.getElementById("filtro-nombre").value.trim().toLowerCase();

    const filtrados = todosLosAnimales.filter((animal) => {
        if (especieId && String(animal.especiesId) !== especieId) return false;
        if (tamano && animal.tamano !== tamano) return false;
        if (sexo && animal.sexo !== sexo) return false;
        if (nombreBuscado && !animal.nombre.toLowerCase().includes(nombreBuscado)) return false;
        return true;
    });

    renderizarTarjetas(filtrados);
}

function renderizarTarjetas(animales) {
    const contenedor = document.getElementById("rejilla-animales");
    document.getElementById("contador-resultados").textContent =
        `${animales.length} animal${animales.length === 1 ? "" : "es"} disponible${animales.length === 1 ? "" : "s"}`;

    if (animales.length === 0) {
        contenedor.innerHTML = `<p class="mensaje-vacio">No encontramos animales con esos filtros. Intenta ajustar la busqueda.</p>`;
        return;
    }

    contenedor.innerHTML = animales.map((animal) => `
        <a class="tarjeta-animal" href="animal.html?id=${animal.animalId}">
            <img class="foto" src="${animal.urlDeImagen || 'img/sin-foto.svg'}" alt="Foto de ${animal.nombre}"
                 onerror="this.src='img/sin-foto.svg'">
            <div class="info">
                <span class="etiqueta ${claseEtiquetaEstado(animal.estadoDeAdopcion)}">${animal.estadoDeAdopcion || "Disponible"}</span>
                <h3>${animal.nombre}</h3>
                <div class="meta">${animal.especieNombre} · ${animal.raza || "Raza mixta"}</div>
                <div class="meta">${calcularEdadTexto(animal.fechaDeNacimiento)} · ${animal.tamano || "?"} · ${animal.sexo || "?"}</div>
            </div>
        </a>
    `).join("");
}
