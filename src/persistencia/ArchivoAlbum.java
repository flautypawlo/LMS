package persistencia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Album;
import modelo.Artista;
import util.Config;
import util.ConcurrentFileManager;
import util.GeneradorID;
import util.JsonUtil;

public class ArchivoAlbum {

    public void guardarTodos(List<Album> albumes) throws IOException {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Album album : albumes) {
            lista.add(albumAMapa(album));
        }
        String json = JsonUtil.listaDeMapasAJson(lista);
        ConcurrentFileManager.escribirArchivo(Config.ARCHIVO_ALBUMS, json);
    }

    public List<Album> cargarTodos(List<Artista> artistasDisponibles) throws IOException {
        List<Album> albumes = new ArrayList<>();
        String json = ConcurrentFileManager.leerArchivo(Config.ARCHIVO_ALBUMS);
        List<Map<String, Object>> lista = JsonUtil.jsonAListaDeMapas(json);
        for (Map<String, Object> mapa : lista) {
            Album album = mapaAAlbum(mapa, artistasDisponibles);
            if (album != null) {
                albumes.add(album);
                GeneradorID.registrarIdAlbum(album.getId());
            }
        }
        return albumes;
    }

    private Map<String, Object> albumAMapa(Album album) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", album.getId());
        mapa.put("nombre", album.getNombre());
        mapa.put("artistaId", album.getArtista().getId());
        mapa.put("anioLanzamiento", album.getAnioLanzamiento());
        mapa.put("rutaPortada", album.getRutaPortada());
        return mapa;
    }

    private Album mapaAAlbum(Map<String, Object> mapa, List<Artista> artistasDisponibles) {
        int id = JsonUtil.getInt(mapa, "id");
        String nombre = JsonUtil.getString(mapa, "nombre");
        int artistaId = JsonUtil.getInt(mapa, "artistaId");
        int anioLanzamiento = JsonUtil.getInt(mapa, "anioLanzamiento");
        String rutaPortada = JsonUtil.getString(mapa, "rutaPortada");
        Artista artista = buscarArtistaPorId(artistasDisponibles, artistaId);
        if (artista == null) {
            return null;
        }
        return new Album(id, nombre, artista, anioLanzamiento, rutaPortada);
    }

    private Artista buscarArtistaPorId(List<Artista> artistas, int id) {
        for (Artista artista : artistas) {
            if (artista.getId() == id) {
                return artista;
            }
        }
        return null;
    }
}