package view;

import com.formdev.flatlaf.FlatLightLaf;
import model.Projeto;
import service.ProjetoService;
import util.LogConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class TelaProjetos extends JFrame {

    private static final Color COLOR_INK = Color.decode("#18342b");
    private static final Color COLOR_CANVAS = Color.decode("#f2f6f2");
    private static final Color COLOR_SURFACE = Color.decode("#ffffff");
    private static final Color COLOR_LINE = Color.decode("#dce6df");
    private static final Color COLOR_GREEN = Color.decode("#176b51");
    private static final Color COLOR_GREEN_DARK = Color.decode("#10533f");
    private static final Color COLOR_GREEN_SOFT = Color.decode("#e7f2eb");
    private static final Color COLOR_RED = Color.decode("#ad4335");
    private static final Color COLOR_MUTED = Color.decode("#64766e");

    private JTextField campoNome;
    private JTextField campoDescricao;
    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;

    private JTextField campoBusca;
    private JComboBox<String> comboFiltroCategoria;
    private JComboBox<String> comboFiltroStatus;
    private JButton botaoLimparFiltros;

    private JTable tabela;
    private DefaultTableModel modelo;

    private JButton botaoCadastrar;
    private JButton botaoLimpar;
    private JButton botaoAlterar;
    private JButton botaoExcluir;
    private JButton botaoGraficos;

    private ProjetoService service;
    private TelaGraficos telaGraficos;
    private Timer timerAutoReload;

    public TelaProjetos() {
        inicializarTemaFlatLaf();

        service = new ProjetoService();

        setTitle("Sistema de Projetos");
        setSize(980, 740);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        criarComponentes();
        criarEventos();
        recarregarDados();
        iniciarAutoReload();
    }

    private void inicializarTemaFlatLaf() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Aumentando o raio (arc) para deixar botões, inputs e selects mais arredondados
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            
            UIManager.put("Panel.background", COLOR_CANVAS);
            UIManager.put("Component.focusColor", COLOR_GREEN);
            UIManager.put("Component.borderColor", Color.decode("#cbd8cf"));
            UIManager.put("Button.font", new Font("DM Sans", Font.BOLD, 13));
            UIManager.put("Label.font", new Font("DM Sans", Font.BOLD, 13));
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar o FlatLaf: " + e.getMessage());
        }
    }

    private void criarComponentes() {
        // Painel principal sem margens externas para colar nas bordas da janela
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(COLOR_CANVAS);

        // 1. HEADER DA PÁGINA (Ocupa toda a largura superior)
        JPanel painelHeader = new JPanel(new BorderLayout());
        painelHeader.setBackground(COLOR_GREEN_DARK);
        painelHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Color.decode("#b9d85c")),
                new EmptyBorder(22, 32, 22, 32)
        ));

        JPanel painelTitulos = new JPanel();
        painelTitulos.setLayout(new BoxLayout(painelTitulos, BoxLayout.Y_AXIS));
        painelTitulos.setBackground(COLOR_GREEN_DARK);

        JLabel lblEyebrowHeader = new JLabel("GESTÃO DE PORTFÓLIO");
        lblEyebrowHeader.setForeground(Color.decode("#c3dc89"));
        lblEyebrowHeader.setFont(new Font("DM Sans", Font.BOLD, 11));

        JLabel lblTituloHeader = new JLabel("Sistema de Projetos");
        lblTituloHeader.setForeground(Color.WHITE);
        lblTituloHeader.setFont(new Font("Space Grotesk", Font.BOLD, 28));

        painelTitulos.add(lblEyebrowHeader);
        painelTitulos.add(Box.createVerticalStrut(4));
        painelTitulos.add(lblTituloHeader);

        // Botão para abrir o Dashboard de Gráficos (D1, D2, D3)
        botaoGraficos = new JButton("📊 Ver Dashboards");
        botaoGraficos.setBackground(COLOR_GREEN);
        botaoGraficos.setForeground(Color.WHITE);
        botaoGraficos.setFont(new Font("DM Sans", Font.BOLD, 13));
        botaoGraficos.setFocusPainted(false);
        botaoGraficos.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botaoGraficos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#2aa37d"), 1),
                new EmptyBorder(8, 16, 8, 16)
        ));

        JPanel painelHeaderDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        painelHeaderDireita.setBackground(COLOR_GREEN_DARK);
        painelHeaderDireita.add(botaoGraficos);

        painelHeader.add(painelTitulos, BorderLayout.WEST);
        painelHeader.add(painelHeaderDireita, BorderLayout.EAST);

        // Painel de conteúdo interno com um respiro elegante nas laterais
        JPanel painelConteudo = new JPanel();
        painelConteudo.setLayout(new BoxLayout(painelConteudo, BoxLayout.Y_AXIS));
        painelConteudo.setBackground(COLOR_CANVAS);
        painelConteudo.setBorder(new EmptyBorder(24, 28, 28, 28));

        // 2. PAINEL DO FORMULÁRIO DE CADASTRO
        JPanel painelFormularioCard = new JPanel(new BorderLayout());
        painelFormularioCard.setBackground(COLOR_SURFACE);
        painelFormularioCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, COLOR_GREEN),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_LINE, 1),
                        new EmptyBorder(22, 26, 20, 26)
                )
        ));

        JPanel painelFormHeading = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        painelFormHeading.setBackground(COLOR_SURFACE);
        painelFormHeading.setBorder(new EmptyBorder(0, 0, 18, 0));

        JPanel tituloBox = new JPanel();
        tituloBox.setLayout(new BoxLayout(tituloBox, BoxLayout.Y_AXIS));
        tituloBox.setBackground(COLOR_SURFACE);

        JLabel lblEyebrowForm = new JLabel("CADASTRO");
        lblEyebrowForm.setForeground(COLOR_MUTED);
        lblEyebrowForm.setFont(new Font("DM Sans", Font.BOLD, 11));

        JLabel lblTituloForm = new JLabel("Novo projeto");
        lblTituloForm.setForeground(COLOR_INK);
        lblTituloForm.setFont(new Font("Space Grotesk", Font.BOLD, 20));

        tituloBox.add(lblEyebrowForm);
        tituloBox.add(Box.createVerticalStrut(3));
        tituloBox.add(lblTituloForm);
        painelFormHeading.add(tituloBox);

        JPanel gridCampos = new JPanel(new GridLayout(2, 2, 18, 16));
        gridCampos.setBackground(COLOR_SURFACE);

        campoNome = criarCampoTexto();
        campoDescricao = criarCampoTexto();

        comboCategoria = new JComboBox<>(new String[]{"Web", "Software", "Mobile", "Desktop", "Outros"});
        comboStatus = new JComboBox<>(new String[]{"Planejado", "Em andamento", "Concluído", "Cancelado"});

        gridCampos.add(criarBlocoCampo("Nome do projeto", campoNome));
        gridCampos.add(criarBlocoCampo("Descrição", campoDescricao));
        gridCampos.add(criarBlocoCampo("Categoria", comboCategoria));
        gridCampos.add(criarBlocoCampo("Status", comboStatus));

        JPanel painelAcoes = new JPanel(new BorderLayout());
        painelAcoes.setBackground(COLOR_SURFACE);
        painelAcoes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.decode("#e8eee9")),
                new EmptyBorder(16, 0, 0, 0)
        ));

        botaoLimpar = criarBotaoQuiet("Limpar formulário");

        JPanel grupoBotoesDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        grupoBotoesDireita.setBackground(COLOR_SURFACE);

        botaoCadastrar = criarBotaoPrimario("Cadastrar projeto");
        botaoAlterar = criarBotaoSecundario("Alterar projeto");
        botaoExcluir = criarBotaoPerigo("Excluir projeto");

        grupoBotoesDireita.add(botaoCadastrar);
        grupoBotoesDireita.add(botaoAlterar);
        grupoBotoesDireita.add(botaoExcluir);

        painelAcoes.add(botaoLimpar, BorderLayout.WEST);
        painelAcoes.add(grupoBotoesDireita, BorderLayout.EAST);

        painelFormularioCard.add(painelFormHeading, BorderLayout.NORTH);
        painelFormularioCard.add(gridCampos, BorderLayout.CENTER);
        painelFormularioCard.add(painelAcoes, BorderLayout.SOUTH);

        // 3. SEÇÃO DA TABELA COM FILTROS E BUSCA
        JPanel painelTabelaSecao = new JPanel(new BorderLayout());
        painelTabelaSecao.setBackground(COLOR_CANVAS);
        painelTabelaSecao.setBorder(new EmptyBorder(28, 0, 0, 0));

        JPanel painelTabelaHeading = new JPanel(new BorderLayout());
        painelTabelaHeading.setBackground(COLOR_CANVAS);
        painelTabelaHeading.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel tabelaTituloBox = new JPanel();
        tabelaTituloBox.setLayout(new BoxLayout(tabelaTituloBox, BoxLayout.Y_AXIS));
        tabelaTituloBox.setBackground(COLOR_CANVAS);

        JLabel lblEyebrowTabela = new JLabel("VISÃO GERAL");
        lblEyebrowTabela.setForeground(COLOR_MUTED);
        lblEyebrowTabela.setFont(new Font("DM Sans", Font.BOLD, 11));

        JLabel lblTituloTabela = new JLabel("Projetos cadastrados");
        lblTituloTabela.setForeground(COLOR_INK);
        lblTituloTabela.setFont(new Font("Space Grotesk", Font.BOLD, 20));

        tabelaTituloBox.add(lblEyebrowTabela);
        tabelaTituloBox.add(Box.createVerticalStrut(3));
        tabelaTituloBox.add(lblTituloTabela);
        painelTabelaHeading.add(tabelaTituloBox, BorderLayout.WEST);

        // Painel de Filtros e Busca
        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        painelFiltros.setBackground(COLOR_CANVAS);

        campoBusca = new JTextField(15);
        campoBusca.putClientProperty("JTextField.placeholderText", "🔍 Pesquisar projeto...");
        campoBusca.setFont(new Font("DM Sans", Font.PLAIN, 13));

        comboFiltroCategoria = new JComboBox<>(new String[]{"Todas Categorias", "Web", "Software", "Mobile", "Desktop", "Outros"});
        comboFiltroStatus = new JComboBox<>(new String[]{"Todos Status", "Planejado", "Em andamento", "Concluído", "Cancelado"});
        
        botaoLimparFiltros = criarBotaoSecundario("Limpar Filtros");

        painelFiltros.add(campoBusca);
        painelFiltros.add(comboFiltroCategoria);
        painelFiltros.add(comboFiltroStatus);
        painelFiltros.add(botaoLimparFiltros);

        painelTabelaHeading.add(painelFiltros, BorderLayout.EAST);

        // Tabela Estilizada
        modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        modelo.addColumn("ID");
        modelo.addColumn("Nome do Projeto");
        modelo.addColumn("Descrição");
        modelo.addColumn("Categoria");
        modelo.addColumn("Status");

        tabela = new JTable(modelo);
        tabela.setRowHeight(38);
        tabela.setFont(new Font("DM Sans", Font.PLAIN, 14));
        tabela.setForeground(COLOR_INK);
        tabela.setSelectionBackground(COLOR_GREEN_SOFT);
        tabela.setSelectionForeground(COLOR_INK);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(Color.decode("#e9efea"));

        tabela.getTableHeader().setBackground(Color.decode("#edf4ef"));
        tabela.getTableHeader().setForeground(Color.decode("#51665b"));
        tabela.getTableHeader().setFont(new Font("DM Sans", Font.BOLD, 11));
        tabela.getTableHeader().setPreferredSize(new Dimension(0, 40));

        JScrollPane scrollPane = new JScrollPane(tabela);
        scrollPane.getViewport().setBackground(COLOR_SURFACE);
        scrollPane.setBorder(BorderFactory.createLineBorder(COLOR_LINE, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        JPanel tableFrame = new JPanel(new BorderLayout());
        tableFrame.setBackground(COLOR_SURFACE);
        tableFrame.add(scrollPane);

        painelTabelaSecao.add(painelTabelaHeading, BorderLayout.NORTH);
        painelTabelaSecao.add(tableFrame, BorderLayout.CENTER);

        painelConteudo.add(painelFormularioCard);
        painelConteudo.add(painelTabelaSecao);

        JScrollPane scrollPanePrincipal = new JScrollPane(painelConteudo);
        scrollPanePrincipal.getVerticalScrollBar().setUnitIncrement(20);
        scrollPanePrincipal.getHorizontalScrollBar().setUnitIncrement(20);
        scrollPanePrincipal.setBorder(null);

        painelPrincipal.add(painelHeader, BorderLayout.NORTH);
        painelPrincipal.add(scrollPanePrincipal, BorderLayout.CENTER);

        add(painelPrincipal);
    }

    private JPanel criarBlocoCampo(String labelTexto, JComponent componente) {
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(COLOR_SURFACE);

        JLabel label = new JLabel(labelTexto.toUpperCase());
        label.setForeground(Color.decode("#344b41"));
        label.setFont(new Font("DM Sans", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        componente.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        componente.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);

        painel.add(label);
        painel.add(Box.createVerticalStrut(6));
        painel.add(componente);

        return painel;
    }

    private JTextField criarCampoTexto() {
        JTextField campo = new JTextField();
        campo.setBackground(Color.decode("#fbfdfb"));
        campo.setFont(new Font("DM Sans", Font.PLAIN, 14));
        campo.setForeground(COLOR_INK);
        return campo;
    }

    private JButton criarBotaoPrimario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(COLOR_GREEN);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton criarBotaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(COLOR_SURFACE);
        btn.setForeground(Color.decode("#345347"));
        btn.setBorder(BorderFactory.createLineBorder(Color.decode("#cbd8cf"), 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton criarBotaoQuiet(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(COLOR_SURFACE);
        btn.setForeground(Color.decode("#345347"));
        btn.setBorder(BorderFactory.createLineBorder(Color.decode("#cbd8cf"), 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton criarBotaoPerigo(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(Color.decode("#fffafa"));
        btn.setForeground(COLOR_RED);
        btn.setBorder(BorderFactory.createLineBorder(Color.decode("#e9c9c4"), 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void criarEventos() {
        botaoGraficos.addActionListener(e -> abrirTelaGraficos());
        botaoLimpar.addActionListener(e -> limparFormulario());
        botaoCadastrar.addActionListener(e -> cadastrar());
        botaoAlterar.addActionListener(e -> alterarSelecionadoTabela());
        botaoExcluir.addActionListener(e -> excluir());

        campoBusca.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { aplicarFiltros(); }
            public void removeUpdate(DocumentEvent e) { aplicarFiltros(); }
            public void changedUpdate(DocumentEvent e) { aplicarFiltros(); }
        });

        comboFiltroCategoria.addActionListener(e -> aplicarFiltros());
        comboFiltroStatus.addActionListener(e -> aplicarFiltros());

        botaoLimparFiltros.addActionListener(e -> {
            campoBusca.setText("");
            comboFiltroCategoria.setSelectedIndex(0);
            comboFiltroStatus.setSelectedIndex(0);
        });

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabela.getSelectedRow() != -1) {
                int linha = tabela.getSelectedRow();
                String nome = (String) tabela.getValueAt(linha, 1);
                String descricao = (String) tabela.getValueAt(linha, 2);
                String categoria = (String) tabela.getValueAt(linha, 3);
                String status = (String) tabela.getValueAt(linha, 4);

                campoNome.setText(nome);
                campoDescricao.setText(descricao);
                comboCategoria.setSelectedItem(categoria);
                comboStatus.setSelectedItem(status);
            }
        });
    }

    public void recarregarDados() {
        try {
            service.carregarProjetos();
        } catch (Exception e) {
            System.err.println("Erro ao recarregar projetos: " + e.getMessage());
        }
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String textoBusca = campoBusca != null ? campoBusca.getText().toLowerCase().trim() : "";
        String catSelecionada = comboFiltroCategoria != null ? (String) comboFiltroCategoria.getSelectedItem() : "Todas Categorias";
        String statusSelecionado = comboFiltroStatus != null ? (String) comboFiltroStatus.getSelectedItem() : "Todos Status";

        List<Projeto> todos = service.listarProjetos();
        if (todos == null) {
            carregarTabela(List.of());
            return;
        }

        List<Projeto> projetosFiltrados = todos.stream().filter(p -> {
            boolean matchBusca = textoBusca.isEmpty() || 
                                 (p.getNome() != null && p.getNome().toLowerCase().contains(textoBusca)) || 
                                 (p.getDescricao() != null && p.getDescricao().toLowerCase().contains(textoBusca));
            
            boolean matchCat = correspondeCategoria(p.getCategoria(), catSelecionada);
            boolean matchStatus = correspondeStatus(p.getStatus(), statusSelecionado);

            return matchBusca && matchCat && matchStatus;
        }).collect(Collectors.toList());

        carregarTabela(projetosFiltrados);
    }

    private boolean correspondeCategoria(String catProjeto, String catFiltro) {
        if (catFiltro == null || "Todas Categorias".equalsIgnoreCase(catFiltro)) return true;
        if (catProjeto == null) return false;
        String cp = catProjeto.trim().toLowerCase();
        String cf = catFiltro.trim().toLowerCase();
        if (cp.equals(cf)) return true;
        if (cf.contains("desk") && cp.contains("desk")) return true;
        if (cf.contains("soft") && cp.contains("soft")) return true;
        if (cf.contains("web") && cp.contains("web")) return true;
        if (cf.contains("mobi") && cp.contains("mobi")) return true;
        if (cf.contains("outro") && cp.contains("outro")) return true;
        return false;
    }

    private boolean correspondeStatus(String statusProjeto, String statusFiltro) {
        if (statusFiltro == null || "Todos Status".equalsIgnoreCase(statusFiltro)) return true;
        if (statusProjeto == null) return false;
        String sp = statusProjeto.trim().toLowerCase();
        String sf = statusFiltro.trim().toLowerCase();
        if (sp.equals(sf)) return true;
        if (sf.contains("conclu") && sp.contains("conclu")) return true;
        if (sf.contains("cancela") && sp.contains("cancela")) return true;
        if ((sf.contains("andamento") || sf.contains("desenv")) && (sp.contains("andamento") || sp.contains("desenv") || sp.contains("ativo"))) return true;
        if (sf.contains("planeja") && sp.contains("planeja")) return true;
        return false;
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoDescricao.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        tabela.clearSelection();
        campoNome.requestFocus();

        recarregarDados();
    }

    private void cadastrar() {
        String nome = campoNome.getText();
        String descricao = campoDescricao.getText();
        String categoria = comboCategoria.getSelectedItem().toString();
        String status = comboStatus.getSelectedItem().toString();

        if (nome.isBlank()) {
            LogConfig.error("CAMPO_INVALIDO", "Tentativa de cadastro com nome em branco na tela Swing", null);
            JOptionPane.showMessageDialog(this, "Informe o nome.");
            return;
        }

        Projeto projeto = new Projeto(nome, descricao, categoria, status);
        service.adicionarProjeto(projeto);

        try {
            service.salvar();
            LogConfig.audit("CRIADO", projeto.getId(),
                    String.format("Nome='%s', Categoria='%s', Status='%s' (via GUI Swing)",
                            projeto.getNome(), projeto.getCategoria(), projeto.getStatus()));
            JOptionPane.showMessageDialog(this, "Projeto cadastrado com sucesso!");
            limparFormulario();
            atualizarTelaGraficosSeAberta();
        } catch (Exception e) {
            LogConfig.error("PERSISTENCIA", "Erro ao salvar novo projeto na tela Swing: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void alterarSelecionadoTabela() {
        int linha = tabela.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um projeto da tabela.");
            return;
        }

        String nome = campoNome.getText();
        String descricao = campoDescricao.getText();
        String categoria = comboCategoria.getSelectedItem().toString();
        String status = comboStatus.getSelectedItem().toString();

        if (nome.isBlank() || descricao.isBlank()) {
            LogConfig.error("CAMPO_INVALIDO", "Preenchimento incompleto ao alterar projeto via tela Swing", null);
            JOptionPane.showMessageDialog(this, "Preencha o nome e a descrição.");
            return;
        }

        int id = (Integer) tabela.getValueAt(linha, 0);
        Projeto projeto = new Projeto(nome, descricao, categoria, status);
        boolean alterou = service.alterarProjeto(id, projeto);

        if (!alterou) {
            LogConfig.error("CAMPO_INVALIDO", "Projeto ID " + id + " não encontrado para alteração na tela Swing", null);
            JOptionPane.showMessageDialog(this, "Não foi possível alterar o projeto.");
            return;
        }

        try {
            service.salvar();
            LogConfig.audit("ALTERADO", id,
                    String.format("Nome='%s', Categoria='%s', Status='%s' (via GUI Swing)",
                            projeto.getNome(), projeto.getCategoria(), projeto.getStatus()));
            JOptionPane.showMessageDialog(this, "Projeto alterado com sucesso.");
            limparFormulario();
            atualizarTelaGraficosSeAberta();
        } catch (Exception a) {
            LogConfig.error("PERSISTENCIA", "Erro ao alterar projeto ID " + id + " via tela Swing: " + a.getMessage(), a);
            JOptionPane.showMessageDialog(this, "Erro ao alterar: " + a.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluir() {
        int linha = tabela.getSelectedRow();
        int idExcluir = -1;

        if (linha != -1) {
            idExcluir = (Integer) tabela.getValueAt(linha, 0);
        } else {
            String input = JOptionPane.showInputDialog(this, "Digite o ID do projeto que deseja excluir:");
            if (input == null || input.isBlank()) return;
            try {
                idExcluir = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                LogConfig.error("CAMPO_INVALIDO", "ID inválido informado para exclusão na tela Swing: " + input, null);
                JOptionPane.showMessageDialog(this, "ID inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Projeto projeto = service.buscarPorId(idExcluir);
        if (projeto == null) {
            LogConfig.error("CAMPO_INVALIDO", "Projeto ID " + idExcluir + " não localizado para exclusão na tela Swing", null);
            JOptionPane.showMessageDialog(this, "ID não encontrado!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(this, 
                "Deseja realmente excluir o projeto: " + projeto.getNome() + "?", 
                "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            service.removerPorId(idExcluir);

            try {
                service.salvar();
                LogConfig.audit("EXCLUIDO", idExcluir, "Nome='" + projeto.getNome() + "' (via GUI Swing)");
                JOptionPane.showMessageDialog(this, "Projeto excluído com sucesso!");
                limparFormulario();
                atualizarTelaGraficosSeAberta();
            } catch (Exception e) {
                LogConfig.error("PERSISTENCIA", "Erro ao excluir projeto ID " + idExcluir + " via tela Swing: " + e.getMessage(), e);
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarTabela(List<Projeto> lista) {
        modelo.setRowCount(0);

        if (lista != null) {
            for (Projeto projeto : lista) {
                modelo.addRow(new Object[]{
                        projeto.getId(),
                        projeto.getNome(),
                        projeto.getDescricao(),
                        projeto.getCategoria(),
                        projeto.getStatus()
                });
            }
        }
    }

    private void iniciarAutoReload() {
        // Recarrega automaticamente a cada 10 segundos preservando a seleção do usuário
        timerAutoReload = new Timer(10000, e -> {
            int linhaSel = tabela.getSelectedRow();
            recarregarDados();
            if (linhaSel != -1 && linhaSel < tabela.getRowCount()) {
                tabela.setRowSelectionInterval(linhaSel, linhaSel);
            }
            atualizarTelaGraficosSeAberta();
        });
        timerAutoReload.setRepeats(true);
        timerAutoReload.start();
    }

    private void abrirTelaGraficos() {
        if (telaGraficos == null || !telaGraficos.isDisplayable()) {
            telaGraficos = new TelaGraficos(this.service);
        }
        telaGraficos.setVisible(true);
        telaGraficos.toFront();
        telaGraficos.atualizarGraficos();
    }

    private void atualizarTelaGraficosSeAberta() {
        if (telaGraficos != null && telaGraficos.isDisplayable() && telaGraficos.isVisible()) {
            telaGraficos.atualizarGraficos();
        }
    }
}