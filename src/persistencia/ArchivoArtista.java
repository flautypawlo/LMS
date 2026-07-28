package persistencia;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.Artista;
import modelo.Banda;
import modelo.Solista;
import util.Config;
import util.ConcurrentFileManager;
import util.GeneradorID;
import util.JsonUtil;

public class ArchivoArtista {

    public void guardarTodos(List<Artista> artistas) throws IOException {
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Artista artista : artistas) {
            lista.add(artistaAMapa(artista));
        }
        String json = JsonUtil.listaDeMapasAJson(lista);
        ConcurrentFileManager.escribirArchivo(Config.ARCHIVO_ARTISTAS, json);
    }

    public List<Artista> cargarTodos() throws IOException {
        List<Artista> artistas = new ArrayList<>();
        String json = ConcurrentFileManager.leerArchivo(Config.ARCHIVO_ARTISTAS);
        List<Map<String, Object>> lista = JsonUtil.jsonAListaDeMapas(json);
        for (Map<String, Object> mapa : lista) {
            Artista artista = mapaAArtista(mapa);
            artistas.add(artista);
            GeneradorID.registrarIdArtista(artista.getId());
        }
        return artistas;
    }

    private Map<String, Object> artistaAMapa(Artista artista) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", artista.getId());
        mapa.put("nombre", artista.getNombre());
        mapa.put("tipo", artista.getTipo());
        if (artista instanceof Solista) {
            Solista solista = (Solista) artista;
            mapa.put("fechaNacimiento", solista.getFechaNacimiento().toString());
            mapa.put("fechaFallecimiento",
                    solista.getFechaFallecimiento() == null ? null : solista.getFechaFallecimiento().toString());
            mapa.put("paisNacimiento", solista.getPaisNacimiento());
        } else if (artista instanceof Banda) {
            Banda banda = (Banda) artista;
            mapa.put("integrantes", new ArrayList<Object>(banda.getIntegrantes()));
        }
        return mapa;
    }

    private Artista mapaAArtista(Map<String, Object> mapa) {
        int id = JsonUtil.getInt(mapa, "id");
        String nombre = JsonUtil.getString(mapa, "nombre");
        String tipo = JsonUtil.getString(mapa, "tipo");

        if ("Banda".equals(tipo)) {
            List<Object> integrantesObj = JsonUtil.getLista(mapa, "integrantes");
            List<String> integrantes = new ArrayList<>();
            for (Object elemento : integrantesObj) {
                integrantes.add(String.valueOf(elemento));
            }
            return new Banda(id, nombre, integrantes);
        }

        String fechaNacimientoTexto = JsonUtil.getString(mapa, "fechaNacimiento");
        String fechaFallecimientoTexto = JsonUtil.getString(mapa, "fechaFallecimiento");
        String paisNacimiento = JsonUtil.getString(mapa, "paisNacimiento");
        LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoTexto);
        LocalDate fechaFallecimiento = fechaFallecimientoTexto == null ? null : LocalDate.parse(fechaFallecimientoTexto);
        return new Solista(id, nombre, fechaNacimiento, fechaFallecimiento, paisNacimiento);
    }
}