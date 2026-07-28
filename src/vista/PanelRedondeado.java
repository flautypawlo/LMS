package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class PanelRedondeado extends JPanel {

    private Color colorFondo;
    private Color colorBorde;
    private final int radio;

    public PanelRedondeado(Color colorFondo, Color colorBorde, int radio) {
        this.colorFondo = colorFondo;
        this.colorBorde = colorBorde;
        this.radio = radio;
        setOpaque(false);
    }

    public void setColorFondo(Color colorFondo) {
        this.colorFondo = colorFondo;
        repaint();
    }

    public void setColorBorde(Color colorBorde) {
        this.colorBorde = colorBorde;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        Graphics2D graficos2D = (Graphics2D) graficos.create();
        graficos2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graficos2D.setColor(colorFondo);
        graficos2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
        graficos2D.setStroke(new BasicStroke(1.5f));
        graficos2D.setColor(colorBorde);
        graficos2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radio, radio);
        graficos2D.dispose();
        super.paintComponent(graficos);
    }
}