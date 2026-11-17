package view;

import com.formdev.flatlaf.FlatLightLaf;
import model.Projeto;
import service.ProjetoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Tela de Gráficos e Dashboard Estatístico da aplicação.
 * Exibe gráficos de pizza/donut para distribuição de projetos por Categoria e por Status,
 * acompanhados de cards de métricas (KPIs) e suporte a atualização em tempo real.
 */
public class TelaGraficos extends JFrame {

    private static final Color COLOR_INK = Color.decode("#18342b");
    private static final Color COLOR_CANVAS = Color.decode("#f2f6f2");
    private static final Color COLOR_SURFACE = Color.decode("#ffffff");
    private static final Color COLOR_LINE = Color.decode("#dce6df");
    private static final Color COLOR_GREEN = Color.decode("#176b51");
    private static final Color COLOR_GREEN_DARK = Color.decode("#10533f");
    private static final Color COLOR_MUTED = Color.decode("#64766e");

    private ProjetoService service;

    private JLabel lblTotalProjetos;
    private JLabel lblProjetosConcluidos;
    private JLabel lblProjetosAndamento;
    private JLabel lblProjetosPlanejados;
    private JLabel lblUltimaAtualizacao;

    private PainelGraficoPizza graficoCategorias;
    private PainelGraficoPizza graficoStatus;

    public TelaGraficos(ProjetoService service) {
        this.service = service != null ? service : new ProjetoService();

        inicializarTema();

        setTitle("Dashboard de Gráficos — Sistema de Projetos");
        setSize(980, 720);
        setMinimumSize(new Dimension(840, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        criarComponentes();
        atualizarGraficos();
    }

    private void inicializarTema() {
        try {
            if (!(UIManager.getLookAndFeel() instanceof FlatLightLaf)) {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }
        } catch (Exception ignored) {
        }
    }

    private void criarComponentes() {
        JPanel painelRaiz = new JPanel(new BorderLayout());
        painelRaiz.setBackground(COLOR_CANVAS);

        // 1. HEADER SUPERIOR
        JPanel painelHeader = new JPanel(new BorderLayout());
        painelHeader.setBackground(COLOR_GREEN_DARK);
        painelHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Color.decode("#b9d85c")),
                new EmptyBorder(20, 32, 20, 32)
        ));

        JPanel headerTitulos = new JPanel();
        headerTitulos.setLayout(new BoxLayout(headerTitulos, BoxLayout.Y_AXIS));
        headerTitulos.setBackground(COLOR_GREEN_DARK);

        JLabel lblEyebrow = new JLabel("DASHBOARD ANALÍTICO");
        lblEyebrow.setForeground(Color.decode("#c3dc89"));
        lblEyebrow.setFont(new Font("DM Sans", Font.BOLD, 11));

        JLabel lblTitulo = new JLabel("Gráficos de Projetos");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Space Grotesk", Font.BOLD, 26));

        headerTitulos.add(lblEyebrow);
        headerTitulos.add(Box.createVerticalStrut(3));
        headerTitulos.add(lblTitulo);

        // Botões de ação no Header
        JPanel headerAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerAcoes.setBackground(COLOR_GREEN_DARK);

        JButton btnAtualizar = new JButton("🔄 Atualizar Dados");
        btnAtualizar.setBackground(COLOR_GREEN);
        btnAtualizar.setForeground(Color.WHITE);
        btnAtualizar.setFont(new Font("DM Sans", Font.BOLD, 13));
        btnAtualizar.setFocusPainted(false);
        btnAtualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAtualizar.addActionListener(e -> atualizarGraficos());

        JButton btnFechar = new JButton("Fechar");
        btnFechar.setBackground(Color.decode("#1f6750"));
        btnFechar.setForeground(Color.WHITE);
        btnFechar.setFont(new Font("DM Sans", Font.PLAIN, 13));
        btnFechar.setFocusPainted(false);
        btnFechar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFechar.addActionListener(e -> dispose());

