package controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import modelo.Album;
import modelo.Artista;
import persistencia.ArchivoArtista;
import util.GeneradorID;

public class ArtistaController {

    private final ArchivoArtista archivoArtista;
    private final List<Artista> artistas;

    public ArtistaController() throws IOException {
        this.archivoArtista = new ArchivoArtista();
        this.artistas = new ArrayList<>(archivoArtista.cargarTodos());
    }

    public synchronized List<Artista> obtenerTodos() {
        return new ArrayList<>(artistas);
    }

    public synchronized List<Artista> obtenerSolistas() {
        List<Artista> resultado = new ArrayList<>();
        for (Artista artista : artistas) {
            if ("Solista".equals(artista.getTipo())) {
                resultado.add(artista);
            }
        }
        return resultado;
    }

    public synchronized List<Artista> obtenerBandas() {
        List<Artista> resultado = new ArrayList<>();
        for (Artista artista : artistas) {
            if ("Banda".equals(artista.getTipo())) {
                resultado.add(artista);
            }
        }
        return resultado;
    }

    public synchronized Artista buscarPorId(int id) {
        for (Artista artista : artistas) {
            if (artista.getId() == id) {
                return artista;
            }
        }
        return null;
    }

    public synchronized List<Artista> buscarPorNombre(String texto) {
        List<Artista> resultado = new ArrayList<>();
        if (texto == null) {
            return resultado;
        }
        String textoBusqueda = texto.trim().toLowerCase();
        for (Artista artista : artistas) {
            if (artista.getNombre().toLowerCase().contains(textoBusqueda)) {
                resultado.add(artista);
            }
        }
        return resultado;
    }

    public synchronized void agregar(Artista artista) throws IOException {
        validarNoDuplicado(artista.getNombre(), -1);
        artistas.add(artista);
        archivoArtista.guardarTodos(artistas);
    }

    public synchronized void modificar(Artista artista) throws IOException {
        Artista existente = buscarPorId(artista.getId());
        if (existente == null) {
            throw new IllegalArgumentException("El artista no existe.");
        }
        validarNoDuplicado(artista.getNombre(), artista.getId());
        int indice = artistas.indexOf(existente);
        artistas.set(indice, artista);
        archivoArtista.guardarTodos(artistas);
    }

    public synchronized void eliminar(int id, List<Album> albumesExistentes) throws IOException {
        Artista artista = buscarPorId(id);
        if (artista == null) {
            throw new IllegalArgumentException("El artista no existe.");
        }
        for (Album album : albumesExistentes) {
            if (album.getArtista().getId() == id) {
                throw new IllegalStateException("No se puede eliminar un artista con álbumes asociados.");
            }
        }
        artistas.remove(artista);
        archivoArtista.guardarTodos(artistas);
    }

    private void validarNoDuplicado(String nombre, int idAExcluir) {
        for (Artista artista : artistas) {
            if (artista.getId() != idAExcluir && artista.getNombre().equalsIgnoreCase(nombre)) {
                throw new IllegalArgumentException("Ya existe un artista con ese nombre.");
            }
        }
    }

    public synchronized int generarNuevoId() {
        return GeneradorID.siguienteIdArtista();
    }
}