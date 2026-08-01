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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import controlador.AlbumController;
import controlador.ArtistaController;
import modelo.Album;
import modelo.Artista;
import modelo.Cancion;
import util.Config;
import util.MusicBrainzCliente;

public class PanelAlbums extends JPanel {

    private final VentanaPrincipal ventanaPrincipal;
    private final DefaultListModel<Album> modeloAlbumes;
    private final JList<Album> listaAlbumes;
    private final CampoTextoRedondeado campoBusqueda;
    private final JComboBox<String> comboOrden;

    private static final Object MARCADOR_MANUAL = new Object();
    private List<MusicBrainzCliente.ResultadoAlbum> resultadosBusquedaMusicBrainz = new ArrayList<>();
    private MusicBrainzCliente.ResultadoAlbum resultadoMusicBrainzSeleccionado;
    private PanelDesplazable panelResultadosMusicBrainz;
    private final Map<String, ImageIcon> cacheMiniaturasMusicBrainz = new HashMap<>();

    public PanelAlbums(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
        this.modeloAlbumes = new DefaultListModel<>();
        this.listaAlbumes = new JList<>(modeloAlbumes);
        this.campoBusqueda = new CampoTextoRedondeado("Buscador");
        this.comboOrden = new JComboBox<>(new String[] { "Nombre", "Año", "Nota promedio" });

        setLayout(new BorderLayout());
        setBackground(TemaVisual.FONDO);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        PanelRedondeado tarjetaGeneral = new PanelRedondeado(TemaVisual.FONDO_TARJETA, TemaVisual.BORDE_ACENTO, 24);
        tarjetaGeneral.setLayout(new BorderLayout(0, 12));
        tarjetaGeneral.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        tarjetaGeneral.add(construirPanelSuperior(), BorderLayout.NORTH);

        listaAlbumes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaAlbumes.setBackground(TemaVisual.FONDO_TARJETA);
        listaAlbumes.setOpaque(true);
        listaAlbumes.setCellRenderer(this::construirFilaAlbum);
        listaAlbumes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2) {
                    verDetalle();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(listaAlbumes);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(TemaVisual.FONDO_TARJETA);
        scroll.getVerticalScrollBar().setUI(new BarraDesplazamientoRedondeada());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(18, 0));
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        tarjetaGeneral.add(scroll, BorderLayout.CENTER);

        add(tarjetaGeneral, BorderLayout.CENTER);

