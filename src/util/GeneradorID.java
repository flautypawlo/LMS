package util;

import java.util.concurrent.atomic.AtomicInteger;

public final class GeneradorID {

    private static final AtomicInteger contadorArtista = new AtomicInteger(0);
    private static final AtomicInteger contadorAlbum = new AtomicInteger(0);
    private static final AtomicInteger contadorCancion = new AtomicInteger(0);

    private GeneradorID() {
    }

    public static int siguienteIdArtista() {
        return contadorArtista.incrementAndGet();
    }

    public static int siguienteIdAlbum() {
        return contadorAlbum.incrementAndGet();
    }

    public static int siguienteIdCancion() {
        return contadorCancion.incrementAndGet();
    }

    public static void registrarIdArtista(int id) {
        actualizarSiEsMayor(contadorArtista, id);
    }

    public static void registrarIdAlbum(int id) {
        actualizarSiEsMayor(contadorAlbum, id);
    }

    public static void registrarIdCancion(int id) {
        actualizarSiEsMayor(contadorCancion, id);
    }

    private static void actualizarSiEsMayor(AtomicInteger contador, int id) {
        int actual;
        do {
            actual = contador.get();
            if (id <= actual) {
                return;
            }
        } while (!contador.compareAndSet(actual, id));
    }
}