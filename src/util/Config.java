package util;

import java.io.File;

public final class Config {

    private static final String DIRECTORIO_DATOS = "data";
    private static final String DIRECTORIO_PORTADAS = "data" + File.separator + "portadas";

    public static final String ARCHIVO_ARTISTAS = DIRECTORIO_DATOS + File.separator + "artistas.json";
    public static final String ARCHIVO_ALBUMS = DIRECTORIO_DATOS + File.separator + "albums.json";
    public static final String ARCHIVO_CANCIONES = DIRECTORIO_DATOS + File.separator + "canciones.json";
    public static final String RUTA_LOGO = DIRECTORIO_PORTADAS + File.separator + "Logo.png";

    private Config() {
    }

    public static String getRutaPortadas() {
        return DIRECTORIO_PORTADAS;
    }

    public static void inicializarDirectorios() {
        File directorioDatos = new File(DIRECTORIO_DATOS);
        if (!directorioDatos.exists()) {
            directorioDatos.mkdirs();
        }
        File directorioPortadas = new File(DIRECTORIO_PORTADAS);
        if (!directorioPortadas.exists()) {
            directorioPortadas.mkdirs();
        }
    }
}