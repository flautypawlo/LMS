package persistencia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Album;
import modelo.Cancion;
import util.Config;
import util.ConcurrentFileManager;
import util.GeneradorID;
import util.JsonUtil;

public class ArchivoCancion {

    public void guardarTodos(List<Album> albumes) throws IOException {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Album album : albumes) {
            for (Cancion cancion : album.getCanciones()) {
                lista.add(cancionAMapa(cancion, album.getId()));
            }
        }
        String json = JsonUtil.listaDeMapasAJson(lista);
        ConcurrentFileManager.escribirArchivo(Config.ARCHIVO_CANCIONES, json);
    }

    public Map<Integer, List<Cancion>> cargarTodos() throws IOException {
        Map<Integer, List<Cancion>> cancionesPorAlbum = new LinkedHashMap<>();
        String json = ConcurrentFileManager.leerArchivo(Config.ARCHIVO_CANCIONES);
        List<Map<String, Object>> lista = JsonUtil.jsonAListaDeMapas(json);
        for (Map<String, Object> mapa : lista) {
            int albumId = JsonUtil.getInt(mapa, "albumId");
            Cancion cancion = mapaACancion(mapa);
            GeneradorID.registrarIdCancion(cancion.getId());
            cancionesPorAlbum.computeIfAbsent(albumId, clave -> new ArrayList<>()).add(cancion);
        }
        return cancionesPorAlbum;
    }

    private Map<String, Object> cancionAMapa(Cancion cancion, int albumId) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", cancion.getId());
        mapa.put("nombre", cancion.getNombre());
        mapa.put("duracionSegundos", cancion.getDuracionSegundos());
        mapa.put("nota", cancion.getNota());
        mapa.put("albumId", albumId);
        return mapa;
    }

    private Cancion mapaACancion(Map<String, Object> mapa) {
        int id = JsonUtil.getInt(mapa, "id");
        String nombre = JsonUtil.getString(mapa, "nombre");
        int duracionSegundos = JsonUtil.getInt(mapa, "duracionSegundos");
        double nota = JsonUtil.getDouble(mapa, "nota");
        Cancion cancion = new Cancion(id, nombre, duracionSegundos);
        if (nota > 0.0) {
            cancion.setNota(nota);
        }
        return cancion;
    }
}