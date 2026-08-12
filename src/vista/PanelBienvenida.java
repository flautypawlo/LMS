package vista;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class PanelBienvenida extends JPanel {

    public PanelBienvenida() {
        setLayout(new BorderLayout());
        setBackground(TemaVisual.FONDO);

        JLabel etiquetaTitulo = new JLabel("Lotolovich Music Station", SwingConstants.CENTER);
        etiquetaTitulo.setFont(new Font("Serif", Font.BOLD, 42));
        etiquetaTitulo.setForeground(TemaVisual.TEXTO_CLARO);
        etiquetaTitulo.setBorder(BorderFactory.createEmptyBorder(25, 10, 25, 10));

        JTextArea areaDescripcion = new JTextArea();
        areaDescripcion.setText(
                "Bienvenido al sistema de gestión y calificación musical.\n\n"
                + "Desde aquí podés administrar solistas y bandas, organizar sus álbumes "
                + "y canciones, y calificar cada canción con una nota de 1.0 a 10.0.\n\n"
                + "Utilizá el menú superior para navegar entre Álbumes y Artistas. "
                + "En cualquier momento podés volver a esta pantalla presionando el logo.");
        areaDescripcion.setLineWrap(true);
        areaDescripcion.setWrapStyleWord(true);
        areaDescripcion.setEditable(false);
        areaDescripcion.setFocusable(false);
        areaDescripcion.setFont(new Font("Serif", Font.BOLD, 17));
        areaDescripcion.setForeground(TemaVisual.TEXTO_CLARO);
        areaDescripcion.setBackground(TemaVisual.FONDO_TARJETA);
        areaDescripcion.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        PanelRedondeado tarjeta = new PanelRedondeado(TemaVisual.FONDO_TARJETA, TemaVisual.BORDE_ACENTO, 28);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tarjeta.add(areaDescripcion, BorderLayout.CENTER);

        JPanel contenedorTarjeta = new JPanel(new BorderLayout());
        contenedorTarjeta.setBackground(TemaVisual.FONDO);
        contenedorTarjeta.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        contenedorTarjeta.add(tarjeta, BorderLayout.CENTER);

        add(etiquetaTitulo, BorderLayout.NORTH);
        add(contenedorTarjeta, BorderLayout.CENTER);
    }
}