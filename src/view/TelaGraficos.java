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
 * Tela de Gráficos e Dashboards Analíticos da aplicação.
 * Implementa rigorosamente os requisitos:
 * - D1 (Visão Geral): Cards com Total, Concluídos, Ativos, Cancelados.
 * - D2 (Status): Gráfico de Pizza (Concluído %, Em Desenv. %, Planejado %, Cancelado %).
 * - D3 (Categoria): Gráfico de Barras (Web, Software, Mobile, Desktop).
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
    private JLabel lblProjetosAtivos;
    private JLabel lblProjetosCancelados;
    private JLabel lblUltimaAtualizacao;

    private PainelGraficoPizza graficoStatus;
    private PainelGraficoBarras graficoCategorias;

    public TelaGraficos(ProjetoService service) {
        this.service = service != null ? service : new ProjetoService();

        inicializarTema();

        setTitle("Dashboards de Projetos — D1, D2, D3");
        setSize(1000, 720);
        setMinimumSize(new Dimension(860, 600));
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

        JLabel lblEyebrow = new JLabel("DASHBOARDS ANALÍTICOS");
        lblEyebrow.setForeground(Color.decode("#c3dc89"));
        lblEyebrow.setFont(new Font("DM Sans", Font.BOLD, 11));

        JLabel lblTitulo = new JLabel("Visão Geral e Estatísticas dos Projetos");
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setFont(new Font("Space Grotesk", Font.BOLD, 26));

        headerTitulos.add(lblEyebrow);
        headerTitulos.add(Box.createVerticalStrut(3));
        headerTitulos.add(lblTitulo);

        JPanel headerAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerAcoes.setBackground(COLOR_GREEN_DARK);

        JButton btnAtualizar = new JButton("🔄 Atualizar Dados");
        btnAtualizar.setBackground(COLOR_GREEN);
        btnAtualizar.setForeground(Color.WHITE);
        btnAtualizar.setFont(new Font("DM Sans", Font.BOLD, 13));
        btnAtualizar.setFocusPainted(false);
        btnAtualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAtualizar.addActionListener(e -> atualizarGraficos());

        lblUltimaAtualizacao = new JLabel("Atualizado agora");
        lblUltimaAtualizacao.setForeground(Color.decode("#a8cfbe"));
        lblUltimaAtualizacao.setFont(new Font("DM Sans", Font.PLAIN, 12));

        headerAcoes.add(lblUltimaAtualizacao);
        headerAcoes.add(Box.createHorizontalStrut(10));
        headerAcoes.add(btnAtualizar);

        painelHeader.add(headerTitulos, BorderLayout.WEST);
        painelHeader.add(headerAcoes, BorderLayout.EAST);

        // 2. CORPO PRINCIPAL COM SCROLL
        JPanel painelCorpo = new JPanel();
        painelCorpo.setLayout(new BoxLayout(painelCorpo, BoxLayout.Y_AXIS));
        painelCorpo.setBackground(COLOR_CANVAS);
        painelCorpo.setBorder(new EmptyBorder(24, 32, 28, 32));

        // D1: CARDS DE KPI (TOTAL, CONCLUÍDOS, ATIVOS, CANCELADOS)
        JPanel painelKPIs = new JPanel(new GridLayout(1, 4, 18, 0));
        painelKPIs.setBackground(COLOR_CANVAS);
        painelKPIs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        lblTotalProjetos = new JLabel("0");
        lblProjetosConcluidos = new JLabel("0");
        lblProjetosAtivos = new JLabel("0");
        lblProjetosCancelados = new JLabel("0");

        painelKPIs.add(criarCardKPI("D1 • TOTAL", lblTotalProjetos, Color.decode("#2563eb")));
        painelKPIs.add(criarCardKPI("D1 • CONCLUÍDOS", lblProjetosConcluidos, Color.decode("#10b981")));
        painelKPIs.add(criarCardKPI("D1 • ATIVOS", lblProjetosAtivos, Color.decode("#f59e0b")));
        painelKPIs.add(criarCardKPI("D1 • CANCELADOS", lblProjetosCancelados, Color.decode("#ef4444")));

        painelCorpo.add(painelKPIs);
        painelCorpo.add(Box.createVerticalStrut(24));

        // D2 e D3: GRÁFICO DE PIZZA (STATUS) E GRÁFICO DE BARRAS (CATEGORIA)
        JPanel painelGraficosGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        painelGraficosGrid.setBackground(COLOR_CANVAS);

        graficoStatus = new PainelGraficoPizza("D2 • Distribuição por Status (Pizza)");
        graficoCategorias = new PainelGraficoBarras("D3 • Distribuição por Categoria (Barras)");

        painelGraficosGrid.add(graficoStatus);
        painelGraficosGrid.add(graficoCategorias);

        painelCorpo.add(painelGraficosGrid);

        JScrollPane scrollPane = new JScrollPane(painelCorpo);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        painelRaiz.add(painelHeader, BorderLayout.NORTH);
        painelRaiz.add(scrollPane, BorderLayout.CENTER);

        add(painelRaiz);
    }

    private JPanel criarCardKPI(String rotulo, JLabel labelValor, Color corDestaque) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_LINE, 1),
                        BorderFactory.createMatteBorder(3, 0, 0, 0, corDestaque)
                ),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel lblRotulo = new JLabel(rotulo);
        lblRotulo.setFont(new Font("DM Sans", Font.BOLD, 11));
        lblRotulo.setForeground(COLOR_MUTED);

        labelValor.setFont(new Font("Space Grotesk", Font.BOLD, 26));
        labelValor.setForeground(COLOR_INK);

        card.add(lblRotulo, BorderLayout.NORTH);
        card.add(labelValor, BorderLayout.CENTER);

        return card;
    }

    public void atualizarGraficos() {
        try {
            service.carregarProjetos();
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao recarregar projetos: " + e.getMessage());
        }

        List<Projeto> projetos = service.listarProjetos();
        if (projetos == null) projetos = new ArrayList<>();

        int total = projetos.size();
        int concluidos = 0;
        int ativos = 0;
        int cancelados = 0;
        int planejados = 0;

        // D3: Contadores por Categoria (Web, Software, Mobile, Desktop)
        Map<String, Integer> dadosCategorias = new LinkedHashMap<>();
        dadosCategorias.put("Web", 0);
        dadosCategorias.put("Software", 0);
        dadosCategorias.put("Mobile", 0);
        dadosCategorias.put("Desktop", 0);

        // D2: Contadores por Status (Concluído, Em Desenv., Planejado, Cancelado)
        Map<String, Integer> dadosStatus = new LinkedHashMap<>();
        dadosStatus.put("Concluído", 0);
        dadosStatus.put("Em Desenv.", 0);
        dadosStatus.put("Planejado", 0);
        dadosStatus.put("Cancelado", 0);

        for (Projeto p : projetos) {
            // Processamento de Status
            String s = p.getStatus() != null ? p.getStatus().trim().toLowerCase() : "";
            if (s.contains("conclu")) {
                concluidos++;
                dadosStatus.put("Concluído", dadosStatus.get("Concluído") + 1);
            } else if (s.contains("cancela")) {
                cancelados++;
                dadosStatus.put("Cancelado", dadosStatus.get("Cancelado") + 1);
            } else if (s.contains("andamento") || s.contains("desenv") || s.contains("ativo")) {
                ativos++;
                dadosStatus.put("Em Desenv.", dadosStatus.get("Em Desenv.") + 1);
            } else {
                planejados++;
                dadosStatus.put("Planejado", dadosStatus.get("Planejado") + 1);
            }

            // Processamento de Categoria
            String c = p.getCategoria() != null ? p.getCategoria().trim().toLowerCase() : "";
            if (c.contains("web")) {
                dadosCategorias.put("Web", dadosCategorias.get("Web") + 1);
            } else if (c.contains("desk")) {
                dadosCategorias.put("Desktop", dadosCategorias.get("Desktop") + 1);
            } else if (c.contains("mobi")) {
                dadosCategorias.put("Mobile", dadosCategorias.get("Mobile") + 1);
            } else if (c.contains("soft")) {
                dadosCategorias.put("Software", dadosCategorias.get("Software") + 1);
            } else {
                dadosCategorias.put("Software", dadosCategorias.get("Software") + 1);
            }
        }

        // Atualizar D1: Cards de KPI
        lblTotalProjetos.setText(String.valueOf(total));
        lblProjetosConcluidos.setText(String.valueOf(concluidos));
        lblProjetosAtivos.setText(String.valueOf(ativos));
        lblProjetosCancelados.setText(String.valueOf(cancelados));

        // Cores personalizadas para D2 (Status - Pizza)
        Map<String, Color> coresStatus = new HashMap<>();
        coresStatus.put("concluído", Color.decode("#10b981"));
        coresStatus.put("em desenv.", Color.decode("#3b82f6"));
        coresStatus.put("planejado", Color.decode("#8b5cf6"));
        coresStatus.put("cancelado", Color.decode("#ef4444"));

        // Cores personalizadas para D3 (Categoria - Barras)
        Map<String, Color> coresCategorias = new HashMap<>();
        coresCategorias.put("web", Color.decode("#2563eb"));
        coresCategorias.put("software", Color.decode("#176b51"));
        coresCategorias.put("mobile", Color.decode("#7c3aed"));
        coresCategorias.put("desktop", Color.decode("#0284c7"));

        // Atualizar gráficos
        graficoStatus.setDados(dadosStatus, coresStatus);
        graficoCategorias.setDados(dadosCategorias, coresCategorias);

        lblUltimaAtualizacao.setText("Atualizado às " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }
}