        refrescar();
    }

    private JPanel construirPanelSuperior() {
        JPanel contenedor = new JPanel();
        contenedor.setOpaque(false);
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));

        JPanel filaBusqueda = new JPanel(new BorderLayout(10, 0));
        filaBusqueda.setOpaque(false);
        filaBusqueda.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        filaBusqueda.add(campoBusqueda, BorderLayout.CENTER);

        comboOrden.setBackground(TemaVisual.FONDO_TARJETA);
        comboOrden.setForeground(TemaVisual.TEXTO_CLARO);
        comboOrden.setFont(new Font("Serif", Font.BOLD, 14));
        comboOrden.setBorder(new LineBorder(TemaVisual.BORDE_ACENTO, 1, true));
        comboOrden.setPreferredSize(new Dimension(160, comboOrden.getPreferredSize().height + 14));
        comboOrden.addActionListener(evento -> ordenar());
        filaBusqueda.add(comboOrden, BorderLayout.EAST);

        contenedor.add(filaBusqueda);

        JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaBotones.setOpaque(false);
        BotonRedondeado botonAgregar = new BotonRedondeado("Agregar +", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonModificar = new BotonRedondeado("Modificar", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonEliminar = new BotonRedondeado("Borrar -", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);

        botonAgregar.addActionListener(evento -> agregarConBusquedaMusicBrainz());
        botonModificar.addActionListener(evento -> modificar());
        botonEliminar.addActionListener(evento -> eliminar());

        filaBotones.add(botonAgregar);
        filaBotones.add(botonModificar);
        filaBotones.add(botonEliminar);
        contenedor.add(filaBotones);

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

        return contenedor;
    }

    private Component construirFilaAlbum(JList<? extends Album> lista, Album album, int indice, boolean seleccionado,
            boolean tieneFoco) {
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 8));

        PanelRedondeado fila = new PanelRedondeado(TemaVisual.FONDO_FILA,
                seleccionado ? TemaVisual.TEXTO_CLARO : TemaVisual.BORDE_ACENTO, 20);
        fila.setLayout(new BorderLayout(15, 0));
        fila.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 20));

        JLabel etiquetaPortada = new JLabel(ImagenUtil.cargarEscalada(album.getRutaPortada(), 55, 55));
        etiquetaPortada.setPreferredSize(new Dimension(55, 55));
        fila.add(etiquetaPortada, BorderLayout.WEST);

        JPanel panelTextos = new JPanel();
        panelTextos.setOpaque(false);
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));

        JLabel etiquetaNombre = new JLabel(album.getNombre());
        etiquetaNombre.setFont(new Font("Serif", Font.BOLD, 22));
        etiquetaNombre.setForeground(TemaVisual.TEXTO_CLARO);

        String nombreArtistaMostrar = album.obtenerNombreArtistaParaMostrar();
        JLabel etiquetaArtista = new JLabel(nombreArtistaMostrar == null ? "Sin artista asignado"
                : album.getArtista() == null ? nombreArtistaMostrar + " (sin registrar)" : nombreArtistaMostrar);
        etiquetaArtista.setFont(new Font("Serif", Font.BOLD, 13));
        etiquetaArtista.setForeground(TemaVisual.TEXTO_SECUNDARIO);

        JLabel etiquetaAnio = new JLabel(String.valueOf(album.getAnioLanzamiento()));
        etiquetaAnio.setFont(new Font("Serif", Font.BOLD, 13));
        etiquetaAnio.setForeground(TemaVisual.TEXTO_SECUNDARIO);

        panelTextos.add(etiquetaNombre);
        panelTextos.add(etiquetaArtista);
        panelTextos.add(etiquetaAnio);
        fila.add(panelTextos, BorderLayout.CENTER);

        JPanel panelNota = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        panelNota.setOpaque(false);

        JLabel etiquetaNotaTexto = new JLabel("Nota:");
        etiquetaNotaTexto.setFont(new Font("Serif", Font.BOLD, 20));
        etiquetaNotaTexto.setForeground(TemaVisual.TEXTO_CLARO);

        PanelRedondeado badgeNota = new PanelRedondeado(TemaVisual.FONDO_BADGE, TemaVisual.BORDE_ACENTO, 12);
        badgeNota.setLayout(new BorderLayout());
        badgeNota.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        JLabel etiquetaNotaValor = new JLabel(album.getNotaPromedioTexto());
        etiquetaNotaValor.setFont(new Font("Serif", Font.BOLD, 20));
        etiquetaNotaValor.setForeground(TemaVisual.TEXTO_CLARO);
        badgeNota.add(etiquetaNotaValor, BorderLayout.CENTER);

        panelNota.add(etiquetaNotaTexto);
        panelNota.add(badgeNota);
        fila.add(panelNota, BorderLayout.EAST);

        envoltorio.add(fila, BorderLayout.CENTER);
        return envoltorio;
    }

    public void refrescar() {
        mostrarAlbumes(ventanaPrincipal.getAlbumController().obtenerTodos());
    }

    private void mostrarAlbumes(List<Album> albumes) {
        modeloAlbumes.clear();
        for (Album album : albumes) {
            modeloAlbumes.addElement(album);
        }
    }

    private void buscar() {
        String texto = campoBusqueda.getText();
        AlbumController controlador = ventanaPrincipal.getAlbumController();
        List<Album> resultados = texto.trim().isEmpty() ? controlador.obtenerTodos() : controlador.buscarPorNombre(texto);
        mostrarAlbumes(resultados);
    }

    private void ordenar() {
        AlbumController controlador = ventanaPrincipal.getAlbumController();
        List<Album> actuales = obtenerListaActual();
        String criterio = (String) comboOrden.getSelectedItem();
        List<Album> ordenados;
        if ("Año".equals(criterio)) {
            ordenados = controlador.ordenarPorAnio(actuales);
        } else if ("Nota promedio".equals(criterio)) {
            ordenados = controlador.ordenarPorNotaPromedio(actuales);
        } else {
            ordenados = controlador.ordenarPorNombre(actuales);
        }
        mostrarAlbumes(ordenados);
    }

    private List<Album> obtenerListaActual() {
        List<Album> lista = new ArrayList<>();
        for (int i = 0; i < modeloAlbumes.size(); i++) {
            lista.add(modeloAlbumes.get(i));
        }
        return lista;
    }

    private void agregar() {
        Album album = mostrarDialogoAlbum(null);
        if (album == null) {
            return;
        }
        try {
            ventanaPrincipal.getAlbumController().agregar(album);
            refrescar();
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private void agregarConBusquedaMusicBrainz() {
        Object resultado = mostrarDialogoBusquedaMusicBrainz();
        if (resultado == null) {
            return;
        }
        if (resultado == MARCADOR_MANUAL) {
            agregar();
            return;
        }

        MusicBrainzCliente.ResultadoAlbum seleccionado = (MusicBrainzCliente.ResultadoAlbum) resultado;

        File portadaDescargada = null;
        try {
            Path rutaTemporal = MusicBrainzCliente.descargarPortada(seleccionado.getMbid());
            if (rutaTemporal != null) {
                portadaDescargada = rutaTemporal.toFile();
            }
        } catch (IOException excepcion) {
            // La portada es opcional: si falla la descarga, seguimos sin ella.
        }

        List<MusicBrainzCliente.PistaAlbum> pistas;
        try {
            pistas = MusicBrainzCliente.obtenerPistas(seleccionado.getMbid());
        } catch (IOException excepcion) {
            mostrarError("No se pudieron obtener las canciones desde MusicBrainz: " + excepcion.getMessage()
                    + "\nEl álbum se puede seguir agregando, solo que sin canciones precargadas.");
            pistas = new ArrayList<>();
        }

        Artista artistaCoincidente = buscarArtistaExacto(seleccionado.getArtista());
        Album album = mostrarDialogoAlbum(null, seleccionado.getTitulo(), seleccionado.getAnio(), artistaCoincidente,
                portadaDescargada, seleccionado.getArtista());
        if (album == null) {
            return;
        }
        try {
            ventanaPrincipal.getAlbumController().agregar(album);
            for (MusicBrainzCliente.PistaAlbum pista : pistas) {
                try {
                    int idCancion = ventanaPrincipal.getAlbumController().generarNuevoIdCancion();
                    Cancion cancion = new Cancion(idCancion, pista.getTitulo(), pista.getDuracionSegundos());
                    ventanaPrincipal.getAlbumController().agregarCancion(album.getId(), cancion);
                } catch (IllegalArgumentException | IOException excepcionCancion) {
                    // Si una canción puntual falla (p. ej. nombre duplicado), seguimos con el resto.
                }
            }
            refrescar();
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private Artista buscarArtistaExacto(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }
        for (Artista artista : ventanaPrincipal.getArtistaController().obtenerTodos()) {
            if (artista.getNombre().equalsIgnoreCase(nombre.trim())) {
                return artista;
            }
        }
        return null;
    }

    private Object mostrarDialogoBusquedaMusicBrainz() {
        resultadosBusquedaMusicBrainz = new ArrayList<>();
        resultadoMusicBrainzSeleccionado = null;
        panelResultadosMusicBrainz = new PanelDesplazable();
        panelResultadosMusicBrainz.setBackground(TemaVisual.FONDO_FILA);

        CampoTextoRedondeado campoNombreMb = new CampoTextoRedondeado("Nombre del álbum...");
        campoNombreMb.setPreferredSize(new Dimension(340, 36));
        campoNombreMb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        CampoTextoRedondeado campoArtistaMb = new CampoTextoRedondeado("Artista (opcional)...");
        campoArtistaMb.setPreferredSize(new Dimension(340, 36));
        campoArtistaMb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        BotonRedondeado botonBuscarMb = new BotonRedondeado("Buscar", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);

        JPanel filaNombreMb = new JPanel(new BorderLayout(10, 0));
        filaNombreMb.setOpaque(false);
        filaNombreMb.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaNombreMb.add(DialogoUtil.crearEtiquetaCampo("Nombre:"), BorderLayout.WEST);
        filaNombreMb.add(campoNombreMb, BorderLayout.CENTER);

        JPanel filaArtistaMb = new JPanel(new BorderLayout(10, 0));
        filaArtistaMb.setOpaque(false);
        filaArtistaMb.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaArtistaMb.add(DialogoUtil.crearEtiquetaCampo("Artista:"), BorderLayout.WEST);
        filaArtistaMb.add(campoArtistaMb, BorderLayout.CENTER);
        filaArtistaMb.add(botonBuscarMb, BorderLayout.EAST);

        JLabel etiquetaEstado = new JLabel(" ");
        etiquetaEstado.setFont(new Font("Serif", Font.BOLD, 12));
        etiquetaEstado.setForeground(TemaVisual.TEXTO_SECUNDARIO);
        etiquetaEstado.setAlignmentX(Component.LEFT_ALIGNMENT);

        PanelRedondeado tarjetaResultados = new PanelRedondeado(TemaVisual.FONDO_FILA, TemaVisual.BORDE_ACENTO, 16);
        tarjetaResultados.setLayout(new BorderLayout());
        tarjetaResultados.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        tarjetaResultados.setPreferredSize(new Dimension(420, 260));
        tarjetaResultados.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane scrollResultados = new JScrollPane(panelResultadosMusicBrainz);
        scrollResultados.setBorder(BorderFactory.createEmptyBorder());
        scrollResultados.setOpaque(false);
        scrollResultados.getViewport().setOpaque(true);
        scrollResultados.getViewport().setBackground(TemaVisual.FONDO_FILA);
        scrollResultados.getVerticalScrollBar().setUI(new BarraDesplazamientoRedondeada());
        scrollResultados.getVerticalScrollBar().setPreferredSize(new Dimension(14, 0));
        scrollResultados.getVerticalScrollBar().setOpaque(false);
        tarjetaResultados.add(scrollResultados, BorderLayout.CENTER);

        JPanel panelCampos = new JPanel();
        panelCampos.setOpaque(false);
        panelCampos.setLayout(new BoxLayout(panelCampos, BoxLayout.Y_AXIS));
        panelCampos.add(filaNombreMb);
        panelCampos.add(Box.createVerticalStrut(10));
        panelCampos.add(filaArtistaMb);
        panelCampos.add(Box.createVerticalStrut(8));
        panelCampos.add(etiquetaEstado);
        panelCampos.add(Box.createVerticalStrut(8));
        panelCampos.add(tarjetaResultados);

        PanelRedondeado tarjeta = new PanelRedondeado(TemaVisual.FONDO_TARJETA, TemaVisual.BORDE_ACENTO, 22);
        tarjeta.setLayout(new BorderLayout(0, 16));
        tarjeta.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel etiquetaTitulo = new JLabel("Buscar Álbum en MusicBrainz", SwingConstants.CENTER);
        etiquetaTitulo.setFont(new Font("Serif", Font.BOLD, 22));
        etiquetaTitulo.setForeground(TemaVisual.TEXTO_CLARO);
        tarjeta.add(etiquetaTitulo, BorderLayout.NORTH);
        tarjeta.add(panelCampos, BorderLayout.CENTER);

        JOptionPane opcionPane = new JOptionPane(tarjeta, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
                new Object[0]);
        opcionPane.setBorder(BorderFactory.createEmptyBorder());
        opcionPane.setBackground(TemaVisual.FONDO);
        opcionPane.setOpaque(true);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        panelBotones.setOpaque(false);
        BotonRedondeado botonSeleccionar = new BotonRedondeado("Seleccionar", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonManual = new BotonRedondeado("Agregar Manualmente", TemaVisual.BOTON_FONDO,
                TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonCancelar = new BotonRedondeado("Cancelar", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        panelBotones.add(botonSeleccionar);
        panelBotones.add(botonManual);
        panelBotones.add(botonCancelar);
        tarjeta.add(panelBotones, BorderLayout.SOUTH);

        Runnable ejecutarBusqueda = () -> {
            String nombre = campoNombreMb.getText().trim();
            String artista = campoArtistaMb.getText().trim();
            if (nombre.isEmpty() && artista.isEmpty()) {
                etiquetaEstado.setText("Ingresá al menos el nombre del álbum o el artista.");
                return;
            }
            String consulta = construirConsultaLucene(nombre, artista);
            etiquetaEstado.setText("Buscando...");
            botonBuscarMb.setEnabled(false);
            SwingWorker<List<MusicBrainzCliente.ResultadoAlbum>, Void> tarea = new SwingWorker<>() {
                @Override
                protected List<MusicBrainzCliente.ResultadoAlbum> doInBackground() throws Exception {
                    return MusicBrainzCliente.buscarAlbumes(consulta);
                }

                @Override
                protected void done() {
                    botonBuscarMb.setEnabled(true);
                    try {
                        resultadosBusquedaMusicBrainz = get();
                        resultadoMusicBrainzSeleccionado = null;
                        reconstruirFilasBusqueda();
                        etiquetaEstado.setText(resultadosBusquedaMusicBrainz.isEmpty() ? "Sin resultados."
                                : resultadosBusquedaMusicBrainz.size() + " resultado(s).");
                    } catch (Exception excepcionEjecucion) {
                        etiquetaEstado.setText("Error al buscar. Revisá tu conexión a internet.");
                    }
                }
            };
            tarea.execute();
        };
        botonBuscarMb.addActionListener(evento -> ejecutarBusqueda.run());
        campoNombreMb.addActionListener(evento -> ejecutarBusqueda.run());
        campoArtistaMb.addActionListener(evento -> ejecutarBusqueda.run());

        botonSeleccionar.addActionListener(evento -> {
            if (resultadoMusicBrainzSeleccionado == null) {
                etiquetaEstado.setText("Seleccioná un resultado de la lista primero.");
                return;
            }
            opcionPane.setValue(resultadoMusicBrainzSeleccionado);
        });
        botonManual.addActionListener(evento -> opcionPane.setValue(MARCADOR_MANUAL));
        botonCancelar.addActionListener(evento -> opcionPane.setValue(JOptionPane.CANCEL_OPTION));

        JDialog dialogo = opcionPane.createDialog(this, "Buscar Álbum en MusicBrainz");
        dialogo.getContentPane().setBackground(TemaVisual.FONDO);
        dialogo.setResizable(true);
        dialogo.setVisible(true);

        Object valor = opcionPane.getValue();
        if (valor instanceof MusicBrainzCliente.ResultadoAlbum || valor == MARCADOR_MANUAL) {
            return valor;
        }
        return null;
    }

    private JComponent construirFilaResultadoBusqueda(MusicBrainzCliente.ResultadoAlbum resultado) {
        boolean seleccionado = resultadoMusicBrainzSeleccionado == resultado;

        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setBorder(BorderFactory.createEmptyBorder(3, 4, 3, 8));

        JPanel fila = new PanelRedondeado(TemaVisual.FONDO_TARJETA,
                seleccionado ? TemaVisual.TEXTO_CLARO : TemaVisual.BORDE_ACENTO, 14);
        fila.setLayout(new BorderLayout());
        fila.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel etiquetaMiniatura = new JLabel();
        etiquetaMiniatura.setPreferredSize(new Dimension(45, 45));
        etiquetaMiniatura.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon miniaturaCacheada = cacheMiniaturasMusicBrainz.get(resultado.getMbid());
        if (miniaturaCacheada != null) {
            etiquetaMiniatura.setIcon(miniaturaCacheada);
        } else {
            cargarMiniaturaAsincronica(resultado.getMbid(), etiquetaMiniatura);
        }
        fila.add(etiquetaMiniatura, BorderLayout.WEST);

        String anioTexto = resultado.getAnio() == null ? "¿?" : String.valueOf(resultado.getAnio());
        String artistaTexto = resultado.getArtista().isEmpty() ? "Artista desconocido" : resultado.getArtista();
        JLabel etiqueta = new JLabel(resultado.getTitulo() + " — " + artistaTexto + " (" + anioTexto + ")");
        etiqueta.setFont(new Font("Serif", Font.BOLD, 15));
        etiqueta.setForeground(TemaVisual.TEXTO_CLARO);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        fila.add(etiqueta, BorderLayout.CENTER);

        MouseAdapter seleccionar = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                resultadoMusicBrainzSeleccionado = resultado;
                reconstruirFilasBusqueda();
            }
        };
        fila.addMouseListener(seleccionar);
        etiqueta.addMouseListener(seleccionar);
        etiquetaMiniatura.addMouseListener(seleccionar);

        envoltorio.add(fila, BorderLayout.CENTER);
        return envoltorio;
    }

    private void cargarMiniaturaAsincronica(String mbid, JLabel etiquetaDestino) {
        SwingWorker<ImageIcon, Void> tarea = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                Path rutaTemporal = MusicBrainzCliente.descargarMiniatura(mbid);
                if (rutaTemporal == null) {
                    return null;
                }
                return ImagenUtil.cargarEscalada(rutaTemporal.toString(), 45, 45);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icono = get();
                    if (icono != null) {
                        cacheMiniaturasMusicBrainz.put(mbid, icono);
                        etiquetaDestino.setIcon(icono);
                    }
                } catch (Exception excepcionEjecucion) {
                    // Sin miniatura disponible para esta release: la fila queda sin imagen.
                }
            }
        };
        tarea.execute();
    }

    private void reconstruirFilasBusqueda() {
        panelResultadosMusicBrainz.removeAll();
        for (MusicBrainzCliente.ResultadoAlbum resultado : resultadosBusquedaMusicBrainz) {
            panelResultadosMusicBrainz.add(construirFilaResultadoBusqueda(resultado));
        }
        panelResultadosMusicBrainz.revalidate();
        panelResultadosMusicBrainz.repaint();
    }

    /**
     * Arma una consulta en sintaxis Lucene de MusicBrainz combinando nombre de
     * álbum y artista como campos separados (release:"..." AND artist:"..."),
     * en vez de una búsqueda genérica de texto libre. Esto da resultados mucho
     * más precisos cuando se conocen ambos datos.
     */
    private String construirConsultaLucene(String nombre, String artista) {
        StringBuilder consulta = new StringBuilder();
        if (!nombre.isEmpty()) {
            consulta.append("release:\"").append(escaparLucene(nombre)).append("\"");
        }
        if (!artista.isEmpty()) {
            if (consulta.length() > 0) {
                consulta.append(" AND ");
            }
            consulta.append("artist:\"").append(escaparLucene(artista)).append("\"");
        }
        return consulta.toString();
    }

    private String escaparLucene(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void modificar() {
        Album seleccionado = listaAlbumes.getSelectedValue();
        if (seleccionado == null) {
            mostrarError("Seleccioná un álbum primero.");
            return;
        }
        Album modificado = mostrarDialogoAlbum(seleccionado);
        if (modificado == null) {
            return;
        }
        try {
            ventanaPrincipal.getAlbumController().modificar(modificado);
            refrescar();
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private void eliminar() {
        Album seleccionado = listaAlbumes.getSelectedValue();
        if (seleccionado == null) {
            mostrarError("Seleccioná un álbum primero.");
            return;
        }
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el álbum \"" + seleccionado.getNombre() + "\"? Se eliminarán también sus canciones.",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            ventanaPrincipal.getAlbumController().eliminar(seleccionado.getId());
            refrescar();
        } catch (IllegalArgumentException | IOException excepcion) {
            mostrarError(excepcion.getMessage());
        }
    }

    private void verDetalle() {
        Album seleccionado = listaAlbumes.getSelectedValue();
        if (seleccionado == null) {
            return;
        }
        ventanaPrincipal.mostrarAlbumDetalle(seleccionado.getId());
    }

    private Album mostrarDialogoAlbum(Album existente) {
        return mostrarDialogoAlbum(existente, null, null, null, null, null);
    }

    private Album mostrarDialogoAlbum(Album existente, String nombreSugerido, Integer anioSugerido,
            Artista artistaSugerido, File portadaSugerida, String nombreArtistaExternoSugerido) {
        ArtistaController controladorArtistas = ventanaPrincipal.getArtistaController();
        List<Artista> artistasDisponibles = controladorArtistas.obtenerTodos();
        if (artistasDisponibles.isEmpty()) {
            mostrarError("Primero debés registrar al menos un artista.");
            return null;
        }

        CampoTextoRedondeado campoNombre = new CampoTextoRedondeado("");
        if (existente != null) {
            campoNombre.setText(existente.getNombre());
        } else if (nombreSugerido != null) {
            campoNombre.setText(nombreSugerido);
        }
        campoNombre.setPreferredSize(new Dimension(320, 36));
        campoNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JComboBox<Artista> comboArtistas = new JComboBox<>(artistasDisponibles.toArray(new Artista[0]));
        comboArtistas.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean tieneFoco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, tieneFoco);
                if (valor instanceof Artista) {
                    setText(((Artista) valor).getNombre());
                }
                return this;
            }
        });
        comboArtistas.setBackground(TemaVisual.FONDO_TARJETA);
        comboArtistas.setForeground(TemaVisual.TEXTO_CLARO);
        comboArtistas.setFont(new Font("Serif", Font.BOLD, 14));
        comboArtistas.setBorder(new LineBorder(TemaVisual.BORDE_ACENTO, 1, true));
        comboArtistas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        comboArtistas.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel etiquetaAvisoArtista = new JLabel(" ");
        etiquetaAvisoArtista.setFont(new Font("Serif", Font.BOLD, 12));
        etiquetaAvisoArtista.setForeground(TemaVisual.TEXTO_SECUNDARIO);
        etiquetaAvisoArtista.setAlignmentX(Component.LEFT_ALIGNMENT);
        String nombreArtistaExternoExistente = existente == null ? null : existente.getNombreArtistaSugerido();
        if (existente != null && existente.getArtista() != null) {
            comboArtistas.setSelectedItem(existente.getArtista());
        } else if (existente != null) {
            comboArtistas.setSelectedIndex(-1);
            etiquetaAvisoArtista.setText(nombreArtistaExternoExistente != null
                    ? "Este álbum es de \"" + nombreArtistaExternoExistente + "\", que todavía no está en tu lista local. Seleccioná uno o creálo primero."
                    : "Este álbum todavía no tiene artista asignado. Seleccioná uno si querés asignarlo ahora.");
        } else if (artistaSugerido != null) {
            comboArtistas.setSelectedItem(artistaSugerido);
        } else if (nombreArtistaExternoSugerido != null) {
            comboArtistas.setSelectedIndex(-1);
            etiquetaAvisoArtista.setText("No se encontró a \"" + nombreArtistaExternoSugerido
                    + "\" en tu lista local. Seleccioná uno o cancelá para crearlo primero.");
        }

        CampoTextoRedondeado campoAnio = new CampoTextoRedondeado("");
        if (existente != null) {
            campoAnio.setText(String.valueOf(existente.getAnioLanzamiento()));
        } else if (anioSugerido != null) {
            campoAnio.setText(String.valueOf(anioSugerido));
        } else {
            campoAnio.setText(String.valueOf(LocalDate.now().getYear()));
        }
        campoAnio.setPreferredSize(new Dimension(320, 36));
        campoAnio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel etiquetaPortada = new JLabel();
        etiquetaPortada.setText(existente != null && existente.getRutaPortada() != null
                ? new File(existente.getRutaPortada()).getName()
                : portadaSugerida != null ? portadaSugerida.getName() : "Sin portada");
        etiquetaPortada.setFont(new Font("Serif", Font.BOLD, 14));
        etiquetaPortada.setForeground(TemaVisual.TEXTO_CLARO);
        final File[] archivoPortadaSeleccionado = new File[] { portadaSugerida };
        BotonRedondeado botonSeleccionarPortada = new BotonRedondeado("Seleccionar Portada", TemaVisual.BOTON_FONDO,
                TemaVisual.BOTON_TEXTO);
        botonSeleccionarPortada.addActionListener(evento -> {
            JFileChooser selector = new JFileChooser();
            selector.setFileFilter(new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif"));
            int resultadoSelector = selector.showOpenDialog(this);
            if (resultadoSelector == JFileChooser.APPROVE_OPTION) {
                archivoPortadaSeleccionado[0] = selector.getSelectedFile();
                etiquetaPortada.setText(archivoPortadaSeleccionado[0].getName());
            }
        });

        JPanel filaPortada = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaPortada.setOpaque(false);
        filaPortada.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaPortada.add(botonSeleccionarPortada);
        filaPortada.add(etiquetaPortada);

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(DialogoUtil.crearEtiquetaCampo("Nombre:"));
        panel.add(campoNombre);
        panel.add(Box.createVerticalStrut(12));
        panel.add(DialogoUtil.crearEtiquetaCampo("Artista:"));
        panel.add(comboArtistas);
        panel.add(etiquetaAvisoArtista);
        panel.add(Box.createVerticalStrut(12));
        panel.add(DialogoUtil.crearEtiquetaCampo("Año de lanzamiento:"));
        panel.add(campoAnio);
        panel.add(Box.createVerticalStrut(12));
        panel.add(filaPortada);

        int resultado = DialogoUtil.mostrarFormulario(this, panel,
                existente == null ? "Agregar Album" : "Modificar Album");
        if (resultado != JOptionPane.OK_OPTION) {
            return null;
        }

        try {
            int anio = Integer.parseInt(campoAnio.getText().trim());
            Artista artistaSeleccionado = (Artista) comboArtistas.getSelectedItem();
            int id = existente == null ? ventanaPrincipal.getAlbumController().generarNuevoIdAlbum() : existente.getId();

            String rutaPortada = existente == null ? null : existente.getRutaPortada();
            if (archivoPortadaSeleccionado[0] != null) {
                rutaPortada = copiarPortada(archivoPortadaSeleccionado[0], id);
            }

            Album album = new Album(id, campoNombre.getText(), artistaSeleccionado, anio, rutaPortada);
            if (artistaSeleccionado == null) {
                String nombreSugeridoFinal = nombreArtistaExternoSugerido != null ? nombreArtistaExternoSugerido
                        : nombreArtistaExternoExistente;
                album.setNombreArtistaSugerido(nombreSugeridoFinal);
            }
            if (existente != null) {
                for (Cancion cancion : existente.getCanciones()) {
                    album.agregarCancion(cancion);
                }
            }
            return album;
        } catch (NumberFormatException excepcion) {
            mostrarError("El año debe ser un número válido.");
            return null;
        } catch (IllegalArgumentException excepcion) {
            mostrarError(excepcion.getMessage());
            return null;
        }
    }

    private String copiarPortada(File archivoOrigen, int albumId) {
        try {
            Config.inicializarDirectorios();
            String extension = obtenerExtension(archivoOrigen.getName());
            String nombreDestino = "album_" + albumId + (extension.isEmpty() ? "" : "." + extension);
            Path destino = Paths.get(Config.getRutaPortadas(), nombreDestino);
            Files.copy(archivoOrigen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            return destino.toString();
        } catch (IOException excepcion) {
            mostrarError("No se pudo copiar la portada: " + excepcion.getMessage());
            return null;
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int indice = nombreArchivo.lastIndexOf('.');
        return indice == -1 ? "" : nombreArchivo.substring(indice + 1);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}