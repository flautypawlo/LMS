package vista;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;

public class BotonRedondeado extends JButton {

    private static final int RADIO_ARCO = 18;

    public BotonRedondeado(String texto, Color colorFondo, Color colorTexto) {
        super(texto);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(colorTexto);
        setBackground(colorFondo);
        setFont(getFont().deriveFont(Font.PLAIN, 14f));
        setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        Graphics2D graficos2D = (Graphics2D) graficos.create();
        graficos2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graficos2D.setColor(getBackground());
        graficos2D.fillRoundRect(0, 0, getWidth(), getHeight(), RADIO_ARCO, RADIO_ARCO);
        graficos2D.dispose();
        super.paintComponent(graficos);
    }
}