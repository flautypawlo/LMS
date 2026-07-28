package vista;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import modelo.Album;
import modelo.Artista;
import modelo.Banda;
import modelo.Solista;

public class PanelArtistaDetalle extends JPanel {

    private final VentanaPrincipal ventanaPrincipal;

    private final JLabel etiquetaNombre;
    private final JLabel etiquetaTipoDatos;
    private final JLabel etiquetaCantidad;
    private final PanelDesplazable panelAlbumes;

    public PanelArtistaDetalle(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;

        this.etiquetaNombre = new JLabel();
        this.etiquetaNombre.setFont(new Font("Serif", Font.BOLD, 32));
        this.etiquetaNombre.setForeground(TemaVisual.TEXTO_CLARO);

        this.etiquetaTipoDatos = new JLabel();
        this.etiquetaTipoDatos.setFont(new Font("Serif", Font.BOLD, 14));
        this.etiquetaTipoDatos.setForeground(TemaVisual.TEXTO_SECUNDARIO);

        this.etiquetaCantidad = new JLabel();
        this.etiquetaCantidad.setFont(new Font("Serif", Font.BOLD, 14));
        this.etiquetaCantidad.setForeground(TemaVisual.TEXTO_SECUNDARIO);

        this.panelAlbumes = new PanelDesplazable();

        setLayout(new BorderLayout(0, 15));
        setBackground(TemaVisual.FONDO);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel panelEncabezado = new JPanel();
        panelEncabezado.setOpaque(false);
        panelEncabezado.setLayout(new BoxLayout(panelEncabezado, BoxLayout.Y_AXIS));
        panelEncabezado.add(etiquetaNombre);
        panelEncabezado.add(etiquetaTipoDatos);
        panelEncabezado.add(etiquetaCantidad);
        add(panelEncabezado, BorderLayout.NORTH);

        PanelRedondeado tarjetaAlbumes = new PanelRedondeado(TemaVisual.FONDO_TARJETA, TemaVisual.BORDE_ACENTO, 22);
        tarjetaAlbumes.setLayout(new BorderLayout());
        tarjetaAlbumes.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        panelAlbumes.setBackground(TemaVisual.FONDO_TARJETA);

        JScrollPane scroll = new JScrollPane(panelAlbumes);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(TemaVisual.FONDO_TARJETA);
        scroll.getVerticalScrollBar().setUI(new BarraDesplazamientoRedondeada());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(16, 0));
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        tarjetaAlbumes.add(scroll, BorderLayout.CENTER);

        add(tarjetaAlbumes, BorderLayout.CENTER);

        BotonRedondeado botonVolver = new BotonRedondeado("Volver a Artistas", TemaVisual.BOTON_FONDO, TemaVisual.BOTON_TEXTO);
        botonVolver.addActionListener(evento -> ventanaPrincipal.mostrarArtistas());
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelInferior.setOpaque(false);
        panelInferior.add(botonVolver);
        add(panelInferior, BorderLayout.SOUTH);
    }

    public void cargarArtista(int artistaId) {
        Artista artista = ventanaPrincipal.getArtistaController().buscarPorId(artistaId);
        if (artista == null) {
            return;
        }
        etiquetaNombre.setText(artista.getNombre());

        if (artista instanceof Solista) {
            Solista solista = (Solista) artista;
            String fallecimiento = solista.getFechaFallecimiento() == null ? ""
                    : " — Falleció: " + solista.getFechaFallecimiento();
            etiquetaTipoDatos.setText(
                    "Solista / Nació: " + solista.getFechaNacimiento() + " en " + solista.getPaisNacimiento() + fallecimiento);
        } else if (artista instanceof Banda) {
            Banda banda = (Banda) artista;
            etiquetaTipoDatos.setText("Banda / Integrantes: " + String.join(", ", banda.getIntegrantes()));
        }

        List<Album> albumesDelArtista = ventanaPrincipal.getAlbumController().obtenerPorArtista(artistaId);
        int totalCanciones = 0;
        for (Album album : albumesDelArtista) {
            totalCanciones += album.getCantidadCanciones();
        }
        etiquetaCantidad.setText(
                "Cantidad de Álbumes: " + albumesDelArtista.size() + " / Cantidad de Canciones: " + totalCanciones);

        panelAlbumes.removeAll();
        for (Album album : albumesDelArtista) {
            panelAlbumes.add(construirFilaAlbum(album));
        }
        panelAlbumes.revalidate();
        panelAlbumes.repaint();
    }

    private JComponent construirFilaAlbum(Album album) {
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.setBorder(BorderFactory.createEmptyBorder(6, 2, 6, 8));

        PanelRedondeado fila = new PanelRedondeado(TemaVisual.FONDO_FILA, TemaVisual.BORDE_ACENTO, 20);
        fila.setLayout(new BorderLayout(15, 0));
        fila.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 20));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel etiquetaPortada = new JLabel(ImagenUtil.cargarEscalada(album.getRutaPortada(), 55, 55));
        etiquetaPortada.setPreferredSize(new Dimension(55, 55));
        fila.add(etiquetaPortada, BorderLayout.WEST);

        JPanel panelTextos = new JPanel();
        panelTextos.setOpaque(false);
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));

        JLabel etiquetaNombreAlbum = new JLabel(album.getNombre());
        etiquetaNombreAlbum.setFont(new Font("Serif", Font.BOLD, 22));
        etiquetaNombreAlbum.setForeground(TemaVisual.TEXTO_CLARO);

        JLabel etiquetaArtista = new JLabel(album.getArtista().getNombre());
        etiquetaArtista.setFont(new Font("Serif", Font.BOLD, 13));
        etiquetaArtista.setForeground(TemaVisual.TEXTO_SECUNDARIO);

        JLabel etiquetaAnio = new JLabel(String.valueOf(album.getAnioLanzamiento()));
        etiquetaAnio.setFont(new Font("Serif", Font.BOLD, 13));
        etiquetaAnio.setForeground(TemaVisual.TEXTO_SECUNDARIO);

        panelTextos.add(etiquetaNombreAlbum);
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

        MouseAdapter irADetalle = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2) {
                    ventanaPrincipal.mostrarAlbumDetalle(album.getId());
                }
            }
        };
        fila.addMouseListener(irADetalle);
        etiquetaNombreAlbum.addMouseListener(irADetalle);
        etiquetaArtista.addMouseListener(irADetalle);
        etiquetaAnio.addMouseListener(irADetalle);

        envoltorio.add(fila, BorderLayout.CENTER);
        return envoltorio;
    }
}