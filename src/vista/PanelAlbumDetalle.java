package vista;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import modelo.Album;
import modelo.Cancion;

public class PanelAlbumDetalle extends JPanel {

    private final VentanaPrincipal ventanaPrincipal;

    private final JLabel etiquetaPortada;
    private final JLabel etiquetaTituloArtista;
    private final JLabel etiquetaAnioCantidad;
    private final JLabel etiquetaNotaAlbumValor;
    private final PanelDesplazable panelCanciones;

    private int albumIdActual;
    private Album albumActual;
    private Cancion cancionSeleccionada;

    public PanelAlbumDetalle(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;

        this.etiquetaPortada = new JLabel();
        this.etiquetaPortada.setPreferredSize(new Dimension(350, 350));
        this.etiquetaPortada.setHorizontalAlignment(SwingConstants.CENTER);
        this.etiquetaPortada.setVerticalAlignment(SwingConstants.CENTER);

        this.etiquetaTituloArtista = new JLabel();
        this.etiquetaTituloArtista.setFont(new Font("Serif", Font.BOLD, 32));
        this.etiquetaTituloArtista.setForeground(TemaVisual.TEXTO_CLARO);
        this.etiquetaTituloArtista.setVerticalAlignment(SwingConstants.TOP);

        this.etiquetaAnioCantidad = new JLabel();
        this.etiquetaAnioCantidad.setFont(new Font("Serif", Font.BOLD, 15));
        this.etiquetaAnioCantidad.setForeground(TemaVisual.TEXTO_CLARO);

        this.etiquetaNotaAlbumValor = new JLabel();
        this.etiquetaNotaAlbumValor.setFont(new Font("Serif", Font.BOLD, 15));
        this.etiquetaNotaAlbumValor.setForeground(TemaVisual.TEXTO_CLARO);

        this.panelCanciones = new PanelDesplazable();

        setLayout(new BorderLayout());
        setBackground(TemaVisual.FONDO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        PanelRedondeado tarjetaGeneral = new PanelRedondeado(TemaVisual.FONDO_TARJETA, TemaVisual.BORDE_ACENTO, 24);
        tarjetaGeneral.setLayout(new BorderLayout(25, 0));
        tarjetaGeneral.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        tarjetaGeneral.add(etiquetaPortada, BorderLayout.WEST);
        tarjetaGeneral.add(construirPanelDerecho(), BorderLayout.CENTER);

        add(tarjetaGeneral, BorderLayout.CENTER);
    }

   private JPanel construirPanelDerecho() {
        JPanel panelDerecho = new JPanel(new BorderLayout(0, 12));
        panelDerecho.setOpaque(false);

        JPanel panelEncabezado = new JPanel(new BorderLayout(0, 8)); 
        panelEncabezado.setOpaque(false);
        
        panelEncabezado.add(etiquetaTituloArtista, BorderLayout.NORTH);

        JPanel filaSubtitulo = new JPanel(new BorderLayout());
        filaSubtitulo.setOpaque(false);
        filaSubtitulo.add(etiquetaAnioCantidad, BorderLayout.WEST);

        JPanel panelNotaAlbum = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelNotaAlbum.setOpaque(false);
        JLabel etiquetaNotaAlbumTexto = new JLabel("Nota:");
        etiquetaNotaAlbumTexto.setFont(new Font("Serif", Font.BOLD, 15));
        etiquetaNotaAlbumTexto.setForeground(TemaVisual.TEXTO_CLARO);

        PanelRedondeado badgeNotaAlbum = new PanelRedondeado(TemaVisual.FONDO_BADGE, TemaVisual.BORDE_ACENTO, 12);
        badgeNotaAlbum.setLayout(new BorderLayout());
        badgeNotaAlbum.setBorder(BorderFactory.createEmptyBorder(3, 12, 3, 12));
        badgeNotaAlbum.add(etiquetaNotaAlbumValor, BorderLayout.CENTER);

        panelNotaAlbum.add(etiquetaNotaAlbumTexto);
        panelNotaAlbum.add(badgeNotaAlbum);
        filaSubtitulo.add(panelNotaAlbum, BorderLayout.EAST);

        panelEncabezado.add(filaSubtitulo, BorderLayout.CENTER);
        

        panelDerecho.add(panelEncabezado, BorderLayout.NORTH);

        PanelRedondeado tarjetaCanciones = new PanelRedondeado(TemaVisual.FONDO_FILA, TemaVisual.BORDE_ACENTO, 18);
        tarjetaCanciones.setLayout(new BorderLayout());
        tarjetaCanciones.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        panelCanciones.setBackground(TemaVisual.FONDO_FILA);

        JScrollPane scrollCanciones = new JScrollPane(panelCanciones);
        scrollCanciones.setBorder(BorderFactory.createEmptyBorder());
        scrollCanciones.setOpaque(false);
        scrollCanciones.getViewport().setOpaque(true);
        scrollCanciones.getViewport().setBackground(TemaVisual.FONDO_FILA);
        scrollCanciones.getVerticalScrollBar().setUI(new BarraDesplazamientoRedondeada());
        scrollCanciones.getVerticalScrollBar().setPreferredSize(new Dimension(16, 0));
        scrollCanciones.getVerticalScrollBar().setOpaque(false);
        scrollCanciones.getVerticalScrollBar().setUnitIncrement(16);
        tarjetaCanciones.add(scrollCanciones, BorderLayout.CENTER);

        panelDerecho.add(tarjetaCanciones, BorderLayout.CENTER);
        panelDerecho.add(construirPanelBotones(), BorderLayout.SOUTH);

        return panelDerecho;
    }

    private JPanel construirPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        panelBotones.setOpaque(false);

        BotonRedondeado botonAgregarCancion = new BotonRedondeado("+", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonModificarCancion = new BotonRedondeado("\u270E", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonEliminarCancion = new BotonRedondeado("\u2212", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);

        botonAgregarCancion.addActionListener(evento -> agregarCancion());
        botonModificarCancion.addActionListener(evento -> modificarCancion());
        botonEliminarCancion.addActionListener(evento -> eliminarCancion());

        panelBotones.add(botonAgregarCancion);
        panelBotones.add(botonModificarCancion);
        panelBotones.add(botonEliminarCancion);
        return panelBotones;
    }

    private JComponent construirFilaCancion(Cancion cancion) {
        boolean seleccionada = cancionSeleccionada != null && cancionSeleccionada.getId() == cancion.getId();

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 10));

        JPanel fila = seleccionada
                ? new PanelRedondeado(TemaVisual.FONDO_TARJETA, TemaVisual.TEXTO_CLARO, 14)
                : construirPanelPlano();
        fila.setLayout(new BorderLayout());
        fila.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 14));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel etiquetaTexto = new JLabel(cancion.getNombre() + " - " + cancion.getDuracionFormateada());
        etiquetaTexto.setFont(new Font("Serif", Font.BOLD, 15));
        etiquetaTexto.setForeground(TemaVisual.TEXTO_CLARO);
        fila.add(etiquetaTexto, BorderLayout.CENTER);

        MouseAdapter seleccionarFila = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                cancionSeleccionada = cancion;
                reconstruirListaCanciones();
            }
        };
        fila.addMouseListener(seleccionarFila);
        etiquetaTexto.addMouseListener(seleccionarFila);

        JPanel panelNota = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelNota.setOpaque(false);

        JLabel etiquetaNotaTexto = new JLabel("Nota:");
        etiquetaNotaTexto.setFont(new Font("Serif", Font.BOLD, 15));
        etiquetaNotaTexto.setForeground(TemaVisual.TEXTO_CLARO);
        etiquetaNotaTexto.addMouseListener(seleccionarFila);

        JTextField campoNota = new JTextField(cancion.estaCalificada() ? cancion.getNotaTexto() : "");
        campoNota.setHorizontalAlignment(JTextField.CENTER);
        campoNota.setFont(new Font("Serif", Font.BOLD, 15));
        campoNota.setForeground(TemaVisual.TEXTO_CLARO);
        campoNota.setBackground(TemaVisual.FONDO_BADGE);
        campoNota.setCaretColor(TemaVisual.TEXTO_CLARO);
        campoNota.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        campoNota.setColumns(3);
        campoNota.addActionListener(evento -> confirmarNota(cancion.getId(), campoNota));
        campoNota.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent evento) {
                confirmarNota(cancion.getId(), campoNota);
            }
        });

        PanelRedondeado badge = new PanelRedondeado(TemaVisual.FONDO_BADGE, TemaVisual.BORDE_ACENTO, 12);
        badge.setLayout(new BorderLayout());
        badge.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        badge.add(campoNota, BorderLayout.CENTER);

        panelNota.add(etiquetaNotaTexto);
        panelNota.add(badge);
        fila.add(panelNota, BorderLayout.EAST);

        envoltorio.add(fila, BorderLayout.CENTER);
        return envoltorio;
    }

    private JPanel construirPanelPlano() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private void confirmarNota(int cancionId, JTextField campo) {
        String texto = campo.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        try {
            double nota = Double.parseDouble(texto.replace(",", "."));
            ventanaPrincipal.getAlbumController().calificarCancion(albumIdActual, cancionId, nota);
            refrescarDatosAlbum();
        } catch (NumberFormatException excepcion) {
            mostrarError("La nota debe ser un número válido (1.0 a 10.0).");
            refrescarDatosAlbum();
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
            refrescarDatosAlbum();
        }
    }

    private void reconstruirListaCanciones() {
        panelCanciones.removeAll();
        if (albumActual != null) {
            for (Cancion cancion : albumActual.getCanciones()) {
                panelCanciones.add(construirFilaCancion(cancion));
            }
        }
        panelCanciones.revalidate();
        panelCanciones.repaint();
    }

    private void refrescarDatosAlbum() {
        this.albumActual = ventanaPrincipal.getAlbumController().buscarPorId(albumIdActual);
        if (albumActual == null) {
            return;
        }
        etiquetaNotaAlbumValor.setText(albumActual.getNotaPromedioTexto());
        reconstruirListaCanciones();
    }

    public void cargarAlbum(int albumId) {
        this.albumIdActual = albumId;
        this.albumActual = ventanaPrincipal.getAlbumController().buscarPorId(albumId);
        this.cancionSeleccionada = null;
        if (albumActual == null) {
            return;
        }
        etiquetaTituloArtista.setText(albumActual.getNombre() + " - " + albumActual.getArtista().getNombre());
        etiquetaAnioCantidad.setText(
                albumActual.getAnioLanzamiento() + " - Cantidad de canciones: " + albumActual.getCantidadCanciones());
        etiquetaNotaAlbumValor.setText(albumActual.getNotaPromedioTexto());
        etiquetaPortada.setIcon(ImagenUtil.cargarEscalada(albumActual.getRutaPortada(), 350, 350));
        reconstruirListaCanciones();
    }

    private void agregarCancion() {
        JTextField campoNombre = new JTextField();
        JTextField campoMinutos = new JTextField();
        JTextField campoSegundos = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre:"));
        panel.add(campoNombre);
        panel.add(new JLabel("Minutos:"));
        panel.add(campoMinutos);
        panel.add(new JLabel("Segundos:"));
        panel.add(campoSegundos);

        int resultado = DialogoUtil.mostrarFormulario(this, panel, "Agregar Canción");
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            int duracion = leerDuracionSegundos(campoMinutos.getText(), campoSegundos.getText());
            int id = ventanaPrincipal.getAlbumController().generarNuevoIdCancion();
            Cancion cancion = new Cancion(id, campoNombre.getText(), duracion);
            ventanaPrincipal.getAlbumController().agregarCancion(albumIdActual, cancion);
            refrescarDatosAlbum();
        } catch (NumberFormatException excepcion) {
            mostrarError("Minutos y segundos deben ser números válidos.");
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private void modificarCancion() {
        if (cancionSeleccionada == null) {
            mostrarError("Seleccioná una canción primero.");
            return;
        }
        JTextField campoNombre = new JTextField(cancionSeleccionada.getNombre());
        JTextField campoMinutos = new JTextField(String.valueOf(cancionSeleccionada.getDuracionSegundos() / 60));
        JTextField campoSegundos = new JTextField(String.valueOf(cancionSeleccionada.getDuracionSegundos() % 60));
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre:"));
        panel.add(campoNombre);
        panel.add(new JLabel("Minutos:"));
        panel.add(campoMinutos);
        panel.add(new JLabel("Segundos:"));
        panel.add(campoSegundos);

        int resultado = DialogoUtil.mostrarFormulario(this, panel, "Modificar Canción");
        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            int duracion = leerDuracionSegundos(campoMinutos.getText(), campoSegundos.getText());
            Cancion cancionModificada = new Cancion(cancionSeleccionada.getId(), campoNombre.getText(), duracion);
            ventanaPrincipal.getAlbumController().modificarCancion(albumIdActual, cancionModificada);
            refrescarDatosAlbum();
        } catch (NumberFormatException excepcion) {
            mostrarError("Minutos y segundos deben ser números válidos.");
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private int leerDuracionSegundos(String textoMinutos, String textoSegundos) {
        int minutos = Integer.parseInt(textoMinutos.trim());
        int segundos = Integer.parseInt(textoSegundos.trim());
        if (minutos < 0 || segundos < 0 || segundos > 59) {
            throw new IllegalArgumentException("Los minutos no pueden ser negativos y los segundos deben estar entre 0 y 59.");
        }
        return minutos * 60 + segundos;
    }

    private void eliminarCancion() {
        if (cancionSeleccionada == null) {
            mostrarError("Seleccioná una canción primero.");
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la canción \"" + cancionSeleccionada.getNombre() + "\"?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            ventanaPrincipal.getAlbumController().eliminarCancion(albumIdActual, cancionSeleccionada.getId());
            cancionSeleccionada = null;
            refrescarDatosAlbum();
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}