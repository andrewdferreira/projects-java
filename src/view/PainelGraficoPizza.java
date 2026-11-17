package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Componente gráfico Swing personalizado para desenhar Gráficos de Pizza / Donut.
 * Totalmente renderizado com Java 21 Graphics2D com antialiasing de alta qualidade,
 * suporte a temas modernos e legenda detalhada com contagem e percentuais.
 */
public class PainelGraficoPizza extends JPanel {

    private String titulo;
    private Map<String, Integer> dados;
    private Map<String, Color> mapaCores;
    private Color corFundo = Color.decode("#ffffff");
    private Color corBorda = Color.decode("#dce6df");
    private Color corTextoTitulo = Color.decode("#18342b");
    private Color corTextoSecundario = Color.decode("#64766e");

    private static final Color[] PALETA_PADRAO = new Color[]{
            Color.decode("#2563eb"), // Azul (Web)
            Color.decode("#176b51"), // Verde (Software)
            Color.decode("#7c3aed"), // Roxo (Mobile)
            Color.decode("#f59e0b"), // Laranja/Âmbar (Outros)
            Color.decode("#10b981"), // Verde Esmeralda
            Color.decode("#ec4899"), // Rosa
            Color.decode("#06b6d4"), // Ciano
            Color.decode("#e11d48"), // Vermelho
            Color.decode("#84cc16"), // Verde Lima
            Color.decode("#64748b")  // Ardósia / Cinza
    };

    // Padrão Fixo e Imutável de Cores para as Categorias do Sistema
    private static final Map<String, Color> CORES_PADRAO_CATEGORIAS = new LinkedHashMap<>();
    static {
        CORES_PADRAO_CATEGORIAS.put("web", Color.decode("#2563eb"));      // Azul
        CORES_PADRAO_CATEGORIAS.put("software", Color.decode("#176b51")); // Verde Corporativo
        CORES_PADRAO_CATEGORIAS.put("desktop", Color.decode("#176b51"));  // Software
        CORES_PADRAO_CATEGORIAS.put("mobile", Color.decode("#7c3aed"));   // Roxo / Violeta
        CORES_PADRAO_CATEGORIAS.put("outros", Color.decode("#f59e0b"));   // Laranja / Âmbar
        CORES_PADRAO_CATEGORIAS.put("outro", Color.decode("#f59e0b"));
    }

