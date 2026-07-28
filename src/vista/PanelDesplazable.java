package vista;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.Scrollable;

public class PanelDesplazable extends JPanel implements Scrollable {

    public PanelDesplazable() {
        setOpaque(true);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle areaVisible, int orientacion, int direccion) {
        return 24;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle areaVisible, int orientacion, int direccion) {
        return 96;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}