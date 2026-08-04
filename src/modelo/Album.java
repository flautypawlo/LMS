package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Album {

    private int id;
    private String nombre;
    private Artista artista;
    private String nombreArtistaSugerido;
    private int anioLanzamiento;
    private String rutaPortada;
    private List<Cancion> canciones;

    public Album(int id, String nombre, Artista artista, int anioLanzamiento, String rutaPortada) {
        setId(id);
        setNombre(nombre);
        setArtista(artista);
        setAnioLanzamiento(anioLanzamiento);
        setRutaPortada(rutaPortada);
        this.canciones = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID debe ser un valor positivo.");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del álbum no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public Artista getArtista() {
        return artista;
    }

    public void setArtista(Artista artista) {
        this.artista = artista;
    }

    /**
     * Nombre del artista tal como vino de una fuente externa (ej: MusicBrainz),
     * usado solo para mostrar cuando el álbum todavía no tiene un Artista real
     * asociado (artista == null). No reemplaza al Artista registrado.
     */
    public String getNombreArtistaSugerido() {
        return nombreArtistaSugerido;
    }

    public void setNombreArtistaSugerido(String nombreArtistaSugerido) {
        this.nombreArtistaSugerido = nombreArtistaSugerido == null || nombreArtistaSugerido.trim().isEmpty() ? null
                : nombreArtistaSugerido.trim();
    }

    /**
     * Nombre del artista para mostrar en pantalla: el del Artista registrado si
     * existe, o el sugerido (externo, sin registrar) si no. Puede ser null si
     * no se sabe nada del artista.
     */
    public String obtenerNombreArtistaParaMostrar() {
        if (artista != null) {
            return artista.getNombre();
        }
        return nombreArtistaSugerido;
    }

    public int getAnioLanzamiento() {
        return anioLanzamiento;
    }

    public void setAnioLanzamiento(int anioLanzamiento) {
        int anioActual = LocalDate.now().getYear();
        if (anioLanzamiento > anioActual) {
            throw new IllegalArgumentException("El año de lanzamiento no puede ser mayor al año actual.");
        }
        this.anioLanzamiento = anioLanzamiento;
    }

    public String getRutaPortada() {
        return rutaPortada;
    }

    public void setRutaPortada(String rutaPortada) {
        this.rutaPortada = rutaPortada;
    }

    public List<Cancion> getCanciones() {
        return Collections.unmodifiableList(canciones);
    }

    public void agregarCancion(Cancion cancion) {
        if (cancion == null) {
            throw new IllegalArgumentException("La canción no puede ser nula.");
        }
        for (Cancion c : canciones) {
            if (c.getNombre().equalsIgnoreCase(cancion.getNombre())) {
                throw new IllegalArgumentException("Ya existe una canción con ese nombre en el álbum.");
            }
        }
        this.canciones.add(cancion);
    }

    public void eliminarCancion(int cancionId) {
        canciones.removeIf(c -> c.getId() == cancionId);
    }

    public int getCantidadCanciones() {
        return canciones.size();
    }

    public double getNotaPromedio() {
        List<Cancion> calificadas = new ArrayList<>();
        for (Cancion c : canciones) {
            if (c.estaCalificada()) {
                calificadas.add(c);
            }
        }
        if (calificadas.isEmpty()) {
            return 0.0;
        }
        double suma = 0.0;
        for (Cancion c : calificadas) {
            suma += c.getNota();
        }
        double promedio = suma / calificadas.size();
        return Math.round(promedio * 100.0) / 100.0;
    }

    public String getNotaPromedioTexto() {
        boolean sinCalificar = true;
        for (Cancion c : canciones) {
            if (c.estaCalificada()) {
                sinCalificar = false;
                break;
            }
        }
        if (sinCalificar) {
            return "Sin calificar";
        }
        return String.format("%.2f", getNotaPromedio());
    }
}