        headerAcoes.add(btnAtualizar);
        headerAcoes.add(btnFechar);

        painelHeader.add(headerTitulos, BorderLayout.WEST);
        painelHeader.add(headerAcoes, BorderLayout.EAST);

        // 2. CONTEÚDO PRINCIPAL (Scrollável)
        JPanel painelConteudo = new JPanel();
        painelConteudo.setLayout(new BoxLayout(painelConteudo, BoxLayout.Y_AXIS));
        painelConteudo.setBackground(COLOR_CANVAS);
        painelConteudo.setBorder(new EmptyBorder(22, 28, 24, 28));

        // 2.1 CARDS DE KPI (Métricas resumidas)
        JPanel painelCards = new JPanel(new GridLayout(1, 4, 16, 0));
        painelCards.setBackground(COLOR_CANVAS);
        painelCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        painelCards.setPreferredSize(new Dimension(800, 95));

        lblTotalProjetos = new JLabel("0");
        lblProjetosConcluidos = new JLabel("0");
        lblProjetosAndamento = new JLabel("0");
        lblProjetosPlanejados = new JLabel("0");

        painelCards.add(criarCardMetrica("TOTAL DE PROJETOS", lblTotalProjetos, Color.decode("#2563eb")));
        painelCards.add(criarCardMetrica("CONCLUÍDOS", lblProjetosConcluidos, Color.decode("#10b981")));
        painelCards.add(criarCardMetrica("EM ANDAMENTO", lblProjetosAndamento, Color.decode("#f59e0b")));
        painelCards.add(criarCardMetrica("PLANEJADOS", lblProjetosPlanejados, Color.decode("#8b5cf6")));

        painelConteudo.add(painelCards);
        painelConteudo.add(Box.createVerticalStrut(20));

        // 2.2 ÁREA DOS GRÁFICOS DE PIZZA (Lado a Lado)
        JPanel painelGraficos = new JPanel(new GridLayout(1, 2, 18, 0));
        painelGraficos.setBackground(COLOR_CANVAS);
        painelGraficos.setPreferredSize(new Dimension(800, 420));
        painelGraficos.setMinimumSize(new Dimension(600, 380));

        graficoCategorias = new PainelGraficoPizza("Distribuição por Categoria");
        graficoStatus = new PainelGraficoPizza("Distribuição por Status");

        painelGraficos.add(graficoCategorias);
        painelGraficos.add(graficoStatus);

        painelConteudo.add(painelGraficos);
        painelConteudo.add(Box.createVerticalStrut(14));

        // 2.3 RODAPÉ COM STATUS E HORA
        JPanel painelRodape = new JPanel(new BorderLayout());
        painelRodape.setBackground(COLOR_CANVAS);
        lblUltimaAtualizacao = new JLabel("Última sincronização: --:--:--");
        lblUltimaAtualizacao.setForeground(COLOR_MUTED);
        lblUltimaAtualizacao.setFont(new Font("DM Sans", Font.ITALIC, 11));
        painelRodape.add(lblUltimaAtualizacao, BorderLayout.EAST);

        painelConteudo.add(painelRodape);

        JScrollPane scrollPane = new JScrollPane(painelConteudo);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setBorder(null);

        painelRaiz.add(painelHeader, BorderLayout.NORTH);
        painelRaiz.add(scrollPane, BorderLayout.CENTER);

