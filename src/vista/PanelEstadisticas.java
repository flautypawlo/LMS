package vista;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import controlador.EstadisticasController;
import controlador.EstadisticasController.ArtistaConNota;
import controlador.EstadisticasController.CancionConContexto;
import modelo.Album;
import modelo.Cancion;

public class PanelEstadisticas extends JPanel {

    private final VentanaPrincipal ventanaPrincipal;
    private final PanelDesplazable panelTopAlbumes;
    private final PanelDesplazable panelTopArtistas;
    private final PanelDesplazable panelTopCanciones;

    public PanelEstadisticas(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
        this.panelTopAlbumes = new PanelDesplazable();
        this.panelTopArtistas = new PanelDesplazable();
        this.panelTopCanciones = new PanelDesplazable();

        setLayout(new BorderLayout());
        setBackground(TemaVisual.FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelSecciones = new JPanel(new GridLayout(3, 1, 0, 10));
        panelSecciones.setOpaque(false);
        panelSecciones.add(construirSeccion("Top 10 Albums", panelTopAlbumes));
        panelSecciones.add(construirSeccion("Top 10 Artistas", panelTopArtistas));
        panelSecciones.add(construirSeccion("Top 50 Canciones", panelTopCanciones));
        add(panelSecciones, BorderLayout.CENTER);
    }

    private JComponent construirSeccion(String titulo, PanelDesplazable panelLista) {
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

    public void refrescar() {
        EstadisticasController controlador = ventanaPrincipal.getEstadisticasController();
        controlador.actualizar(ventanaPrincipal.getArtistaController().obtenerTodos(),
                ventanaPrincipal.getAlbumController().obtenerTodos());

        panelTopAlbumes.removeAll();
        List<Album> topAlbumes = controlador.obtenerTopAlbumes(10);
        if (topAlbumes.isEmpty()) {
            panelTopAlbumes.add(construirFilaSinDatos());
        } else {
            for (int i = 0; i < topAlbumes.size(); i++) {
                panelTopAlbumes.add(construirFilaAlbum(i + 1, topAlbumes.get(i)));
            }
        }
        panelTopAlbumes.revalidate();
        panelTopAlbumes.repaint();

        panelTopArtistas.removeAll();
        List<ArtistaConNota> topArtistas = controlador.obtenerTopArtistas(10);
        if (topArtistas.isEmpty()) {
            panelTopArtistas.add(construirFilaSinDatos());
        } else {
            for (int i = 0; i < topArtistas.size(); i++) {
                panelTopArtistas.add(construirFilaArtista(i + 1, topArtistas.get(i)));
            }
        }
        panelTopArtistas.revalidate();
        panelTopArtistas.repaint();

        panelTopCanciones.removeAll();
        List<CancionConContexto> topCanciones = controlador.obtenerTopCanciones(50);
        if (topCanciones.isEmpty()) {
            panelTopCanciones.add(construirFilaSinDatos());
        } else {
            for (int i = 0; i < topCanciones.size(); i++) {
                panelTopCanciones.add(construirFilaCancion(i + 1, topCanciones.get(i)));
            }
        }
        panelTopCanciones.revalidate();
        panelTopCanciones.repaint();
    }

    private JComponent construirFilaSinDatos() {
        JLabel etiqueta = new JLabel("Todavía no hay datos calificados.");
        etiqueta.setFont(new Font("Serif", Font.BOLD, 16));
        etiqueta.setForeground(TemaVisual.TEXTO_SECUNDARIO);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(15, 12, 15, 12));
        return etiqueta;
    }

    private JComponent construirFilaAlbum(int posicion, Album album) {
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 8));

