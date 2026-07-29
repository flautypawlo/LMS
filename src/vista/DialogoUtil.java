package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public final class DialogoUtil {

    private DialogoUtil() {
    }

    public static JLabel crearEtiquetaCampo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Serif", Font.BOLD, 14));
        etiqueta.setForeground(TemaVisual.TEXTO_CLARO);
        etiqueta.setAlignmentX(Component.LEFT_ALIGNMENT);
        return etiqueta;
    }

    /**
     * Muestra un formulario con el tema oscuro de la aplicación: tarjeta redondeada,
     * título y botones "Aceptar"/"Cancelar" propios (no los de JOptionPane por defecto).
     * El panel recibido debe contener únicamente los campos del formulario.
     */
    public static int mostrarFormulario(Component padre, JPanel camposFormulario, String titulo) {
        PanelRedondeado tarjeta = new PanelRedondeado(TemaVisual.FONDO_TARJETA, TemaVisual.BORDE_ACENTO, 22);
        tarjeta.setLayout(new BorderLayout(0, 18));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel etiquetaTitulo = new JLabel(titulo, SwingConstants.CENTER);
        etiquetaTitulo.setFont(new Font("Serif", Font.BOLD, 22));
        etiquetaTitulo.setForeground(TemaVisual.TEXTO_CLARO);
        tarjeta.add(etiquetaTitulo, BorderLayout.NORTH);

        camposFormulario.setOpaque(false);
        tarjeta.add(camposFormulario, BorderLayout.CENTER);

        JOptionPane opcionPane = new JOptionPane(tarjeta, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                new Object[0]);
        opcionPane.setBorder(BorderFactory.createEmptyBorder());
        opcionPane.setOpaque(true);
        opcionPane.setBackground(TemaVisual.FONDO);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        panelBotones.setOpaque(false);
        BotonRedondeado botonAceptar = new BotonRedondeado("Aceptar", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonCancelar = new BotonRedondeado("Cancelar", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        botonAceptar.addActionListener(evento -> opcionPane.setValue(JOptionPane.OK_OPTION));
        botonCancelar.addActionListener(evento -> opcionPane.setValue(JOptionPane.CANCEL_OPTION));
        panelBotones.add(botonAceptar);
        panelBotones.add(botonCancelar);
        tarjeta.add(panelBotones, BorderLayout.SOUTH);

        JDialog dialogo = opcionPane.createDialog(padre, titulo);
        dialogo.getContentPane().setBackground(TemaVisual.FONDO);
        dialogo.setResizable(true);
        dialogo.setVisible(true);

        Object valorSeleccionado = opcionPane.getValue();
        if (valorSeleccionado instanceof Integer) {
            return (Integer) valorSeleccionado;
        }
        return JOptionPane.CLOSED_OPTION;
    }
}