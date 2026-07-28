package vista;

import java.awt.BorderLayout;
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
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
        JTextField campoNombre = new JTextField(existente == null ? "" : existente.getNombre());
        JTextField campoPais = new JTextField(existente == null ? "" : existente.getPaisNacimiento());

        LocalDate nacimientoExistente = existente == null ? null : existente.getFechaNacimiento();
        JTextField campoAnioNacimiento = new JTextField(nacimientoExistente == null ? "" : String.valueOf(nacimientoExistente.getYear()), 4);
        JTextField campoMesNacimiento = new JTextField(nacimientoExistente == null ? "" : String.valueOf(nacimientoExistente.getMonthValue()), 4);
        JTextField campoDiaNacimiento = new JTextField(nacimientoExistente == null ? "" : String.valueOf(nacimientoExistente.getDayOfMonth()), 4);

        LocalDate fallecimientoExistente = existente == null ? null : existente.getFechaFallecimiento();
        JTextField campoAnioFallecimiento = new JTextField(fallecimientoExistente == null ? "" : String.valueOf(fallecimientoExistente.getYear()), 4);
        JTextField campoMesFallecimiento = new JTextField(fallecimientoExistente == null ? "" : String.valueOf(fallecimientoExistente.getMonthValue()), 4);
        JTextField campoDiaFallecimiento = new JTextField(fallecimientoExistente == null ? "" : String.valueOf(fallecimientoExistente.getDayOfMonth()), 4);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel filaNombre = new JPanel(new BorderLayout(5, 5));
        filaNombre.add(new JLabel("Nombre:"), BorderLayout.WEST);
        filaNombre.add(campoNombre, BorderLayout.CENTER);
        panel.add(filaNombre);

        panel.add(new JLabel("Fecha de nacimiento:"));
        JPanel filaNacimiento = new JPanel(new GridLayout(1, 3, 5, 5));
        filaNacimiento.add(crearCampoFecha("Año", campoAnioNacimiento));
        filaNacimiento.add(crearCampoFecha("Mes", campoMesNacimiento));
        filaNacimiento.add(crearCampoFecha("Día", campoDiaNacimiento));
        panel.add(filaNacimiento);

        panel.add(new JLabel("Fecha de fallecimiento (opcional):"));
        JPanel filaFallecimiento = new JPanel(new GridLayout(1, 3, 5, 5));
        filaFallecimiento.add(crearCampoFecha("Año", campoAnioFallecimiento));
        filaFallecimiento.add(crearCampoFecha("Mes", campoMesFallecimiento));
        filaFallecimiento.add(crearCampoFecha("Día", campoDiaFallecimiento));
        panel.add(filaFallecimiento);

        JPanel filaPais = new JPanel(new BorderLayout(5, 5));
        filaPais.add(new JLabel("País de nacimiento:"), BorderLayout.WEST);
        filaPais.add(campoPais, BorderLayout.CENTER);
        panel.add(filaPais);

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

    private JPanel crearCampoFecha(String etiqueta, JTextField campo) {
        JPanel panel = new JPanel(new BorderLayout(2, 2));
        panel.add(new JLabel(etiqueta), BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);
        return panel;
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
        JTextField campoNombre = new JTextField(existente == null ? "" : existente.getNombre());
        JTextArea campoIntegrantes = new JTextArea(5, 20);
        if (existente != null) {
            campoIntegrantes.setText(String.join("\n", existente.getIntegrantes()));
        }

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel panelNombre = new JPanel(new BorderLayout(5, 5));
        panelNombre.add(new JLabel("Nombre:"), BorderLayout.WEST);
        panelNombre.add(campoNombre, BorderLayout.CENTER);
        panel.add(panelNombre, BorderLayout.NORTH);
        panel.add(new JLabel("Integrantes (uno por línea):"), BorderLayout.CENTER);
        panel.add(new JScrollPane(campoIntegrantes), BorderLayout.SOUTH);

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