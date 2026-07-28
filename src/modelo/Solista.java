package modelo;

import java.time.LocalDate;

public class Solista extends Artista {

    private LocalDate fechaNacimiento;
    private LocalDate fechaFallecimiento;
    private String paisNacimiento;

    public Solista(int id, String nombre, LocalDate fechaNacimiento, LocalDate fechaFallecimiento, String paisNacimiento) {
        super(id, nombre);
        setFechaNacimiento(fechaNacimiento);
        setFechaFallecimiento(fechaFallecimiento);
        setPaisNacimiento(paisNacimiento);
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (fechaNacimiento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDate getFechaFallecimiento() {
        return fechaFallecimiento;
    }

    public void setFechaFallecimiento(LocalDate fechaFallecimiento) {
        if (fechaFallecimiento != null) {
            if (this.fechaNacimiento != null && fechaFallecimiento.isBefore(this.fechaNacimiento)) {
                throw new IllegalArgumentException("La fecha de fallecimiento no puede ser anterior a la fecha de nacimiento.");
            }
            if (fechaFallecimiento.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de fallecimiento no puede ser futura.");
            }
        }
        this.fechaFallecimiento = fechaFallecimiento;
    }

    public String getPaisNacimiento() {
        return paisNacimiento;
    }

    public void setPaisNacimiento(String paisNacimiento) {
        if (paisNacimiento == null || paisNacimiento.trim().isEmpty()) {
            throw new IllegalArgumentException("El país de nacimiento no puede estar vacío.");
        }
        this.paisNacimiento = paisNacimiento.trim();
    }

    public boolean estaFallecido() {
        return fechaFallecimiento != null;
    }

    @Override
    public String getTipo() {
        return "Solista";
    }
}