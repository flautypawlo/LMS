package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import modelo.Album;
import modelo.Cancion;
import util.Config;

/**
 * Genera una imagen JPG estilo "factura/recibo", con la portada del álbum a color,
 * título, artista y año en negrita, listado de canciones con su nota alineada y la
 * nota final del álbum.
 */
public final class GeneradorImagenNota {

    private static final int ANCHO_LIENZO = 700;
    private static final int MARGEN = 32;
    private static final int TAMANIO_PORTADA = 130;
    private static final int ESPACIO_PORTADA_TITULO = 24;
    private static final int INDENTACION_CANCIONES = 18;
    private static final int ANCHO_COLUMNA_NOTA = 90;

    private static final Font FUENTE_TITULO = new Font("SansSerif", Font.BOLD, 25);
    private static final Font FUENTE_ARTISTA = new Font("SansSerif", Font.BOLD, 18);
    private static final Font FUENTE_ANIO = new Font("SansSerif", Font.BOLD, 18);
    private static final Font FUENTE_SEPARADOR = new Font("SansSerif", Font.BOLD, 14);
    private static final Font FUENTE_CANCION = new Font("SansSerif", Font.BOLD, 15);
    private static final Font FUENTE_NOTA = new Font("SansSerif", Font.BOLD, 15);
    private static final Font FUENTE_NOTA_FINAL_ETIQUETA = new Font("SansSerif", Font.BOLD, 17);
    private static final Font FUENTE_NOTA_FINAL_VALOR = new Font("SansSerif", Font.BOLD, 19);

    private GeneradorImagenNota() {
    }

    public static File generarImagen(Album album) throws IOException {
        BufferedImage portadaOriginal = cargarPortadaOriginal(album.getRutaPortada());
        BufferedImage portadaEscalada = escalarPortada(portadaOriginal, TAMANIO_PORTADA);

        String nombreArtista = album.obtenerNombreArtistaParaMostrar();
        if (nombreArtista == null) {
            nombreArtista = "Artista desconocido";
        }

        // Lienzo de 1x1 solo para medir texto antes de saber el alto final real.
        BufferedImage medidor = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D gMedicion = medidor.createGraphics();
        gMedicion.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int anchoTitulo = ANCHO_LIENZO - MARGEN - TAMANIO_PORTADA - ESPACIO_PORTADA_TITULO - MARGEN;
        gMedicion.setFont(FUENTE_TITULO);
        List<String> lineasTitulo = partirEnLineas(gMedicion, album.getNombre(), anchoTitulo);

        int anchoTextoCancion = ANCHO_LIENZO - MARGEN - INDENTACION_CANCIONES - ANCHO_COLUMNA_NOTA - MARGEN;
        gMedicion.setFont(FUENTE_CANCION);
        List<List<String>> lineasPorCancion = new ArrayList<>();
        for (Cancion cancion : album.getCanciones()) {
            String textoCancion = cancion.getNombre() + " (" + cancion.getDuracionFormateada() + ")";
            lineasPorCancion.add(partirEnLineas(gMedicion, textoCancion, anchoTextoCancion));
        }
        gMedicion.dispose();

        int altoLineaTitulo = 30;
        int altoLineaCancion = 24;

        int y = MARGEN;
        int yEncabezado = y;
        int altoTextoTitulo = lineasTitulo.size() * altoLineaTitulo;
        int altoEncabezado = Math.max(TAMANIO_PORTADA, altoTextoTitulo);
        y = yEncabezado + altoEncabezado + 18;

        int yArtistaAnio = y;
        y += 30;

        int ySeparador1 = y;
        y += 32;

        List<Integer> yBaseCancion = new ArrayList<>();
        for (List<String> lineas : lineasPorCancion) {
            yBaseCancion.add(y);
            y += Math.max(1, lineas.size()) * altoLineaCancion;
        }
        if (lineasPorCancion.isEmpty()) {
            y += altoLineaCancion;
        }

        y += 40;
        int ySeparador2 = y;
        y += 34;

        int yNotaFinal = y;
        y += 20;

        int altoLienzo = y + MARGEN;

        BufferedImage lienzo = new BufferedImage(ANCHO_LIENZO, altoLienzo, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = lienzo.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ANCHO_LIENZO, altoLienzo);
        g.setColor(Color.BLACK);

        // Portada a color con borde fino
        g.drawImage(portadaEscalada, MARGEN, yEncabezado, null);
        g.drawRect(MARGEN, yEncabezado, TAMANIO_PORTADA, TAMANIO_PORTADA);

        // Título del álbum (una o más líneas), al lado de la portada
        int xTitulo = MARGEN + TAMANIO_PORTADA + ESPACIO_PORTADA_TITULO;
        g.setFont(FUENTE_TITULO);
        int yLineaTitulo = yEncabezado + 22;
        for (String linea : lineasTitulo) {
            g.drawString(linea, xTitulo, yLineaTitulo);
            yLineaTitulo += altoLineaTitulo;
        }

        // Artista - Año (ambos en negrita)
        g.setFont(FUENTE_ARTISTA);
        g.drawString(nombreArtista, MARGEN, yArtistaAnio);
        FontMetrics metricasArtista = g.getFontMetrics();
        int xAnio = MARGEN + metricasArtista.stringWidth(nombreArtista) + 10;
        g.setFont(FUENTE_ANIO);
        g.drawString("- " + album.getAnioLanzamiento(), xAnio, yArtistaAnio);

        dibujarSeparador(g, ySeparador1);

        // Canciones, con la nota alineada a la derecha de cada una
        for (int i = 0; i < lineasPorCancion.size(); i++) {
            List<String> lineas = lineasPorCancion.get(i);
            int yBase = yBaseCancion.get(i);
            g.setFont(FUENTE_CANCION);
            int yLinea = yBase;
            for (String linea : lineas) {
                g.drawString(linea, MARGEN + INDENTACION_CANCIONES, yLinea);
                yLinea += altoLineaCancion;
            }

            Cancion cancion = album.getCanciones().get(i);
            g.setFont(FUENTE_NOTA);
            String textoNota = formatearNota(cancion.getNotaTexto());
            FontMetrics metricasNota = g.getFontMetrics();
            int xNota = ANCHO_LIENZO - MARGEN - metricasNota.stringWidth(textoNota);
            g.drawString(textoNota, xNota, yBase);
        }

        dibujarSeparador(g, ySeparador2);

        // Nota final del álbum
        g.setFont(FUENTE_NOTA_FINAL_ETIQUETA);
        g.drawString("Nota final:", MARGEN, yNotaFinal);
        g.setFont(FUENTE_NOTA_FINAL_VALOR);
        String textoNotaFinal = formatearNota(album.getNotaPromedioTexto());
        FontMetrics metricasNotaFinal = g.getFontMetrics();
        int xNotaFinal = ANCHO_LIENZO - MARGEN - metricasNotaFinal.stringWidth(textoNotaFinal);
        g.drawString(textoNotaFinal, xNotaFinal, yNotaFinal);

        g.dispose();

        Config.inicializarDirectorios();
        File carpetaExportados = new File(Config.getRutaExportados());
        if (!carpetaExportados.exists()) {
            carpetaExportados.mkdirs();
        }
        String nombreArchivo = "nota_" + limpiarNombreArchivo(album.getNombre()) + "_" + album.getId() + ".jpg";
        File archivoDestino = new File(carpetaExportados, nombreArchivo);
        if (!ImageIO.write(lienzo, "jpg", archivoDestino)) {
            throw new IOException("No se encontró un codificador JPG disponible en este sistema.");
        }
        return archivoDestino;
    }

