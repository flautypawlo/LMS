package util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cliente muy simple para la API pública de MusicBrainz (https://musicbrainz.org/doc/MusicBrainz_API)
 * y la Cover Art Archive (https://coverartarchive.org), usando únicamente
 * java.net.http.HttpClient (incluido en el JDK, sin librerías externas).
 *
 * MusicBrainz exige un User-Agent identificable y limita a ~1 solicitud por segundo
 * por IP a su API (ws/2); por eso esas llamadas se espacian con esperarTurno().
 * La Cover Art Archive corre en otro host con otro límite (más permisivo), así que
 * las descargas de imágenes no pasan por ese mismo limitador.
 */
public final class MusicBrainzCliente {

    private static final String USER_AGENT = "MusicRatingSystem/1.0 ( contacto@ejemplo.com )";
    private static final String URL_BUSQUEDA_RELEASE = "https://musicbrainz.org/ws/2/release/?query=%s&fmt=json&limit=50";
    private static final String URL_PORTADA = "https://coverartarchive.org/release/%s/front-500";
    private static final String URL_MINIATURA = "https://coverartarchive.org/release/%s/front-250";

    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static volatile long ultimaSolicitudMillis = 0L;

    private MusicBrainzCliente() {
    }

    public static List<ResultadoAlbum> buscarAlbumes(String textoBusqueda) throws IOException {
        esperarTurno();
        try {
            String consultaCodificada = URLEncoder.encode(textoBusqueda, StandardCharsets.UTF_8);
            String url = String.format(URL_BUSQUEDA_RELEASE, consultaCodificada);

            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> respuesta = CLIENTE.send(solicitud, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new IOException("MusicBrainz respondió con código " + respuesta.statusCode());
            }

            Map<String, Object> raiz = JsonUtil.jsonAMapa(respuesta.body());
            List<Object> releases = JsonUtil.getLista(raiz, "releases");

            List<ResultadoAlbum> resultados = new ArrayList<>();
            for (Object elemento : releases) {
                if (!(elemento instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> release = (Map<String, Object>) elemento;
                String mbid = JsonUtil.getString(release, "id");
                String titulo = JsonUtil.getString(release, "title");
                String artista = extraerArtistaCredito(release);
                String fecha = JsonUtil.getString(release, "date");
                Integer anio = extraerAnio(fecha);
                if (mbid != null && titulo != null) {
                    resultados.add(new ResultadoAlbum(mbid, titulo, artista, anio));
                }
            }
            return resultados;
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new IOException("La búsqueda fue interrumpida.", excepcion);
        }
    }

    /**
     * Intenta descargar la portada frontal (tamaño completo, 500px) desde la Cover
     * Art Archive. Devuelve null si esa release no tiene portada registrada (404),
     * en vez de lanzar error.
     */
    public static Path descargarPortada(String mbid) throws IOException {
        return descargarImagen(String.format(URL_PORTADA, mbid), "portada_" + mbid);
    }

    /**
     * Descarga una miniatura chica (250px) pensada para listas de resultados de
     * búsqueda, donde se necesitan varias imágenes rápido. No comparte el
     * limitador de MusicBrainz porque la Cover Art Archive corre en otro host
     * (coverartarchive.org / archive.org) con su propio límite, más permisivo.
     */
    public static Path descargarMiniatura(String mbid) throws IOException {
        return descargarImagen(String.format(URL_MINIATURA, mbid), "miniatura_" + mbid);
    }

    private static Path descargarImagen(String url, String prefijoArchivoTemporal) throws IOException {
        try {
            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<InputStream> respuesta = CLIENTE.send(solicitud, HttpResponse.BodyHandlers.ofInputStream());
            if (respuesta.statusCode() == 404) {
                return null;
            }
            if (respuesta.statusCode() != 200) {
                throw new IOException("Cover Art Archive respondió con código " + respuesta.statusCode());
            }

            Path archivoTemporal = Files.createTempFile(prefijoArchivoTemporal, ".jpg");
            try (InputStream entrada = respuesta.body()) {
                Files.copy(entrada, archivoTemporal, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return archivoTemporal;
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new IOException("La descarga de la imagen fue interrumpida.", excepcion);
        }
    }

    /**
     * Obtiene el listado de canciones (pistas) de una release, consultando el
     * endpoint de lookup con inc=recordings (la búsqueda por texto no trae el
     * detalle de las pistas, solo el lookup por MBID lo incluye).
     */
    public static List<PistaAlbum> obtenerPistas(String mbid) throws IOException {
        esperarTurno();
        try {
            String url = "https://musicbrainz.org/ws/2/release/" + mbid + "?inc=recordings&fmt=json";
            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> respuesta = CLIENTE.send(solicitud, HttpResponse.BodyHandlers.ofString());
            if (respuesta.statusCode() != 200) {
                throw new IOException("MusicBrainz respondió con código " + respuesta.statusCode() + " al buscar las canciones.");
            }

            Map<String, Object> release = JsonUtil.jsonAMapa(respuesta.body());
            List<Object> medios = JsonUtil.getLista(release, "media");

            List<PistaAlbum> pistas = new ArrayList<>();
            for (Object medioObj : medios) {
                if (!(medioObj instanceof Map)) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> medio = (Map<String, Object>) medioObj;
                List<Object> pistasDelMedio = JsonUtil.getLista(medio, "tracks");
                for (Object pistaObj : pistasDelMedio) {
                    if (!(pistaObj instanceof Map)) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> pista = (Map<String, Object>) pistaObj;
                    String titulo = JsonUtil.getString(pista, "title");
                    int duracionMs = JsonUtil.getInt(pista, "length");
                    int duracionSegundos = duracionMs > 0 ? Math.max(1, Math.round(duracionMs / 1000f)) : 1;
                    if (titulo != null && !titulo.trim().isEmpty()) {
                        pistas.add(new PistaAlbum(titulo.trim(), duracionSegundos));
                    }
                }
            }
            return pistas;
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new IOException("La consulta de canciones fue interrumpida.", excepcion);
        }
    }

    /**
     * El JSON de MusicBrainz no trae un campo de texto simple con el artista;
     * viene como un array "artist-credit", con el nombre de cada crédito y,
     * opcionalmente, una "joinphrase" para unir varios (ej: "A feat. B").
     * Esto reconstruye el nombre completo tal como se mostraría.
     */
    private static String extraerArtistaCredito(Map<String, Object> release) {
        List<Object> creditos = JsonUtil.getLista(release, "artist-credit");
        if (creditos.isEmpty()) {
            return "";
        }
        StringBuilder resultado = new StringBuilder();
        for (Object creditoObj : creditos) {
            if (!(creditoObj instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> credito = (Map<String, Object>) creditoObj;
            String nombre = JsonUtil.getString(credito, "name");
            if (nombre == null) {
                Object artistaObj = credito.get("artist");
                if (artistaObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> artista = (Map<String, Object>) artistaObj;
                    nombre = JsonUtil.getString(artista, "name");
                }
            }
            if (nombre != null) {
                resultado.append(nombre);
            }
            String joinphrase = JsonUtil.getString(credito, "joinphrase");
            if (joinphrase != null) {
                resultado.append(joinphrase);
            }
        }
        return resultado.toString();
    }

    private static Integer extraerAnio(String fecha) {
        if (fecha == null || fecha.length() < 4) {
            return null;
        }
        String textoAnio = fecha.substring(0, 4);
        try {
            return Integer.parseInt(textoAnio);
        } catch (NumberFormatException excepcion) {
            return null;
        }
    }

    /**
     * Respeta el límite de ~1 solicitud por segundo de MusicBrainz para evitar
     * respuestas HTTP 503 por exceso de solicitudes.
     */
    private static synchronized void esperarTurno() {
        long ahora = System.currentTimeMillis();
        long transcurrido = ahora - ultimaSolicitudMillis;
        long esperaMinimaMs = 1100L;
        if (transcurrido < esperaMinimaMs) {
            try {
                Thread.sleep(esperaMinimaMs - transcurrido);
            } catch (InterruptedException excepcion) {
                Thread.currentThread().interrupt();
            }
        }
        ultimaSolicitudMillis = System.currentTimeMillis();
    }

    public static final class ResultadoAlbum {

        private final String mbid;
        private final String titulo;
        private final String artista;
        private final Integer anio;

        public ResultadoAlbum(String mbid, String titulo, String artista, Integer anio) {
            this.mbid = mbid;
            this.titulo = titulo;
            this.artista = artista;
            this.anio = anio;
        }

        public String getMbid() {
            return mbid;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getArtista() {
            return artista;
        }

        public Integer getAnio() {
            return anio;
        }
    }

    public static final class PistaAlbum {

        private final String titulo;
        private final int duracionSegundos;

        public PistaAlbum(String titulo, int duracionSegundos) {
            this.titulo = titulo;
            this.duracionSegundos = duracionSegundos;
        }

        public String getTitulo() {
            return titulo;
        }

        public int getDuracionSegundos() {
            return duracionSegundos;
        }
    }
}