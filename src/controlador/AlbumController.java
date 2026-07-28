package controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import modelo.Album;
import modelo.Artista;
import modelo.Cancion;
import persistencia.ArchivoAlbum;
import persistencia.ArchivoCancion;
import util.GeneradorID;

public class AlbumController {

    private final ArchivoAlbum archivoAlbum;
    private final ArchivoCancion archivoCancion;
    private final List<Album> albumes;

    public AlbumController(List<Artista> artistasDisponibles) throws IOException {
        this.archivoAlbum = new ArchivoAlbum();
        this.archivoCancion = new ArchivoCancion();
        this.albumes = new ArrayList<>(archivoAlbum.cargarTodos(artistasDisponibles));
        Map<Integer, List<Cancion>> cancionesPorAlbum = archivoCancion.cargarTodos();
        for (Album album : albumes) {
            List<Cancion> canciones = cancionesPorAlbum.get(album.getId());
            if (canciones != null) {
                for (Cancion cancion : canciones) {
                    album.agregarCancion(cancion);
                }
            }
        }
    }

    public synchronized List<Album> obtenerTodos() {
        return new ArrayList<>(albumes);
    }

    public synchronized List<Album> obtenerPorArtista(int artistaId) {
        List<Album> resultado = new ArrayList<>();
        for (Album album : albumes) {
            if (album.getArtista().getId() == artistaId) {
                resultado.add(album);
            }
        }
        return resultado;
    }

    public synchronized Album buscarPorId(int id) {
        for (Album album : albumes) {
            if (album.getId() == id) {
                return album;
            }
        }
        return null;
    }

    public synchronized List<Album> buscarPorNombre(String texto) {
        List<Album> resultado = new ArrayList<>();
        if (texto == null) {
            return resultado;
        }
        String textoBusqueda = texto.trim().toLowerCase();
        for (Album album : albumes) {
            if (album.getNombre().toLowerCase().contains(textoBusqueda)) {
                resultado.add(album);
            }
        }
        return resultado;
    }

    public List<Album> ordenarPorNombre(List<Album> lista) {
        List<Album> copia = new ArrayList<>(lista);
        copia.sort(Comparator.comparing(album -> album.getNombre().toLowerCase()));
        return copia;
    }

    public List<Album> ordenarPorAnio(List<Album> lista) {
        List<Album> copia = new ArrayList<>(lista);
        copia.sort(Comparator.comparingInt(Album::getAnioLanzamiento));
        return copia;
    }

    public List<Album> ordenarPorNotaPromedio(List<Album> lista) {
        List<Album> copia = new ArrayList<>(lista);
        copia.sort(Comparator.comparingDouble(Album::getNotaPromedio).reversed());
        return copia;
    }

    public synchronized void agregar(Album album) throws IOException {
        validarNoDuplicado(album.getNombre(), album.getArtista().getId(), -1);
        albumes.add(album);
        guardarTodo();
    }

    public synchronized void modificar(Album album) throws IOException {
        Album existente = buscarPorId(album.getId());
        if (existente == null) {
            throw new IllegalArgumentException("El álbum no existe.");
        }
        validarNoDuplicado(album.getNombre(), album.getArtista().getId(), album.getId());
        int indice = albumes.indexOf(existente);
        albumes.set(indice, album);
        guardarTodo();
    }

    public synchronized void eliminar(int id) throws IOException {
        Album album = buscarPorId(id);
        if (album == null) {
            throw new IllegalArgumentException("El álbum no existe.");
        }
        albumes.remove(album);
        guardarTodo();
    }

    public synchronized void agregarCancion(int albumId, Cancion cancion) throws IOException {
        Album album = buscarPorId(albumId);
        if (album == null) {
            throw new IllegalArgumentException("El álbum no existe.");
        }
        album.agregarCancion(cancion);
        guardarTodo();
    }

    public synchronized void modificarCancion(int albumId, Cancion cancionModificada) throws IOException {
        Album album = buscarPorId(albumId);
        if (album == null) {
            throw new IllegalArgumentException("El álbum no existe.");
        }
        for (Cancion cancion : album.getCanciones()) {
            if (cancion.getId() != cancionModificada.getId()
                    && cancion.getNombre().equalsIgnoreCase(cancionModificada.getNombre())) {
                throw new IllegalArgumentException("Ya existe una canción con ese nombre en el álbum.");
            }
        }
        Cancion cancionExistente = buscarCancion(album, cancionModificada.getId());
        if (cancionExistente == null) {
            throw new IllegalArgumentException("La canción no existe en este álbum.");
        }
        cancionExistente.setNombre(cancionModificada.getNombre());
        cancionExistente.setDuracionSegundos(cancionModificada.getDuracionSegundos());
        guardarTodo();
    }

    public synchronized void eliminarCancion(int albumId, int cancionId) throws IOException {
        Album album = buscarPorId(albumId);
        if (album == null) {
            throw new IllegalArgumentException("El álbum no existe.");
        }
        album.eliminarCancion(cancionId);
        guardarTodo();
    }

    public synchronized void calificarCancion(int albumId, int cancionId, double nota) throws IOException {
        Album album = buscarPorId(albumId);
        if (album == null) {
            throw new IllegalArgumentException("El álbum no existe.");
        }
        Cancion cancion = buscarCancion(album, cancionId);
        if (cancion == null) {
            throw new IllegalArgumentException("La canción no existe en este álbum.");
        }
        cancion.setNota(nota);
        guardarTodo();
    }

    private Cancion buscarCancion(Album album, int cancionId) {
        for (Cancion cancion : album.getCanciones()) {
            if (cancion.getId() == cancionId) {
                return cancion;
            }
        }
        return null;
    }

    private void validarNoDuplicado(String nombre, int artistaId, int idAExcluir) {
        for (Album album : albumes) {
            if (album.getId() != idAExcluir
                    && album.getArtista().getId() == artistaId
                    && album.getNombre().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Ya existe un álbum con ese nombre para este artista.");
            }
        }
    }

    private void guardarTodo() throws IOException {
        archivoAlbum.guardarTodos(albumes);
        archivoCancion.guardarTodos(albumes);
    }

    public int generarNuevoIdAlbum() {
        return GeneradorID.siguienteIdAlbum();
    }

    public int generarNuevoIdCancion() {
        return GeneradorID.siguienteIdCancion();
    }
}