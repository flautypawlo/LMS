package util;

import java.io.File;

public final class Config {

    private static final String DIRECTORIO_DATOS = "data";
    private static final String DIRECTORIO_PORTADAS = "data/portadas";

    public static final String ARCHIVO_ARTISTAS = normalizarRuta(DIRECTORIO_DATOS + "/artistas.json");
    public static final String ARCHIVO_ALBUMS = normalizarRuta(DIRECTORIO_DATOS + "/albums.json");
    public static final String ARCHIVO_CANCIONES = normalizarRuta(DIRECTORIO_DATOS + "/canciones.json");
    public static final String RUTA_LOGO = normalizarRuta(DIRECTORIO_PORTADAS + "/Logo.png");

    private Config() {
    }

    public static String getRutaPortadas() {
        return normalizarRuta(DIRECTORIO_PORTADAS);
    }

    public static void inicializarDirectorios() {
        File directorioDatos = new File(normalizarRuta(DIRECTORIO_DATOS));
        if (!directorioDatos.exists()) {
            directorioDatos.mkdirs();
        }
        File directorioPortadas = new File(normalizarRuta(DIRECTORIO_PORTADAS));
        if (!directorioPortadas.exists()) {
            directorioPortadas.mkdirs();
        }
    }

    /**
     * Convierte cualquier ruta (con separadores "/" o "\", mezclados o no) a la
     * forma nativa del sistema operativo actual. Se aplica tanto a las rutas
     * propias de Config como a cualquier ruta leída del JSON, para que nunca
     * quede un solo punto del código usando "/" o "\" a mano sin pasar por acá.
     */
    public static String normalizarRuta(String ruta) {
        if (ruta == null) {
            return null;
        }
        String rutaConBarras = ruta.replace('\\', '/');
        return rutaConBarras.replace('/', File.separatorChar);
    }
}