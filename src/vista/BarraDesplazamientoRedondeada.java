package vista;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class BarraDesplazamientoRedondeada extends BasicScrollBarUI {

    private static final int GROSOR = 12;
    private static final int MARGEN_LATERAL = 3;

    @Override
    protected void configureScrollBarColors() {
        this.thumbColor = TemaVisual.FONDO_FILA;
        this.trackColor = TemaVisual.FONDO_BADGE;
    }

    @Override
    protected JButton createDecreaseButton(int orientacion) {
        return crearBotonInvisible();
    }

    @Override
    protected JButton createIncreaseButton(int orientacion) {
        return crearBotonInvisible();
    }

    private JButton crearBotonInvisible() {
        JButton boton = new JButton();
        boton.setPreferredSize(new Dimension(0, 0));
        boton.setMinimumSize(new Dimension(0, 0));
        boton.setMaximumSize(new Dimension(0, 0));
        return boton;
    }

    @Override
    protected void paintTrack(Graphics graficos, JComponent componente, Rectangle limites) {
        Graphics2D graficos2D = (Graphics2D) graficos.create();
        graficos2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graficos2D.setColor(TemaVisual.FONDO_BADGE);
        graficos2D.fillRoundRect(limites.x + MARGEN_LATERAL, limites.y, GROSOR, limites.height, GROSOR, GROSOR);
        graficos2D.setColor(TemaVisual.BORDE_ACENTO);
        graficos2D.drawRoundRect(limites.x + MARGEN_LATERAL, limites.y, GROSOR, limites.height, GROSOR, GROSOR);
        graficos2D.dispose();
    }

    @Override
    protected void paintThumb(Graphics graficos, JComponent componente, Rectangle limites) {
        if (limites.isEmpty() || !scrollbar.isEnabled()) {
            return;
        }
        Graphics2D graficos2D = (Graphics2D) graficos.create();
        graficos2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graficos2D.setColor(TemaVisual.FONDO_FILA);
        graficos2D.fillRoundRect(limites.x + MARGEN_LATERAL, limites.y + 2, GROSOR, limites.height - 4, GROSOR, GROSOR);
        graficos2D.setColor(TemaVisual.BORDE_ACENTO);
        graficos2D.drawRoundRect(limites.x + MARGEN_LATERAL, limites.y + 2, GROSOR, limites.height - 4, GROSOR, GROSOR);
        graficos2D.dispose();
    }

    @Override
    protected Dimension getMinimumThumbSize() {
        return new Dimension(GROSOR, GROSOR * 2);
    }
}