        PanelRedondeado fila = new PanelRedondeado(TemaVisual.FONDO_FILA, TemaVisual.BORDE_ACENTO, 18);
        fila.setLayout(new BorderLayout(12, 0));
        fila.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 16));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel etiquetaPosicion = new JLabel("#" + posicion);
        etiquetaPosicion.setFont(new Font("SansSerif", Font.BOLD, 24));
        etiquetaPosicion.setForeground(TemaVisual.TEXTO_CLARO);
        fila.add(etiquetaPosicion, BorderLayout.WEST);

        JLabel etiquetaPortada = new JLabel(ImagenUtil.cargarEscalada(album.getRutaPortada(), 45, 45));
        etiquetaPortada.setPreferredSize(new Dimension(45, 45));

        JPanel panelTextos = new JPanel();
        panelTextos.setOpaque(false);
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        JLabel etiquetaNombre = new JLabel(album.getNombre());
        etiquetaNombre.setFont(new Font("Serif", Font.BOLD, 18));
        etiquetaNombre.setForeground(TemaVisual.TEXTO_CLARO);
        String nombreArtista = album.obtenerNombreArtistaParaMostrar();
        JLabel etiquetaArtista = new JLabel(nombreArtista == null ? "Artista desconocido" : nombreArtista);
        etiquetaArtista.setFont(new Font("Serif", Font.BOLD, 11));
        etiquetaArtista.setForeground(TemaVisual.TEXTO_SECUNDARIO);
        JLabel etiquetaAnio = new JLabel(String.valueOf(album.getAnioLanzamiento()));
        etiquetaAnio.setFont(new Font("Serif", Font.BOLD, 11));
        etiquetaAnio.setForeground(TemaVisual.TEXTO_SECUNDARIO);
        panelTextos.add(etiquetaNombre);
        panelTextos.add(etiquetaArtista);
        panelTextos.add(etiquetaAnio);

        JPanel panelPortadaTexto = new JPanel(new BorderLayout(12, 0));
        panelPortadaTexto.setOpaque(false);
        panelPortadaTexto.add(etiquetaPortada, BorderLayout.WEST);
        panelPortadaTexto.add(panelTextos, BorderLayout.CENTER);
        fila.add(panelPortadaTexto, BorderLayout.CENTER);

        JPanel panelNota = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelNota.setOpaque(false);
        JLabel etiquetaNotaTexto = new JLabel("Nota:");
        etiquetaNotaTexto.setFont(new Font("Serif", Font.BOLD, 16));
        etiquetaNotaTexto.setForeground(TemaVisual.TEXTO_CLARO);
        PanelRedondeado badge = new PanelRedondeado(TemaVisual.FONDO_BADGE, TemaVisual.BORDE_ACENTO, 10);
        badge.setLayout(new BorderLayout());
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        JLabel etiquetaNotaValor = new JLabel(album.getNotaPromedioTexto());
        etiquetaNotaValor.setFont(new Font("Serif", Font.BOLD, 16));
        etiquetaNotaValor.setForeground(TemaVisual.TEXTO_CLARO);
        badge.add(etiquetaNotaValor, BorderLayout.CENTER);
        panelNota.add(etiquetaNotaTexto);
        panelNota.add(badge);
        fila.add(panelNota, BorderLayout.EAST);

        fila.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2) {
                    ventanaPrincipal.mostrarAlbumDetalle(album.getId());
                }
            }
        });

        envoltorio.add(fila, BorderLayout.CENTER);
        return envoltorio;
    }

    private JComponent construirFilaArtista(int posicion, ArtistaConNota entrada) {
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 12));

        String texto = "#" + posicion + " " + entrada.getArtista().getNombre() + " - Nota: "
                + String.format("%.2f", entrada.getNota());
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Serif", Font.BOLD, 26));
        etiqueta.setForeground(TemaVisual.TEXTO_CLARO);
        etiqueta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        etiqueta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2) {
                    ventanaPrincipal.mostrarArtistaDetalle(entrada.getArtista().getId());
                }
            }
        });

        envoltorio.add(etiqueta, BorderLayout.CENTER);
        return envoltorio;
    }

    private JComponent construirFilaCancion(int posicion, CancionConContexto entrada) {
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 12));

        Cancion cancion = entrada.getCancion();
        Album album = entrada.getAlbum();
        String nombreArtista = album.obtenerNombreArtistaParaMostrar();
        if (nombreArtista == null) {
            nombreArtista = "Artista desconocido";
        }

        String texto = "#" + posicion + " " + cancion.getNombre() + " (" + cancion.getDuracionFormateada() + ") - "
                + nombreArtista + " - Nota: " + cancion.getNotaTexto();
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font("Serif", Font.BOLD, 17));
        etiqueta.setForeground(TemaVisual.TEXTO_CLARO);
        etiqueta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        etiqueta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2) {
                    ventanaPrincipal.mostrarAlbumDetalle(album.getId());
                }
            }
        });

        envoltorio.add(etiqueta, BorderLayout.CENTER);
        return envoltorio;
    }
}