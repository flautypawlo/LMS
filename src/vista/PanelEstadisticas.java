/*package vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import controlador.EstadisticasController;
import modelo.Album;
import modelo.Artista;
import modelo.Cancion;

public class PanelEstadisticas extends JPanel {

    private final VentanaPrincipal ventanaPrincipal;
    private final JLabel etiquetaMejorCancion;
    private final JLabel etiquetaPeorCancion;
    private final JLabel etiquetaMejorAlbum;
    private final JLabel etiquetaPeorAlbum;
    private final JLabel etiquetaArtistaMejorValorado;
    private final JLabel etiquetaTotalArtistas;
    private final JLabel etiquetaTotalAlbumes;
    private final JLabel etiquetaTotalCanciones;

    public PanelEstadisticas(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
        this.etiquetaMejorCancion = crearEtiqueta();
        this.etiquetaPeorCancion = crearEtiqueta();
        this.etiquetaMejorAlbum = crearEtiqueta();
        this.etiquetaPeorAlbum = crearEtiqueta();
        this.etiquetaArtistaMejorValorado = crearEtiqueta();
        this.etiquetaTotalArtistas = crearEtiqueta();
        this.etiquetaTotalAlbumes = crearEtiqueta();
        this.etiquetaTotalCanciones = crearEtiqueta();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel etiquetaTitulo = new JLabel("Estadísticas");
        etiquetaTitulo.setFont(etiquetaTitulo.getFont().deriveFont(Font.BOLD, 22f));
        add(etiquetaTitulo, BorderLayout.NORTH);

        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.add(etiquetaMejorCancion);
        panelContenido.add(etiquetaPeorCancion);
        panelContenido.add(etiquetaMejorAlbum);
        panelContenido.add(etiquetaPeorAlbum);
        panelContenido.add(etiquetaArtistaMejorValorado);
        panelContenido.add(etiquetaTotalArtistas);
        panelContenido.add(etiquetaTotalAlbumes);
        panelContenido.add(etiquetaTotalCanciones);
        add(panelContenido, BorderLayout.CENTER);

        JButton botonActualizar = new JButton("Actualizar");
        botonActualizar.addActionListener(evento -> refrescar());
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelInferior.add(botonActualizar);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JLabel crearEtiqueta() {
        JLabel etiqueta = new JLabel();
        etiqueta.setFont(etiqueta.getFont().deriveFont(15f));
        etiqueta.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        return etiqueta;
    }

    public void refrescar() {
        EstadisticasController controlador = ventanaPrincipal.getEstadisticasController();
        controlador.actualizar(ventanaPrincipal.getArtistaController().obtenerTodos(),
                ventanaPrincipal.getAlbumController().obtenerTodos());

        Cancion mejorCancion = controlador.getMejorCancion();
        Cancion peorCancion = controlador.getPeorCancion();
        Album mejorAlbum = controlador.getMejorAlbum();
        Album peorAlbum = controlador.getPeorAlbum();
        Artista artistaMejorValorado = controlador.getArtistaMejorValorado();

        etiquetaMejorCancion.setText("Mejor canción: "
                + (mejorCancion == null ? "Sin datos" : mejorCancion.getNombre() + " (" + mejorCancion.getNotaTexto() + ")"));
        etiquetaPeorCancion.setText("Peor canción: "
                + (peorCancion == null ? "Sin datos" : peorCancion.getNombre() + " (" + peorCancion.getNotaTexto() + ")"));
        etiquetaMejorAlbum.setText("Mejor álbum: "
                + (mejorAlbum == null ? "Sin datos" : mejorAlbum.getNombre() + " (" + mejorAlbum.getNotaPromedioTexto() + ")"));
        etiquetaPeorAlbum.setText("Peor álbum: "
                + (peorAlbum == null ? "Sin datos" : peorAlbum.getNombre() + " (" + peorAlbum.getNotaPromedioTexto() + ")"));
        etiquetaArtistaMejorValorado.setText(
                "Artista mejor valorado: " + (artistaMejorValorado == null ? "Sin datos" : artistaMejorValorado.getNombre()));
        etiquetaTotalArtistas.setText("Cantidad total de artistas: " + controlador.getCantidadArtistas());
        etiquetaTotalAlbumes.setText("Cantidad total de álbumes: " + controlador.getCantidadAlbumes());
        etiquetaTotalCanciones.setText("Cantidad total de canciones: " + controlador.getCantidadCanciones());
    }
}*/