    public PainelGraficoPizza(String titulo) {
        this.titulo = titulo;
        this.dados = new LinkedHashMap<>();
        this.mapaCores = new LinkedHashMap<>();
        setBackground(corFundo);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(corBorda, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
    }

    public void setDados(Map<String, Integer> novosDados, Map<String, Color> coresCustomizadas) {
        this.dados = novosDados != null ? new LinkedHashMap<>(novosDados) : new LinkedHashMap<>();
        this.mapaCores = coresCustomizadas != null ? new LinkedHashMap<>(coresCustomizadas) : new LinkedHashMap<>();
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

        // 1. TÍTULO DO GRÁFICO
        g2.setColor(corTextoTitulo);
        g2.setFont(new Font("Space Grotesk", Font.BOLD, 16));
        FontMetrics fmTitulo = g2.getFontMetrics();
        g2.drawString(titulo, 16, 24);

        int total = dados.values().stream().mapToInt(Integer::intValue).sum();

        // Caso sem dados
        if (total == 0 || dados.isEmpty()) {
            desenharEstadoVazio(g2, largura, altura);
            g2.dispose();
            return;
        }

        // 2. DIMENSÕES E ÁREA DO GRÁFICO DE PIZZA
        int espacoSuperior = 44;
        int alturaLegenda = Math.min(140, Math.max(70, dados.size() * 24));
        int espacoGraficoDisponivel = altura - espacoSuperior - alturaLegenda;

        int diametro = Math.max(80, Math.min(largura - 40, espacoGraficoDisponivel - 20));
        int xCentro = largura / 2;
        int yCentro = espacoSuperior + (espacoGraficoDisponivel / 2);
        int x = xCentro - (diametro / 2);
        int y = yCentro - (diametro / 2);

        // 3. DESENHO DAS FATIAS DA PIZZA / DONUT
        double anguloAtual = 90.0; // Começar no topo
        int corIndex = 0;

        for (Map.Entry<String, Integer> entry : dados.entrySet()) {
            if (entry.getValue() <= 0) continue;

            double proporcao = (double) entry.getValue() / total;
            double anguloExtensao = proporcao * 360.0;

            Color corFatia = obterCor(entry.getKey(), corIndex++);

            // Desenhar arco externo
            g2.setColor(corFatia);
            g2.fill(new Arc2D.Double(x, y, diametro, diametro, anguloAtual, anguloExtensao, Arc2D.PIE));

            // Linha de divisão elegante
            g2.setColor(corFundo);
            g2.setStroke(new BasicStroke(2.0f));
            g2.draw(new Arc2D.Double(x, y, diametro, diametro, anguloAtual, anguloExtensao, Arc2D.PIE));

            anguloAtual += anguloExtensao;
        }

        // 4. EFEITO DONUT (Círculo interno vazado para modernidade)
        int diametroInterno = (int) (diametro * 0.52);
        int xInterno = xCentro - (diametroInterno / 2);
        int yInterno = yCentro - (diametroInterno / 2);

        g2.setColor(corFundo);
        g2.fillOval(xInterno, yInterno, diametroInterno, diametroInterno);
        g2.setColor(corBorda);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawOval(xInterno, yInterno, diametroInterno, diametroInterno);

        // Texto no centro do Donut (Total)
        g2.setColor(corTextoSecundario);
        g2.setFont(new Font("DM Sans", Font.BOLD, 10));
        FontMetrics fmLabel = g2.getFontMetrics();
        String rotuloTotal = "TOTAL";
        g2.drawString(rotuloTotal, xCentro - (fmLabel.stringWidth(rotuloTotal) / 2), yCentro - 4);

        g2.setColor(corTextoTitulo);
        g2.setFont(new Font("Space Grotesk", Font.BOLD, 18));
        FontMetrics fmValor = g2.getFontMetrics();
        String valorTotal = String.valueOf(total);
        g2.drawString(valorTotal, xCentro - (fmValor.stringWidth(valorTotal) / 2), yCentro + 16);

        // 5. LEGENDA COM CORES, NOMES E PERCENTUAIS
        int yLegenda = altura - alturaLegenda + 8;
        int colunas = largura > 380 ? 2 : 1;
        int larguraColuna = (largura - 32) / colunas;

        int idx = 0;
        int itemIndex = 0;
        for (Map.Entry<String, Integer> entry : dados.entrySet()) {
            double percentual = ((double) entry.getValue() / total) * 100.0;
            Color cor = obterCor(entry.getKey(), itemIndex++);

            int col = idx % colunas;
            int lin = idx / colunas;

            int itemX = 16 + (col * larguraColuna);
            int itemY = yLegenda + (lin * 22);

            // Marcador de cor arredondado
            g2.setColor(cor);
            g2.fillRoundRect(itemX, itemY - 10, 12, 12, 4, 4);

            // Nome da categoria/status
            g2.setColor(corTextoTitulo);
            g2.setFont(new Font("DM Sans", Font.BOLD, 12));
            String textoChave = entry.getKey();
            if (textoChave.length() > 14) {
                textoChave = textoChave.substring(0, 12) + "...";
            }
            g2.drawString(textoChave, itemX + 18, itemY);

            // Quantidade e percentual
            g2.setColor(corTextoSecundario);
            g2.setFont(new Font("DM Sans", Font.PLAIN, 12));
            String detalhe = String.format(" %d (%.1f%%)", entry.getValue(), percentual);
            g2.drawString(detalhe, itemX + 18 + g2.getFontMetrics(new Font("DM Sans", Font.BOLD, 12)).stringWidth(textoChave), itemY);

            idx++;
        }

        g2.dispose();
    }

    private Color obterCor(String chave, int indice) {
        if (chave == null) return PALETA_PADRAO[indice % PALETA_PADRAO.length];
        String normalizada = chave.trim().toLowerCase();

        // 1. Prioriza cor explicitamente mapeada
        if (mapaCores.containsKey(normalizada)) {
            return mapaCores.get(normalizada);
        }

        // 2. Mantém o padrão de cores para as categorias do sistema
        for (Map.Entry<String, Color> entry : CORES_PADRAO_CATEGORIAS.entrySet()) {
            if (normalizada.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return PALETA_PADRAO[indice % PALETA_PADRAO.length];
    }

    private void desenharEstadoVazio(Graphics2D g2, int largura, int altura) {
        int xCentro = largura / 2;
        int yCentro = (altura / 2) + 10;
        int diametro = Math.min(largura, altura) / 3;

        g2.setColor(Color.decode("#edf2ee"));
        g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6}, 0));
        g2.drawOval(xCentro - (diametro / 2), yCentro - (diametro / 2), diametro, diametro);

        g2.setColor(corTextoSecundario);
        g2.setFont(new Font("DM Sans", Font.PLAIN, 13));
        String msg = "Nenhum dado cadastrado";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, xCentro - (fm.stringWidth(msg) / 2), yCentro + 5);
    }
}

