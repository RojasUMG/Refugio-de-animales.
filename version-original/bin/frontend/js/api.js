// Funciones compartidas para hablar con la API del backend (Javalin en localhost:7070).
// Como el frontend se sirve desde el mismo servidor Javalin, las rutas son relativas ("/api/...").

const API_BASE = "/api";

async function apiGet(ruta) {
    const respuesta = await fetch(API_BASE + ruta);
    if (!respuesta.ok) {
        const cuerpo = await respuesta.json().catch(() => ({}));
        throw new Error(cuerpo.error || `Error ${respuesta.status} al consultar ${ruta}`);
    }
    return respuesta.json();
}

async function apiEnviar(metodo, ruta, datos) {
    const respuesta = await fetch(API_BASE + ruta, {
        method: metodo,
        headers: { "Content-Type": "application/json" },
        body: datos !== undefined ? JSON.stringify(datos) : undefined,
    });
    if (!respuesta.ok) {
        const cuerpo = await respuesta.json().catch(() => ({}));
        throw new Error(cuerpo.error || `Error ${respuesta.status} al llamar ${ruta}`);
    }
    if (respuesta.status === 204) {
        return null;
    }
    return respuesta.json();
}

function calcularEdadTexto(fechaDeNacimientoIso) {
    if (!fechaDeNacimientoIso) {
        return "Edad desconocida";
    }
    const nacimiento = new Date(fechaDeNacimientoIso);
    const ahora = new Date();
    let meses = (ahora.getFullYear() - nacimiento.getFullYear()) * 12 + (ahora.getMonth() - nacimiento.getMonth());
    if (meses < 12) {
        return meses <= 1 ? "1 mes" : `${meses} meses`;
    }
    const anios = Math.floor(meses / 12);
    return anios === 1 ? "1 año" : `${anios} años`;
}

function claseEtiquetaEstado(estado) {
    if (!estado) return "proceso";
    const texto = estado.toLowerCase();
    if (texto.includes("adopt")) return "adoptado";
    if (texto.includes("proceso")) return "proceso";
    return "disponible";
}
