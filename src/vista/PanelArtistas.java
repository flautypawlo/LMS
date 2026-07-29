package vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controlador.ArtistaController;
import modelo.Album;
import modelo.Artista;
import modelo.Banda;
import modelo.Solista;

public class PanelArtistas extends JPanel {

    private final VentanaPrincipal ventanaPrincipal;
    private final CampoTextoRedondeado campoBusqueda;
    private final PanelDesplazable panelSolistas;
    private final PanelDesplazable panelBandas;

    private List<Artista> solistasMostrados = new ArrayList<>();
    private List<Artista> bandasMostrados = new ArrayList<>();
    private Artista artistaSeleccionado;

    public PanelArtistas(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
        this.campoBusqueda = new CampoTextoRedondeado("Buscador");
        this.panelSolistas = new PanelDesplazable();
        this.panelBandas = new PanelDesplazable();

        setLayout(new BorderLayout(0, 12));
        setBackground(TemaVisual.FONDO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(campoBusqueda, BorderLayout.NORTH);

        JPanel panelColumnas = new JPanel(new GridLayout(1, 2, 20, 0));
        panelColumnas.setOpaque(false);
        panelColumnas.add(construirColumna("Solistas", panelSolistas));
        panelColumnas.add(construirColumna("Bandas", panelBandas));
        add(panelColumnas, BorderLayout.CENTER);

        add(construirPanelBotones(), BorderLayout.SOUTH);

        campoBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent evento) {
                buscar();
            }

            @Override
            public void removeUpdate(DocumentEvent evento) {
                buscar();
            }

            @Override
            public void changedUpdate(DocumentEvent evento) {
                buscar();
            }
        });

        refrescar();
    }

    private JComponent construirColumna(String titulo, PanelDesplazable panelLista) {
        PanelConPestana tarjeta = new PanelConPestana(titulo);

        panelLista.setBackground(TemaVisual.FONDO_TARJETA);

        JScrollPane scroll = new JScrollPane(panelLista);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(TemaVisual.FONDO_TARJETA);
        scroll.getVerticalScrollBar().setUI(new BarraDesplazamientoRedondeada());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(16, 0));
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        tarjeta.add(scroll, BorderLayout.CENTER);
        return tarjeta;
    }

    private JPanel construirPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        panelBotones.setOpaque(false);

        BotonRedondeado botonAgregarSolista = new BotonRedondeado("+ Solista", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonAgregarBanda = new BotonRedondeado("+ Banda", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonModificar = new BotonRedondeado("\u270E", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonEliminar = new BotonRedondeado("\u2212", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);

        botonAgregarSolista.addActionListener(evento -> agregarSolista());
        botonAgregarBanda.addActionListener(evento -> agregarBanda());
        botonModificar.addActionListener(evento -> modificar());
        botonEliminar.addActionListener(evento -> eliminar());

        panelBotones.add(botonAgregarSolista);
        panelBotones.add(botonAgregarBanda);
        panelBotones.add(botonModificar);
        panelBotones.add(botonEliminar);
        return panelBotones;
    }

    private JComponent construirFilaArtista(Artista artista) {
        boolean seleccionado = artistaSeleccionado != null && artistaSeleccionado.getId() == artista.getId();

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 8));

        JPanel fila = seleccionado
                ? new PanelRedondeado(TemaVisual.FONDO_FILA, TemaVisual.TEXTO_CLARO, 14)
                : construirPanelPlano();
        fila.setLayout(new BorderLayout());
        fila.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel etiquetaNombre = new JLabel(artista.getNombre());
        etiquetaNombre.setFont(new Font("Serif", Font.BOLD, 17));
        etiquetaNombre.setForeground(TemaVisual.TEXTO_CLARO);
        fila.add(etiquetaNombre, BorderLayout.CENTER);

        MouseAdapter seleccionar = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                artistaSeleccionado = artista;
                if (evento.getClickCount() == 2) {
                    ventanaPrincipal.mostrarArtistaDetalle(artista.getId());
                    return;
                }
                reconstruirListas();
            }
        };
        fila.addMouseListener(seleccionar);
        etiquetaNombre.addMouseListener(seleccionar);

        envoltorio.add(fila, BorderLayout.CENTER);
        return envoltorio;
    }

    private JPanel construirPanelPlano() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    private void reconstruirListas() {
        panelSolistas.removeAll();
        for (Artista artista : solistasMostrados) {
            panelSolistas.add(construirFilaArtista(artista));
        }
        panelSolistas.revalidate();
        panelSolistas.repaint();

        panelBandas.removeAll();
        for (Artista artista : bandasMostrados) {
            panelBandas.add(construirFilaArtista(artista));
        }
        panelBandas.revalidate();
        panelBandas.repaint();
    }

    public void refrescar() {
        artistaSeleccionado = null;
        campoBusqueda.setText("");
        buscar();
    }

    private void buscar() {
        String texto = campoBusqueda.getText();
        ArtistaController controlador = ventanaPrincipal.getArtistaController();
        List<Artista> resultados = texto.trim().isEmpty() ? controlador.obtenerTodos() : controlador.buscarPorNombre(texto);
        solistasMostrados = new ArrayList<>();
        bandasMostrados = new ArrayList<>();
        for (Artista artista : resultados) {
            if ("Solista".equals(artista.getTipo())) {
                solistasMostrados.add(artista);
            } else {
                bandasMostrados.add(artista);
            }
        }
        reconstruirListas();
    }

    private void agregarSolista() {
        Solista solista = mostrarDialogoSolista(null);
        if (solista == null) {
            return;
        }
        try {
            ventanaPrincipal.getArtistaController().agregar(solista);
            buscar();
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private void agregarBanda() {
        Banda banda = mostrarDialogoBanda(null);
        if (banda == null) {
            return;
        }
        try {
            ventanaPrincipal.getArtistaController().agregar(banda);
            buscar();
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private void modificar() {
        if (artistaSeleccionado == null) {
            mostrarError("Seleccioná un artista primero.");
            return;
        }
        try {
            if (artistaSeleccionado instanceof Solista) {
                Solista solista = mostrarDialogoSolista((Solista) artistaSeleccionado);
                if (solista != null) {
                    ventanaPrincipal.getArtistaController().modificar(solista);
                }
            } else {
                Banda banda = mostrarDialogoBanda((Banda) artistaSeleccionado);
                if (banda != null) {
                    ventanaPrincipal.getArtistaController().modificar(banda);
                }
            }
            buscar();
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private void eliminar() {
        if (artistaSeleccionado == null) {
            mostrarError("Seleccioná un artista primero.");
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + artistaSeleccionado.getNombre() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            List<Album> albumesExistentes = ventanaPrincipal.getAlbumController().obtenerTodos();
            ventanaPrincipal.getArtistaController().eliminar(artistaSeleccionado.getId(), albumesExistentes);
            artistaSeleccionado = null;
            buscar();
        } catch (IllegalArgumentException | IllegalStateException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private Solista mostrarDialogoSolista(Solista existente) {
        CampoTextoRedondeado campoNombre = new CampoTextoRedondeado("");
        campoNombre.setText(existente == null ? "" : existente.getNombre());
        campoNombre.setPreferredSize(new Dimension(320, 36));
        campoNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        CampoTextoRedondeado campoPais = new CampoTextoRedondeado("");
        campoPais.setText(existente == null ? "" : existente.getPaisNacimiento());
        campoPais.setPreferredSize(new Dimension(320, 36));
        campoPais.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        LocalDate nacimientoExistente = existente == null ? null : existente.getFechaNacimiento();
        CampoTextoRedondeado campoAnioNacimiento = crearCampoFechaPequenio(
                nacimientoExistente == null ? "" : String.valueOf(nacimientoExistente.getYear()));
        CampoTextoRedondeado campoMesNacimiento = crearCampoFechaPequenio(
                nacimientoExistente == null ? "" : String.valueOf(nacimientoExistente.getMonthValue()));
        CampoTextoRedondeado campoDiaNacimiento = crearCampoFechaPequenio(
                nacimientoExistente == null ? "" : String.valueOf(nacimientoExistente.getDayOfMonth()));

        LocalDate fallecimientoExistente = existente == null ? null : existente.getFechaFallecimiento();
        CampoTextoRedondeado campoAnioFallecimiento = crearCampoFechaPequenio(
                fallecimientoExistente == null ? "" : String.valueOf(fallecimientoExistente.getYear()));
        CampoTextoRedondeado campoMesFallecimiento = crearCampoFechaPequenio(
                fallecimientoExistente == null ? "" : String.valueOf(fallecimientoExistente.getMonthValue()));
        CampoTextoRedondeado campoDiaFallecimiento = crearCampoFechaPequenio(
                fallecimientoExistente == null ? "" : String.valueOf(fallecimientoExistente.getDayOfMonth()));

        JPanel filaNacimiento = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaNacimiento.setOpaque(false);
        filaNacimiento.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaNacimiento.add(DialogoUtil.crearEtiquetaCampo("Año:"));
        filaNacimiento.add(campoAnioNacimiento);
        filaNacimiento.add(DialogoUtil.crearEtiquetaCampo("Mes:"));
        filaNacimiento.add(campoMesNacimiento);
        filaNacimiento.add(DialogoUtil.crearEtiquetaCampo("Día:"));
        filaNacimiento.add(campoDiaNacimiento);

        JPanel filaFallecimiento = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaFallecimiento.setOpaque(false);
        filaFallecimiento.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaFallecimiento.add(DialogoUtil.crearEtiquetaCampo("Año:"));
        filaFallecimiento.add(campoAnioFallecimiento);
        filaFallecimiento.add(DialogoUtil.crearEtiquetaCampo("Mes:"));
        filaFallecimiento.add(campoMesFallecimiento);
        filaFallecimiento.add(DialogoUtil.crearEtiquetaCampo("Día:"));
        filaFallecimiento.add(campoDiaFallecimiento);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(DialogoUtil.crearEtiquetaCampo("Nombre:"));
        panel.add(campoNombre);
        panel.add(Box.createVerticalStrut(14));
        panel.add(DialogoUtil.crearEtiquetaCampo("Fecha de Nacimiento:"));
        panel.add(filaNacimiento);
        panel.add(Box.createVerticalStrut(10));
        panel.add(DialogoUtil.crearEtiquetaCampo("Fecha de Fallecimeinto (Opcional):"));
        panel.add(filaFallecimiento);
        panel.add(Box.createVerticalStrut(10));
        panel.add(DialogoUtil.crearEtiquetaCampo("Pais de Nacimiento:"));
        panel.add(campoPais);

        int resultado = DialogoUtil.mostrarFormulario(this, panel,
                existente == null ? "Agregar Solista" : "Modificar Solista");
        if (resultado != JOptionPane.OK_OPTION) {
            return null;
        }
        try {
            LocalDate fechaNacimiento = construirFecha(campoAnioNacimiento.getText(), campoMesNacimiento.getText(),
                    campoDiaNacimiento.getText());
            LocalDate fechaFallecimiento = construirFechaOpcional(campoAnioFallecimiento.getText(),
                    campoMesFallecimiento.getText(), campoDiaFallecimiento.getText());
            int id = existente == null ? ventanaPrincipal.getArtistaController().generarNuevoId() : existente.getId();
            return new Solista(id, campoNombre.getText(), fechaNacimiento, fechaFallecimiento, campoPais.getText());
        } catch (NumberFormatException | DateTimeException excepcion) {
            mostrarError("Verificá que año, mes y día sean valores numéricos válidos.");
            return null;
        } catch (IllegalArgumentException excepcion) {
            mostrarError(excepcion.getMessage());
            return null;
        }
    }

    private CampoTextoRedondeado crearCampoFechaPequenio(String textoInicial) {
        CampoTextoRedondeado campo = new CampoTextoRedondeado("");
        campo.setText(textoInicial);
        campo.setPreferredSize(new Dimension(64, 34));
        return campo;
    }

    private LocalDate construirFecha(String textoAnio, String textoMes, String textoDia) {
        int anio = Integer.parseInt(textoAnio.trim());
        int mes = Integer.parseInt(textoMes.trim());
        int dia = Integer.parseInt(textoDia.trim());
        return LocalDate.of(anio, mes, dia);
    }

    private LocalDate construirFechaOpcional(String textoAnio, String textoMes, String textoDia) {
        boolean todosVacios = textoAnio.trim().isEmpty() && textoMes.trim().isEmpty() && textoDia.trim().isEmpty();
        if (todosVacios) {
            return null;
        }
        if (textoAnio.trim().isEmpty() || textoMes.trim().isEmpty() || textoDia.trim().isEmpty()) {
            throw new IllegalArgumentException("Completá año, mes y día de fallecimiento, o dejá los tres vacíos.");
        }
        return construirFecha(textoAnio, textoMes, textoDia);
    }

    private Banda mostrarDialogoBanda(Banda existente) {
        CampoTextoRedondeado campoNombre = new CampoTextoRedondeado("");
        campoNombre.setText(existente == null ? "" : existente.getNombre());
        campoNombre.setPreferredSize(new Dimension(360, 36));
        campoNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JTextArea campoIntegrantes = new JTextArea(6, 24);
        campoIntegrantes.setLineWrap(true);
        campoIntegrantes.setWrapStyleWord(true);
        campoIntegrantes.setFont(new Font("Serif", Font.BOLD, 14));
        campoIntegrantes.setForeground(TemaVisual.TEXTO_CLARO);
        campoIntegrantes.setBackground(TemaVisual.FONDO_BADGE);
        campoIntegrantes.setCaretColor(TemaVisual.TEXTO_CLARO);
        campoIntegrantes.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        if (existente != null) {
            campoIntegrantes.setText(String.join("\n", existente.getIntegrantes()));
        }

        PanelRedondeado tarjetaIntegrantes = new PanelRedondeado(TemaVisual.FONDO_BADGE, TemaVisual.BORDE_ACENTO, 14);
        tarjetaIntegrantes.setLayout(new BorderLayout());
        tarjetaIntegrantes.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        tarjetaIntegrantes.add(new JScrollPane(campoIntegrantes), BorderLayout.CENTER);
        tarjetaIntegrantes.setAlignmentX(Component.LEFT_ALIGNMENT);
        tarjetaIntegrantes.setPreferredSize(new Dimension(360, 160));
        tarjetaIntegrantes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(DialogoUtil.crearEtiquetaCampo("Nombre:"));
        panel.add(campoNombre);
        panel.add(Box.createVerticalStrut(14));
        panel.add(DialogoUtil.crearEtiquetaCampo("Nombre de los integrantes (uno por linea):"));
        panel.add(tarjetaIntegrantes);

        int resultado = DialogoUtil.mostrarFormulario(this, panel,
                existente == null ? "Agregar Banda" : "Modificar Banda");
        if (resultado != JOptionPane.OK_OPTION) {
            return null;
        }
        List<String> integrantes = new ArrayList<>();
        for (String linea : campoIntegrantes.getText().split("\n")) {
            if (!linea.trim().isEmpty()) {
                integrantes.add(linea.trim());
            }
        }
        try {
            int id = existente == null ? ventanaPrincipal.getArtistaController().generarNuevoId() : existente.getId();
            return new Banda(id, campoNombre.getText(), integrantes);
        } catch (IllegalArgumentException excepcion) {
            mostrarError(excepcion.getMessage());
            return null;
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}