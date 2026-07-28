package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ConcurrentFileManager {

    private static final Map<String, ReentrantReadWriteLock> bloqueos = new ConcurrentHashMap<>();

    private ConcurrentFileManager() {
    }

    private static ReentrantReadWriteLock obtenerBloqueo(String rutaArchivo) {
        Path rutaAbsoluta = Paths.get(rutaArchivo).toAbsolutePath().normalize();
        return bloqueos.computeIfAbsent(rutaAbsoluta.toString(), clave -> new ReentrantReadWriteLock());
    }

    public static String leerArchivo(String rutaArchivo) throws IOException {
        ReentrantReadWriteLock bloqueo = obtenerBloqueo(rutaArchivo);
        bloqueo.readLock().lock();
        try {
            Path ruta = Paths.get(rutaArchivo);
            if (!Files.exists(ruta)) {
                return "";
            }
            return new String(Files.readAllBytes(ruta), StandardCharsets.UTF_8);
        } finally {
            bloqueo.readLock().unlock();
        }
    }

    public static void escribirArchivo(String rutaArchivo, String contenido) throws IOException {
        ReentrantReadWriteLock bloqueo = obtenerBloqueo(rutaArchivo);
        bloqueo.writeLock().lock();
        try {
            Path ruta = Paths.get(rutaArchivo);
            Path directorioPadre = ruta.toAbsolutePath().getParent();
            if (directorioPadre != null && !Files.exists(directorioPadre)) {
                Files.createDirectories(directorioPadre);
            }
            Path archivoTemporal = Paths.get(rutaArchivo + ".tmp");
            Files.write(archivoTemporal, contenido.getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(archivoTemporal, ruta, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException excepcion) {
                Files.move(archivoTemporal, ruta, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            bloqueo.writeLock().unlock();
        }
    }
}