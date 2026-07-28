package vista;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;

public final class ImagenUtil {

    private ImagenUtil() {
    }

    /**
     * Carga una imagen escalada a (ancho x alto) "lógicos", pero incluye además
     * variantes de mayor resolución (2x y 3x) para que se vea nítida en pantallas
     * con escalado de Windows/HiDPI (125%, 150%, 200%, etc.). Sin esto, Java
     * termina estirando el bitmap ya reducido y se ve pixelado.
     */
    public static ImageIcon cargarEscalada(String rutaArchivo, int ancho, int alto) {
        if (rutaArchivo == null) {
            return null;
        }
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            return null;
        }
        Image imagenOriginal = new ImageIcon(rutaArchivo).getImage();

        BufferedImage variante1x = escalarConCalidad(imagenOriginal, ancho, alto);
        BufferedImage variante2x = escalarConCalidad(imagenOriginal, ancho * 2, alto * 2);
        BufferedImage variante3x = escalarConCalidad(imagenOriginal, ancho * 3, alto * 3);

        Image imagenMultiResolucion = new BaseMultiResolutionImage(
                new BufferedImage[] { variante1x, variante2x, variante3x });
        return new ImageIcon(imagenMultiResolucion);
    }

    private static BufferedImage escalarConCalidad(Image imagenOriginal, int ancho, int alto) {
        BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graficos = resultado.createGraphics();
        graficos.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graficos.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graficos.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graficos.drawImage(imagenOriginal, 0, 0, ancho, alto, null);
        graficos.dispose();
        return resultado;
    }
}