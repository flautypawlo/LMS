package controlador;

import java.util.ArrayList;
import java.util.Comparator;
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

    /**
     * Devuelve hasta "cantidad" álbumes calificados, ordenados de mejor a peor
     * nota promedio. Los álbumes sin calificar (promedio 0.0) quedan afuera.
     */
    public List<Album> obtenerTopAlbumes(int cantidad) {
        List<Album> calificados = new ArrayList<>();
        for (Album album : albumes) {
            if (album.getNotaPromedio() > 0.0) {
                calificados.add(album);
            }
        }
        calificados.sort(Comparator.comparingDouble(Album::getNotaPromedio).reversed());
        return limitar(calificados, cantidad);
    }

    /**
     * Devuelve hasta "cantidad" artistas con al menos un álbum calificado,
     * ordenados de mejor a peor promedio, junto con ese promedio ya calculado.
     */
    public List<ArtistaConNota> obtenerTopArtistas(int cantidad) {
        List<ArtistaConNota> resultado = new ArrayList<>();
        for (Artista artista : artistas) {
            double promedio = calcularPromedioArtista(artista);
            if (promedio > 0.0) {
                resultado.add(new ArtistaConNota(artista, promedio));
            }
        }
        resultado.sort((uno, otro) -> Double.compare(otro.getNota(), uno.getNota()));
        return limitar(resultado, cantidad);
    }

    /**
     * Devuelve hasta "cantidad" canciones calificadas de cualquier álbum,
     * ordenadas de mejor a peor nota, junto con el álbum al que pertenecen
     * (para poder mostrar el artista y no solo el nombre de la canción).
     */
    public List<CancionConContexto> obtenerTopCanciones(int cantidad) {
        List<CancionConContexto> resultado = new ArrayList<>();
        for (Album album : albumes) {
            for (Cancion cancion : album.getCanciones()) {
                if (cancion.estaCalificada()) {
                    resultado.add(new CancionConContexto(cancion, album));
                }
            }
        }
        resultado.sort((uno, otro) -> Double.compare(otro.getCancion().getNota(), uno.getCancion().getNota()));
        return limitar(resultado, cantidad);
    }

    private <T> List<T> limitar(List<T> lista, int cantidad) {
        return lista.size() > cantidad ? new ArrayList<>(lista.subList(0, cantidad)) : lista;
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

    public static final class ArtistaConNota {

        private final Artista artista;
        private final double nota;

        public ArtistaConNota(Artista artista, double nota) {
            this.artista = artista;
            this.nota = nota;
        }

        public Artista getArtista() {
            return artista;
        }

        public double getNota() {
            return nota;
        }
    }

    public static final class CancionConContexto {

        private final Cancion cancion;
        private final Album album;

        public CancionConContexto(Cancion cancion, Album album) {
            this.cancion = cancion;
            this.album = album;
        }

        public Cancion getCancion() {
            return cancion;
        }

        public Album getAlbum() {
            return album;
        }
    }
}