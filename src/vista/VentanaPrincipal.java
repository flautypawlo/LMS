package vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controlador.AlbumController;
import controlador.ArtistaController;
import controlador.EstadisticasController;
import util.Config;

public class VentanaPrincipal extends JFrame {

    private static final String TARJETA_BIENVENIDA = "BIENVENIDA";
    private static final String TARJETA_ALBUMS = "ALBUMS";
    private static final String TARJETA_ALBUM_DETALLE = "ALBUM_DETALLE";
    private static final String TARJETA_ARTISTAS = "ARTISTAS";
    private static final String TARJETA_ARTISTA_DETALLE = "ARTISTA_DETALLE";
    private static final String TARJETA_ESTADISTICAS = "ESTADISTICAS";

    private final ArtistaController artistaController;
    private final AlbumController albumController;
    private final EstadisticasController estadisticasController;

    private final CardLayout organizadorTarjetas;
    private final JPanel panelContenedor;

    private final PanelBienvenida panelBienvenida;
    private final PanelAlbums panelAlbums;
    private final PanelAlbumDetalle panelAlbumDetalle;
    private final PanelArtistas panelArtistas;
    private final PanelArtistaDetalle panelArtistaDetalle;
    private final PanelEstadisticas panelEstadisticas;

    public VentanaPrincipal() {
        super("Music Rating System");

        Config.inicializarDirectorios();

        ArtistaController controladorArtistaTemporal;
        AlbumController controladorAlbumTemporal;
        try {
            controladorArtistaTemporal = new ArtistaController();
            controladorAlbumTemporal = new AlbumController(controladorArtistaTemporal.obtenerTodos());
        } catch (IOException excepcion) {
            JOptionPane.showMessageDialog(null, "No se pudieron cargar los datos: " + excepcion.getMessage(),
                    "Error crítico", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException(excepcion);
        }
        this.artistaController = controladorArtistaTemporal;
        this.albumController = controladorAlbumTemporal;
        this.estadisticasController = new EstadisticasController(artistaController.obtenerTodos(),
                albumController.obtenerTodos());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(construirBarraSuperior(), BorderLayout.NORTH);

        this.organizadorTarjetas = new CardLayout();
        this.panelContenedor = new JPanel(organizadorTarjetas);
        this.panelContenedor.setBackground(TemaVisual.FONDO);

        this.panelBienvenida = new PanelBienvenida();
        this.panelAlbums = new PanelAlbums(this);
        this.panelAlbumDetalle = new PanelAlbumDetalle(this);
        this.panelArtistas = new PanelArtistas(this);
        this.panelArtistaDetalle = new PanelArtistaDetalle(this);
        this.panelEstadisticas = new PanelEstadisticas(this);

        panelContenedor.add(panelBienvenida, TARJETA_BIENVENIDA);
        panelContenedor.add(panelAlbums, TARJETA_ALBUMS);
        panelContenedor.add(panelAlbumDetalle, TARJETA_ALBUM_DETALLE);
        panelContenedor.add(panelArtistas, TARJETA_ARTISTAS);
        panelContenedor.add(panelArtistaDetalle, TARJETA_ARTISTA_DETALLE);
        panelContenedor.add(panelEstadisticas, TARJETA_ESTADISTICAS);

        add(panelContenedor, BorderLayout.CENTER);

        mostrarBienvenida();
    }

    private JPanel construirBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(TemaVisual.FONDO);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, TemaVisual.BORDE_ACENTO),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        JPanel panelLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelLogo.setOpaque(false);
        panelLogo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel etiquetaIcono = new JLabel(cargarIconoLogo());
        JLabel etiquetaTexto = new JLabel("LMS");
        etiquetaTexto.setFont(new Font("Serif", Font.BOLD, 26));
        etiquetaTexto.setForeground(TemaVisual.TEXTO_CLARO);

        MouseAdapter volverABienvenida = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                mostrarBienvenida();
            }
        };
        panelLogo.addMouseListener(volverABienvenida);
        etiquetaIcono.addMouseListener(volverABienvenida);
        etiquetaTexto.addMouseListener(volverABienvenida);

        panelLogo.add(etiquetaIcono);
        panelLogo.add(etiquetaTexto);
        barra.add(panelLogo, BorderLayout.WEST);

        JPanel panelNavegacion = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelNavegacion.setOpaque(false);
        BotonRedondeado botonAlbums = new BotonRedondeado("Álbumes", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        BotonRedondeado botonArtistas = new BotonRedondeado("Artistas", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        //BotonRedondeado botonEstadisticas = new BotonRedondeado("Estadísticas", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);

        botonAlbums.addActionListener(evento -> mostrarAlbums());
        botonArtistas.addActionListener(evento -> mostrarArtistas());
        //botonEstadisticas.addActionListener(evento -> mostrarEstadisticas());

        panelNavegacion.add(botonAlbums);
        panelNavegacion.add(botonArtistas);
        //panelNavegacion.add(botonEstadisticas);
        barra.add(panelNavegacion, BorderLayout.EAST);

        return barra;
    }

    private ImageIcon cargarIconoLogo() {
        ImageIcon icono = ImagenUtil.cargarEscalada(Config.RUTA_LOGO, 40, 40);
        return icono == null ? new ImageIcon() : icono;
    }

    public void mostrarBienvenida() {
        organizadorTarjetas.show(panelContenedor, TARJETA_BIENVENIDA);
    }

    public void mostrarAlbums() {
        panelAlbums.refrescar();
        organizadorTarjetas.show(panelContenedor, TARJETA_ALBUMS);
    }

    public void mostrarAlbumDetalle(int albumId) {
        panelAlbumDetalle.cargarAlbum(albumId);
        organizadorTarjetas.show(panelContenedor, TARJETA_ALBUM_DETALLE);
    }

    public void mostrarArtistas() {
        panelArtistas.refrescar();
        organizadorTarjetas.show(panelContenedor, TARJETA_ARTISTAS);
    }

    public void mostrarArtistaDetalle(int artistaId) {
        panelArtistaDetalle.cargarArtista(artistaId);
        organizadorTarjetas.show(panelContenedor, TARJETA_ARTISTA_DETALLE);
    }

    public void mostrarEstadisticas() {
        panelEstadisticas.refrescar();
        organizadorTarjetas.show(panelContenedor, TARJETA_ESTADISTICAS);
    }

    public ArtistaController getArtistaController() {
        return artistaController;
    }

    public AlbumController getAlbumController() {
        return albumController;
    }

    /*public EstadisticasController getEstadisticasController() {
        return estadisticasController;
    }*/
}