package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Componente gráfico Swing personalizado para desenhar Gráfico de Barras (D3).
 * Renderizado com Graphics2D, antialiasing de alta resolução,
 * exibindo valores e rótulos (ex: Web: 35, Software: 20, Mobile: 15, Desktop: 10).
 */
public class PainelGraficoBarras extends JPanel {

    private String titulo;
    private Map<String, Integer> dados;
    private Map<String, Color> mapaCores;
    private Color corFundo = Color.decode("#ffffff");
    private Color corBorda = Color.decode("#dce6df");
    private Color corTextoTitulo = Color.decode("#18342b");
    private Color corTextoSecundario = Color.decode("#64766e");

    private static final Map<String, Color> CORES_CATEGORIAS = new LinkedHashMap<>();
    static {
        CORES_CATEGORIAS.put("web", Color.decode("#2563eb"));      // Azul
        CORES_CATEGORIAS.put("software", Color.decode("#176b51")); // Verde escuro
        CORES_CATEGORIAS.put("mobile", Color.decode("#7c3aed"));   // Roxo
        CORES_CATEGORIAS.put("desktop", Color.decode("#0284c7"));  // Azul Celeste
        CORES_CATEGORIAS.put("outros", Color.decode("#f59e0b"));   // Âmbar
    }

    public PainelGraficoBarras(String titulo) {
        this.titulo = titulo;
        this.dados = new LinkedHashMap<>();
        this.mapaCores = new LinkedHashMap<>(CORES_CATEGORIAS);
        setBackground(corFundo);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corBorda, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
    }

    public void setDados(Map<String, Integer> novosDados, Map<String, Color> coresCustomizadas) {
        this.dados = novosDados != null ? new LinkedHashMap<>(novosDados) : new LinkedHashMap<>();
        if (coresCustomizadas != null) {
            this.mapaCores.putAll(coresCustomizadas);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int largura = getWidth();
        int altura = getHeight();

        // 1. TÍTULO
        g2.setColor(corTextoTitulo);
        g2.setFont(new Font("Space Grotesk", Font.BOLD, 16));
        g2.drawString(titulo, 16, 26);

        int total = dados.values().stream().mapToInt(Integer::intValue).sum();

        if (total == 0 || dados.isEmpty()) {
            desenharEstadoVazio(g2, largura, altura);
            g2.dispose();
            return;
        }

        int maxValor = dados.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        if (maxValor == 0) maxValor = 1;

        int topoY = 50;
        int areaUtilAltura = altura - topoY - 20;
        int numBarras = dados.size();
        int espacamento = 14;
        int alturaBarra = Math.max(22, Math.min(38, (areaUtilAltura - (numBarras - 1) * espacamento) / numBarras));

        int xLabel = 16;
        int larguraLabel = 110;
        int xInicioBarra = xLabel + larguraLabel + 10;
        int larguraMaximaBarra = largura - xInicioBarra - 60; // margem para valor numérico no final

        int yAtual = topoY;

        for (Map.Entry<String, Integer> entry : dados.entrySet()) {
            String categoria = entry.getKey();
            int valor = entry.getValue();

            Color cor = mapaCores.getOrDefault(categoria.toLowerCase(), Color.decode("#2563eb"));

            // 1. Label da Categoria
            g2.setColor(corTextoTitulo);
            g2.setFont(new Font("DM Sans", Font.BOLD, 13));
            g2.drawString(categoria, xLabel, yAtual + alturaBarra / 2 + 5);

            // 2. Fundo da Barra (cinza claro suave)
            g2.setColor(Color.decode("#f1f5f3"));
            g2.fill(new RoundRectangle2D.Float(xInicioBarra, yAtual, larguraMaximaBarra, alturaBarra, 8, 8));

            // 3. Barra Preenchida
            int larguraPreenchida = (int) Math.round(((double) valor / maxValor) * larguraMaximaBarra);
            if (valor > 0 && larguraPreenchida < 10) larguraPreenchida = 10;

            if (larguraPreenchida > 0) {
                g2.setColor(cor);
                g2.fill(new RoundRectangle2D.Float(xInicioBarra, yAtual, larguraPreenchida, alturaBarra, 8, 8));
            }

            // 4. Valor Numérico
            g2.setColor(corTextoTitulo);
            g2.setFont(new Font("DM Sans", Font.BOLD, 13));
            String textoValor = String.valueOf(valor);
            g2.drawString(textoValor, xInicioBarra + larguraMaximaBarra + 12, yAtual + alturaBarra / 2 + 5);

            yAtual += alturaBarra + espacamento;
        }

        g2.dispose();
    }

    private void desenharEstadoVazio(Graphics2D g2, int largura, int altura) {
        g2.setColor(corTextoSecundario);
        g2.setFont(new Font("DM Sans", Font.PLAIN, 13));
        String msg = "Nenhum dado cadastrado para exibir";
        FontMetrics fm = g2.getFontMetrics();
        int x = (largura - fm.stringWidth(msg)) / 2;
        int y = altura / 2;
        g2.drawString(msg, x, y);
    }
}

