# Huellitas — versión con fotos y tarjetas corregidas
Abre `frontend/index.html` para la página pública y `frontend/panel.html` para el personal.
Se añadieron siete fotos suministradas por la persona usuaria. Los nombres, edades y características son datos de ejemplo editables desde el panel; verifica la información antes de publicar.
Se configuró la cuenta administradora solicitada, sin mostrar sus credenciales en pantalla. **Aviso importante:** la autenticación actual se ejecuta en JavaScript y localStorage; NO es segura para producción, y los datos de adopción se almacenan solamente en el navegador. La contraseña inicial debe cambiarse al migrar a un servidor real con autenticación y base de datos.
Las imágenes de ejemplo se almacenan en `frontend/assets/mascotas/`. El panel permite cargar nuevas imágenes JPG, PNG o WebP.

## Corrección del acceso
Se corrigió el problema de cuentas guardadas de versiones anteriores y el acceso local al abrir el HTML sin HTTPS. No se borran registros de animales ni solicitudes. Esta autenticación es solo de demostración y NO debe publicarse con datos reales.

## Versión sincronizada
Copito se eliminó del catálogo inicial y se limpia de los datos guardados al abrir la aplicación. Ya no existe la opción 'Algoritmos' en el menú.
Las solicitudes de adopción se consultan desde la pestaña Adopciones y aparecen en el resumen. El panel se actualiza al cambiar de pestaña, recibir cambios de almacenamiento y periódicamente mientras está visible.
**Importante:** para compartir datos entre la página pública y el panel en un mismo equipo, sirve ambos HTML desde el MISMO origen HTTP (por ejemplo VS Code Live Server), NO abras los archivos con doble clic `file://`, ya que los navegadores pueden aislar su almacenamiento. Esto NO sincroniza dispositivos distintos: para eso se requiere un backend y base de datos.

## Cambios versión 5
- Solicitudes: el panel muestra todos los campos enviados por el visitante, como información de solo lectura, y permite cambiar estado y notas internas.
- Confirmación pública: muestra únicamente éxito y plazo estimado de contacto de 2 a 4 días hábiles.
- Animales: nuevos campos para discapacidad y pronóstico de vida; las mascotas con necesidades especiales aparecen primero cuando están disponibles.
- Imagen nueva: se usa como logo y en la portada.
- Se conservan las fotos anteriores y el panel de administración.

**Limitación:** sincronización solo entre pestañas del mismo navegador y origen. Para usuarios en distintos dispositivos se necesita backend con base de datos. Las credenciales de demostración no son seguras para producción.

Corrección: se mantiene el logotipo original de huella en la cabecera y administración. La nueva fotografía solo aparece en la imagen principal de portada.
