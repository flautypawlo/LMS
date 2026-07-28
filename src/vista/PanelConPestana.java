package vista;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class PanelConPestana extends JPanel {

    private static final int RADIO_TARJETA = 22;
    private static final int RADIO_PESTANA = 16;
    private static final int ALTO_PESTANA = 46;
    private static final int ANCHO_PESTANA = 140;

    private final String titulo;

    public PanelConPestana(String titulo) {
        this.titulo = titulo;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(ALTO_PESTANA + 6, 14, 16, 14));
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        Graphics2D graficos2D = (Graphics2D) graficos.create();
        graficos2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();
        int yTarjeta = ALTO_PESTANA / 2;

        RoundRectangle2D formaTarjeta = new RoundRectangle2D.Float(0, yTarjeta, ancho - 1, alto - yTarjeta - 1,
                RADIO_TARJETA, RADIO_TARJETA);
        RoundRectangle2D formaPestana = new RoundRectangle2D.Float(0, 0, ANCHO_PESTANA - 1, ALTO_PESTANA - 1,
                RADIO_PESTANA, RADIO_PESTANA);

        graficos2D.setColor(TemaVisual.FONDO_TARJETA);
        graficos2D.fill(formaTarjeta);

        graficos2D.setColor(TemaVisual.FONDO_FILA);
        graficos2D.fill(formaPestana);

        Area contorno = new Area(formaTarjeta);
        contorno.add(new Area(formaPestana));
        graficos2D.setStroke(new BasicStroke(1.5f));
        graficos2D.setColor(TemaVisual.BORDE_ACENTO);
        graficos2D.draw(contorno);

        graficos2D.setFont(new Font("Serif", Font.BOLD, 22));
        graficos2D.setColor(TemaVisual.TEXTO_CLARO);
        FontMetrics metricas = graficos2D.getFontMetrics();
        int xTexto = (ANCHO_PESTANA - metricas.stringWidth(titulo)) / 2;
        int yTexto = ALTO_PESTANA / 2 + metricas.getAscent() / 2 - 2;
        graficos2D.drawString(titulo, xTexto, yTexto);

        graficos2D.dispose();
        super.paintComponent(graficos);
    }
}