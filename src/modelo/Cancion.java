package modelo;

public class Cancion {

    public static final double NOTA_MINIMA = 1.0;
    public static final double NOTA_MAXIMA = 10.0;
    public static final double SIN_CALIFICAR = 0.0;

    private int id;
    private String nombre;
    private int duracionSegundos;
    private double nota;

    public Cancion(int id, String nombre, int duracionSegundos) {
        setId(id);
        setNombre(nombre);
        setDuracionSegundos(duracionSegundos);
        this.nota = SIN_CALIFICAR;
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
            throw new IllegalArgumentException("El nombre de la canción no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public int getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(int duracionSegundos) {
        if (duracionSegundos <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor a cero.");
        }
        this.duracionSegundos = duracionSegundos;
    }

    public String getDuracionFormateada() {
        int minutos = duracionSegundos / 60;
        int segundos = duracionSegundos % 60;
        return String.format("%d:%02d", minutos, segundos);
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new IllegalArgumentException("La nota debe estar entre " + NOTA_MINIMA + " y " + NOTA_MAXIMA + ".");
        }
        this.nota = Math.round(nota * 100.0) / 100.0;
    }

    public void quitarCalificacion() {
        this.nota = SIN_CALIFICAR;
    }

    public boolean estaCalificada() {
        return this.nota > 0.0;
    }

    public String getNotaTexto() {
        return estaCalificada() ? String.format("%.2f", nota) : "Sin calificar";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Cancion)) {
            return false;
        }
        Cancion otra = (Cancion) obj;
        return this.id == otra.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}