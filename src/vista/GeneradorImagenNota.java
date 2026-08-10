package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import modelo.Album;
import modelo.Cancion;
import util.Config;

/**
 * Genera una imagen JPG en blanco y negro con la portada del álbum (convertida
 * a grises) y el detalle de sus canciones con sus notas, para "imprimir" en
 * una imagen que se pueda compartir o guardar aparte de la app.
 */
public final class GeneradorImagenNota {

    private static final int ANCHO_LIENZO = 1120; // Ampliado para evitar solapamientos con textos largos
    private static final int MARGEN = 40;
    private static final int TAMANIO_PORTADA = 380;
    private static final int ALTO_FILA_CANCION = 42;
    private static final int ESPACIO_ENTRE_PORTADA_Y_TEXTO = 40;

    private GeneradorImagenNota() {
    }

    public static File generarImagen(Album album) throws IOException {
        BufferedImage portadaOriginal = cargarPortadaOriginal(album.getRutaPortada());
        BufferedImage portadaGris = convertirAGrises(portadaOriginal, TAMANIO_PORTADA);

        String nombreArtista = album.obtenerNombreArtistaParaMostrar();
        if (nombreArtista == null) {
            nombreArtista = "Artista desconocido";
        }
        String titulo = album.getNombre() + " - " + nombreArtista;
        String subtitulo = album.getAnioLanzamiento() + " - Cantidad de canciones: " + album.getCantidadCanciones();

        int columnaDerechaX = MARGEN + TAMANIO_PORTADA + ESPACIO_ENTRE_PORTADA_Y_TEXTO;
        int yPortada = 90;

        int altoContenidoDerecha = 90 + (album.getCanciones().size() * ALTO_FILA_CANCION) + 30;
        int altoLienzo = Math.max(yPortada + TAMANIO_PORTADA + MARGEN, altoContenidoDerecha + MARGEN);

        BufferedImage lienzo = new BufferedImage(ANCHO_LIENZO, altoLienzo, BufferedImage.TYPE_INT_RGB);
        Graphics2D graficos = lienzo.createGraphics();
        graficos.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graficos.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graficos.setColor(Color.WHITE);
        graficos.fillRect(0, 0, ANCHO_LIENZO, altoLienzo);

        graficos.setColor(Color.BLACK);
        graficos.setFont(new Font("Serif", Font.ITALIC, 34));
        graficos.drawString("LMS", MARGEN, MARGEN);

        graficos.drawImage(portadaGris, MARGEN, yPortada, null);
        graficos.setStroke(new BasicStroke(2f));
        graficos.drawRect(MARGEN, yPortada, TAMANIO_PORTADA, TAMANIO_PORTADA);

        // Título del álbum
        Font fuenteTitulo = new Font("Serif", Font.BOLD, 26);
        Font fuenteNotaAlbum = new Font("Serif", Font.BOLD, 16);
        int anchoNotaAlbum = calcularAnchoTotalNota(graficos, "Nota:", album.getNotaPromedioTexto(), fuenteNotaAlbum);
        int espacioDispTitulo = (ANCHO_LIENZO - MARGEN) - columnaDerechaX - anchoNotaAlbum - 20;

        graficos.setFont(fuenteTitulo);
        graficos.drawString(recortarTexto(graficos, titulo, espacioDispTitulo), columnaDerechaX, yPortada);

        graficos.setFont(new Font("Serif", Font.BOLD, 14));
        graficos.drawString(subtitulo, columnaDerechaX, yPortada + 25);
        dibujarNotaConCaja(graficos, "Nota:", album.getNotaPromedioTexto(), fuenteNotaAlbum, yPortada, 26);

        int yFila = yPortada + 65;
        Font fuenteCancion = new Font("Serif", Font.BOLD, 15);
        Font fuenteNota = new Font("Serif", Font.BOLD, 14);

        for (Cancion cancion : album.getCanciones()) {
            String textoNota = cancion.getNotaTexto();
            int anchoNotaCancion = calcularAnchoTotalNota(graficos, "Nota:", textoNota, fuenteNota);
            int espacioDispCancion = (ANCHO_LIENZO - MARGEN) - columnaDerechaX - anchoNotaCancion - 20;

            graficos.setFont(fuenteCancion);
            graficos.setColor(Color.BLACK);
            String textoCancion = cancion.getNombre() + " - " + cancion.getDuracionFormateada();
            String textoCancionAjustado = recortarTexto(graficos, textoCancion, espacioDispCancion);
            
            graficos.drawString(textoCancionAjustado, columnaDerechaX, yFila);

            dibujarNotaConCaja(graficos, "Nota:", textoNota, fuenteNota, yFila - 14, 24);

            yFila += ALTO_FILA_CANCION;
        }

        graficos.dispose();

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
     * Dibuja "Etiqueta: [valor]" con el valor dentro de una caja fina, alineado
     * al margen derecho del lienzo.
     */
    private static void dibujarNotaConCaja(Graphics2D graficos, String etiqueta, String valor, Font fuente,
            int yBase, int altoCaja) {
        graficos.setFont(fuente);
        graficos.setColor(Color.BLACK);
        FontMetrics metricas = graficos.getFontMetrics();

        int anchoValor = metricas.stringWidth(valor);
        int paddingCaja = 10;
        int anchoCaja = anchoValor + paddingCaja * 2;
        int xCaja = ANCHO_LIENZO - MARGEN - anchoCaja;
        int yCaja = yBase - altoCaja + 6;

        String textoEtiqueta = etiqueta + " ";
        int anchoEtiqueta = metricas.stringWidth(textoEtiqueta);
        int xEtiqueta = xCaja - anchoEtiqueta;

        graficos.drawString(textoEtiqueta, xEtiqueta, yBase);
        graficos.drawRect(xCaja, yCaja, anchoCaja, altoCaja);
        graficos.drawString(valor, xCaja + paddingCaja, yBase);
    }

    /**
     * Calcula el ancho total que ocupa el texto de la etiqueta más la caja de la nota.
     */
    private static int calcularAnchoTotalNota(Graphics2D graficos, String etiqueta, String valor, Font fuente) {
        graficos.setFont(fuente);
        FontMetrics metricas = graficos.getFontMetrics();
        int anchoValor = metricas.stringWidth(valor);
        int paddingCaja = 10;
        int anchoCaja = anchoValor + paddingCaja * 2;
        int anchoEtiqueta = metricas.stringWidth(etiqueta + " ");
        return anchoCaja + anchoEtiqueta;
    }

    /**
     * Recorta una cadena de texto y le añade "..." si supera el ancho máximo disponible.
     */
    private static String recortarTexto(Graphics2D graficos, String texto, int anchoMaximo) {
        FontMetrics metricas = graficos.getFontMetrics();
        if (metricas.stringWidth(texto) <= anchoMaximo) {
            return texto;
        }

        String elipsis = "...";
        int anchoElipsis = metricas.stringWidth(elipsis);
        String recortado = texto;

        while (recortado.length() > 0 && (metricas.stringWidth(recortado) + anchoElipsis) > anchoMaximo) {
            recortado = recortado.substring(0, recortado.length() - 1);
        }

        return recortado + elipsis;
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

    private static BufferedImage convertirAGrises(BufferedImage original, int tamanio) {
        BufferedImage escalada = new BufferedImage(tamanio, tamanio, BufferedImage.TYPE_INT_RGB);
        Graphics2D graficosEscalado = escalada.createGraphics();
        graficosEscalado.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graficosEscalado.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graficosEscalado.setColor(Color.LIGHT_GRAY);
        graficosEscalado.fillRect(0, 0, tamanio, tamanio);
        if (original != null) {
            graficosEscalado.drawImage(original, 0, 0, tamanio, tamanio, null);
        } else {
            graficosEscalado.setColor(Color.DARK_GRAY);
            graficosEscalado.setFont(new Font("SansSerif", Font.BOLD, 16));
            graficosEscalado.drawString("Sin portada", tamanio / 2 - 45, tamanio / 2);
        }
        graficosEscalado.dispose();

        BufferedImage gris = new BufferedImage(tamanio, tamanio, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graficosGris = gris.createGraphics();
        graficosGris.drawImage(escalada, 0, 0, null);
        graficosGris.dispose();
        return gris;
    }

    private static String limpiarNombreArchivo(String texto) {
        return texto.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}