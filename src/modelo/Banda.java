package modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Banda extends Artista {

    private List<String> integrantes;

    public Banda(int id, String nombre, List<String> integrantes) {
        super(id, nombre);
        setIntegrantes(integrantes);
    }

    public int getCantidadIntegrantes() {
        return integrantes.size();
    }

    public List<String> getIntegrantes() {
        return Collections.unmodifiableList(integrantes);
    }

    public void setIntegrantes(List<String> integrantes) {
        if (integrantes == null || integrantes.isEmpty()) {
            throw new IllegalArgumentException("La banda debe tener al menos un integrante.");
        }
        for (String integrante : integrantes) {
            if (integrante == null || integrante.trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre de un integrante no puede estar vacío.");
            }
        }
        this.integrantes = new ArrayList<>(integrantes);
    }

    public void agregarIntegrante(String nombreIntegrante) {
        if (nombreIntegrante == null || nombreIntegrante.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del integrante no puede estar vacío.");
        }
        this.integrantes.add(nombreIntegrante.trim());
    }

    public void quitarIntegrante(String nombreIntegrante) {
        this.integrantes.remove(nombreIntegrante);
    }

    @Override
    public String getTipo() {
        return "Banda";
    }
}