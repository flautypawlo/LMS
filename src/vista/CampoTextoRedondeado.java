package vista;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

public class CampoTextoRedondeado extends JTextField {

    private static final int RADIO_ARCO = 18;
    private final String textoPlaceholder;

    public CampoTextoRedondeado(String textoPlaceholder) {
        this.textoPlaceholder = textoPlaceholder;
        setOpaque(false);
        setBackground(TemaVisual.FONDO_TARJETA);
        setForeground(TemaVisual.TEXTO_CLARO);
        setCaretColor(TemaVisual.TEXTO_CLARO);
        setFont(new Font("Serif", Font.BOLD, 15));
        setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        Graphics2D graficos2D = (Graphics2D) graficos.create();
        graficos2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graficos2D.setColor(getBackground());
        graficos2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO_ARCO, RADIO_ARCO);
        graficos2D.setStroke(new BasicStroke(1.2f));
        graficos2D.setColor(TemaVisual.BORDE_ACENTO);
        graficos2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO_ARCO, RADIO_ARCO);
        graficos2D.dispose();

        super.paintComponent(graficos);

        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D graficosPlaceholder = (Graphics2D) graficos.create();
            graficosPlaceholder.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color colorTexto = getForeground();
            graficosPlaceholder.setColor(new Color(colorTexto.getRed(), colorTexto.getGreen(), colorTexto.getBlue(), 130));
            graficosPlaceholder.setFont(getFont());
            int x = getInsets().left;
            int y = getHeight() / 2 + graficosPlaceholder.getFontMetrics().getAscent() / 2 - 2;
            graficosPlaceholder.drawString(textoPlaceholder, x, y);
            graficosPlaceholder.dispose();
        }
    }
}