    /**
     * Le agrega la terminación "/10.0" a la calificación obtenida.
     */
    private static String formatearNota(String notaRaw) {
        if (notaRaw == null || notaRaw.trim().isEmpty() || notaRaw.equalsIgnoreCase("Sin calificar")) {
            return "Sin calificar";
        }
        if (notaRaw.contains("/")) {
            return notaRaw;
        }
        return notaRaw + " / 10,00";
    }

    /**
     * Dibuja una línea de asteriscos que ocupa todo el ancho del margen disponible.
     */
    private static void dibujarSeparador(Graphics2D g, int y) {
        g.setFont(FUENTE_SEPARADOR);
        FontMetrics metricas = g.getFontMetrics();
        int anchoAsterisco = metricas.stringWidth("*");
        int anchoDisponible = ANCHO_LIENZO - MARGEN * 2;
        int cantidadAsteriscos = Math.max(1, anchoDisponible / anchoAsterisco);
        StringBuilder linea = new StringBuilder();
        for (int i = 0; i < cantidadAsteriscos; i++) {
            linea.append('*');
        }
        g.drawString(linea.toString(), MARGEN, y);
    }

    /**
     * Parte un texto en varias líneas para evitar superposiciones.
     */
    private static List<String> partirEnLineas(Graphics2D graficos, String texto, int anchoMaximo) {
        List<String> lineas = new ArrayList<>();
        FontMetrics metricas = graficos.getFontMetrics();
        String[] palabras = texto.split(" ");
        StringBuilder lineaActual = new StringBuilder();
        for (String palabra : palabras) {
            String candidata = lineaActual.length() == 0 ? palabra : lineaActual + " " + palabra;
            if (metricas.stringWidth(candidata) <= anchoMaximo || lineaActual.length() == 0) {
                lineaActual = new StringBuilder(candidata);
            } else {
                lineas.add(lineaActual.toString());
                lineaActual = new StringBuilder(palabra);
            }
        }
        if (lineaActual.length() > 0) {
            lineas.add(lineaActual.toString());
        }
        if (lineas.isEmpty()) {
            lineas.add("");
        }
        return lineas;
    }

    private static BufferedImage cargarPortadaOriginal(String rutaPortada) {
        if (rutaPortada == null) {
            return null;
        }
        File archivo = new File(rutaPortada);
        if (!archivo.exists()) {
            return null;
        }
        try {
            return ImageIO.read(archivo);
        } catch (IOException excepcion) {
            return null;
        }
    }

    /**
     * Escala la portada manteniendo el formato RGB original (a color).
     */
    private static BufferedImage escalarPortada(BufferedImage original, int tamanio) {
        BufferedImage escalada = new BufferedImage(tamanio, tamanio, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = escalada.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, tamanio, tamanio);
        if (original != null) {
            g.drawImage(original, 0, 0, tamanio, tamanio, null);
        } else {
            g.setColor(Color.DARK_GRAY);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString("Sin portada", tamanio / 2 - 38, tamanio / 2);
        }
        g.dispose();
        return escalada;
    }

    private static String limpiarNombreArchivo(String texto) {
        return texto.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}