package controlador;

import java.util.ArrayList;
import java.util.List;

import modelo.Album;
import modelo.Artista;
import modelo.Cancion;

public class EstadisticasController {

    private List<Artista> artistas;
    private List<Album> albumes;

    public EstadisticasController(List<Artista> artistas, List<Album> albumes) {
        actualizar(artistas, albumes);
    }

    public void actualizar(List<Artista> artistas, List<Album> albumes) {
        this.artistas = new ArrayList<>(artistas);
        this.albumes = new ArrayList<>(albumes);
    }

    public int getCantidadArtistas() {
        return artistas.size();
    }

    public int getCantidadAlbumes() {
        return albumes.size();
    }

    public int getCantidadCanciones() {
        int total = 0;
        for (Album album : albumes) {
            total += album.getCantidadCanciones();
        }
        return total;
    }

    public Cancion getMejorCancion() {
        Cancion mejor = null;
        for (Album album : albumes) {
            for (Cancion cancion : album.getCanciones()) {
                if (cancion.estaCalificada() && (mejor == null || cancion.getNota() > mejor.getNota())) {
                    mejor = cancion;
                }
            }
        }
        return mejor;
    }

    public Cancion getPeorCancion() {
        Cancion peor = null;
        for (Album album : albumes) {
            for (Cancion cancion : album.getCanciones()) {
                if (cancion.estaCalificada() && (peor == null || cancion.getNota() < peor.getNota())) {
                    peor = cancion;
                }
            }
        }
        return peor;
    }

    public Album getMejorAlbum() {
        Album mejor = null;
        for (Album album : albumes) {
            if (album.getNotaPromedio() > 0.0 && (mejor == null || album.getNotaPromedio() > mejor.getNotaPromedio())) {
                mejor = album;
            }
        }
        return mejor;
    }

    public Album getPeorAlbum() {
        Album peor = null;
        for (Album album : albumes) {
            if (album.getNotaPromedio() > 0.0 && (peor == null || album.getNotaPromedio() < peor.getNotaPromedio())) {
                peor = album;
            }
        }
        return peor;
    }

    public Artista getArtistaMejorValorado() {
        Artista mejor = null;
        double mejorPromedio = -1.0;
        for (Artista artista : artistas) {
            double promedio = calcularPromedioArtista(artista);
            if (promedio > mejorPromedio) {
                mejorPromedio = promedio;
                mejor = artista;
            }
        }
        return mejorPromedio > 0.0 ? mejor : null;
    }

    private double calcularPromedioArtista(Artista artista) {
        double suma = 0.0;
        int cantidad = 0;
        for (Album album : albumes) {
            if (album.getArtista() != null && album.getArtista().getId() == artista.getId() && album.getNotaPromedio() > 0.0) {
                suma += album.getNotaPromedio();
                cantidad++;
            }
        }
        return cantidad == 0 ? 0.0 : suma / cantidad;
    }
}