package vista;

import java.awt.BorderLayout;
import java.awt.Component;
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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
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

public class PanelAlbums extends JPanel {

    private final VentanaPrincipal ventanaPrincipal;
    private final DefaultListModel<Album> modeloAlbumes;
    private final JList<Album> listaAlbumes;
    private final CampoTextoRedondeado campoBusqueda;
    private final JComboBox<String> comboOrden;

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

        botonAgregar.addActionListener(evento -> agregar());
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

        JLabel etiquetaArtista = new JLabel(album.getArtista().getNombre());
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
        ArtistaController controladorArtistas = ventanaPrincipal.getArtistaController();
        List<Artista> artistasDisponibles = controladorArtistas.obtenerTodos();
        if (artistasDisponibles.isEmpty()) {
            mostrarError("Primero debés registrar al menos un artista.");
            return null;
        }

        CampoTextoRedondeado campoNombre = new CampoTextoRedondeado("");
        campoNombre.setText(existente == null ? "" : existente.getNombre());
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
        if (existente != null) {
            comboArtistas.setSelectedItem(existente.getArtista());
        }

        CampoTextoRedondeado campoAnio = new CampoTextoRedondeado("");
        campoAnio.setText(
                existente == null ? String.valueOf(LocalDate.now().getYear()) : String.valueOf(existente.getAnioLanzamiento()));
        campoAnio.setPreferredSize(new Dimension(320, 36));
        campoAnio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel etiquetaPortada = new JLabel(existente == null || existente.getRutaPortada() == null ? "Sin portada"
                : new File(existente.getRutaPortada()).getName());
        etiquetaPortada.setFont(new Font("Serif", Font.BOLD, 14));
        etiquetaPortada.setForeground(TemaVisual.TEXTO_CLARO);
        final File[] archivoPortadaSeleccionado = new File[1];
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