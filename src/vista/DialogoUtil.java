package vista;

import java.awt.Component;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public final class DialogoUtil {

    private DialogoUtil() {
    }

    public static int mostrarFormulario(Component padre, JPanel panelFormulario, String titulo) {
        JOptionPane opcionPane = new JOptionPane(panelFormulario, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialogo = opcionPane.createDialog(padre, titulo);
        dialogo.setResizable(true);
        dialogo.setVisible(true);

        Object valorSeleccionado = opcionPane.getValue();
        if (valorSeleccionado instanceof Integer) {
            return (Integer) valorSeleccionado;
        }
        return JOptionPane.CLOSED_OPTION;
    }
}