        add(painelRaiz);
    }

    private JPanel criarCardMetrica(String titulo, JLabel lblValor, Color corDestaque) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, corDestaque),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_LINE, 1),
                        new EmptyBorder(12, 16, 12, 16)
                )
        ));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(COLOR_MUTED);
        lblTitulo.setFont(new Font("DM Sans", Font.BOLD, 10));

        lblValor.setForeground(COLOR_INK);
        lblValor.setFont(new Font("Space Grotesk", Font.BOLD, 26));

        card.add(lblTitulo, BorderLayout.NORTH);
        card.add(lblValor, BorderLayout.CENTER);

        return card;
    }

    /**
     * Recarrega os dados do serviço e recalcula as distribuições e os gráficos de pizza.
     */
    public void atualizarGraficos() {
        try {
            service.carregarProjetos();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }

        List<Projeto> projetos = service.listarProjetos();

        Map<String, Integer> contagemCategorias = new LinkedHashMap<>();
        Map<String, Integer> contagemStatus = new LinkedHashMap<>();

        int concluidos = 0;
        int andamento = 0;
        int planejados = 0;

        for (Projeto p : projetos) {
            // Normalizar categoria
            String cat = normalizarCategoria(p.getCategoria());
            contagemCategorias.put(cat, contagemCategorias.getOrDefault(cat, 0) + 1);

            // Normalizar status
            String st = normalizarStatus(p.getStatus());
            contagemStatus.put(st, contagemStatus.getOrDefault(st, 0) + 1);

            if ("Concluído".equalsIgnoreCase(st)) {
                concluidos++;
            } else if ("Em andamento".equalsIgnoreCase(st)) {
                andamento++;
            } else if ("Planejado".equalsIgnoreCase(st)) {
                planejados++;
            }
        }

        // Atualizar KPIs
        int total = projetos.size();
        lblTotalProjetos.setText(String.valueOf(total));
        lblProjetosConcluidos.setText(String.format("%d (%s)", concluidos, total > 0 ? (concluidos * 100 / total) + "%" : "0%"));
        lblProjetosAndamento.setText(String.valueOf(andamento));
        lblProjetosPlanejados.setText(String.valueOf(planejados));

        // Configurar cores específicas para cada status
        Map<String, Color> coresStatus = new HashMap<>();
        coresStatus.put("concluído", Color.decode("#10b981"));    // Verde Esmeralda
        coresStatus.put("concluido", Color.decode("#10b981"));
        coresStatus.put("em andamento", Color.decode("#3b82f6")); // Azul vibrante
        coresStatus.put("planejado", Color.decode("#8b5cf6"));    // Roxo elegante

        // Configurar cores específicas para categorias (Padrão rigoroso)
        Map<String, Color> coresCategorias = new LinkedHashMap<>();
        coresCategorias.put("web", Color.decode("#2563eb"));      // Azul
        coresCategorias.put("software", Color.decode("#176b51")); // Verde
        coresCategorias.put("desktop", Color.decode("#176b51"));
        coresCategorias.put("mobile", Color.decode("#7c3aed"));   // Violeta / Roxo
        coresCategorias.put("outros", Color.decode("#f59e0b"));   // Âmbar / Laranja
        coresCategorias.put("outro", Color.decode("#f59e0b"));

        graficoCategorias.setDados(contagemCategorias, coresCategorias);
        graficoStatus.setDados(contagemStatus, coresStatus);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        lblUltimaAtualizacao.setText("Última sincronização: " + sdf.format(new Date()));
    }

    private String normalizarCategoria(String cat) {
        if (cat == null || cat.isBlank()) return "Outros";
        String c = cat.trim().toLowerCase();
        if (c.contains("web")) return "Web";
        if (c.contains("mobile") || c.contains("celular")) return "Mobile";
        if (c.contains("software") || c.contains("desktop")) return "Software";
        if (c.contains("outro")) return "Outros";
        return Character.toUpperCase(c.charAt(0)) + c.substring(1);
    }

    private String normalizarStatus(String st) {
        if (st == null || st.isBlank()) return "Planejado";
        String s = st.trim().toLowerCase();
        if (s.contains("conclu") || s.contains("feito") || s.contains("pronto")) return "Concluído";
        if (s.contains("andamento") || s.contains("desenvolvimento")) return "Em andamento";
        if (s.contains("planeja")) return "Planejado";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}

