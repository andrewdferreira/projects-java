package view;

import com.formdev.flatlaf.FlatLightLaf;
import model.Projeto;
import service.ProjetoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class TelaProjetos extends JFrame {

    // Cores extraídas diretamente do seu CSS (:root)
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

    // Novos componentes de Pesquisa e Filtro
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

    private ProjetoService service;

    public TelaProjetos() {
        // Inicializa o tema FlatLaf e aplica as propriedades globais idênticas ao CSS
        inicializarTemaFlatLaf();

        service = new ProjetoService();

        setTitle("Sistema de Projetos");
        setSize(940, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        criarComponentes();
        criarEventos();
        carregarTabela(service.listarProjetos());
    }

    private void inicializarTemaFlatLaf() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Customizações globais para respeitar o border-radius e cores do seu projeto
            UIManager.put("Button.arc", 6);
            UIManager.put("Component.arc", 6);
            UIManager.put("TextComponent.arc", 6);
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
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(COLOR_CANVAS);
        painelPrincipal.setBorder(new EmptyBorder(24, 32, 32, 32));

        // 1. HEADER DA PÁGINA (.page-header)
        JPanel painelHeader = new JPanel();
        painelHeader.setLayout(new BoxLayout(painelHeader, BoxLayout.Y_AXIS));
        painelHeader.setBackground(COLOR_GREEN_DARK);
        painelHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Color.decode("#b9d85c")),
                new EmptyBorder(24, 28, 26, 28)
        ));

        JLabel lblEyebrowHeader = new JLabel("GESTÃO DE PORTFÓLIO");
        lblEyebrowHeader.setForeground(Color.decode("#c3dc89"));
        lblEyebrowHeader.setFont(new Font("DM Sans", Font.BOLD, 11));

        JLabel lblTituloHeader = new JLabel("Sistema de Projetos");
        lblTituloHeader.setForeground(Color.WHITE);
        lblTituloHeader.setFont(new Font("Space Grotesk", Font.BOLD, 28));

        painelHeader.add(lblEyebrowHeader);
        painelHeader.add(Box.createVerticalStrut(4));
        painelHeader.add(lblTituloHeader);

        // 2. PAINEL DO FORMULÁRIO DE CADASTRO (.form-panel)
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

        comboCategoria = new JComboBox<>(new String[]{"Web", "Software", "Mobile", "Outros"});
        comboStatus = new JComboBox<>(new String[]{"Planejado", "Em andamento", "Concluído"});

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

        // --- PAINEL DE BUSCA E FILTROS ---
        JPanel painelFiltros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        painelFiltros.setBackground(COLOR_CANVAS);

        campoBusca = new JTextField(15);
        campoBusca.putClientProperty("JTextField.placeholderText", "🔍 Pesquisar projeto...");
        campoBusca.setFont(new Font("DM Sans", Font.PLAIN, 13));

        comboFiltroCategoria = new JComboBox<>(new String[]{"Todas Categorias", "Web", "Software", "Mobile", "Outros"});
        comboFiltroStatus = new JComboBox<>(new String[]{"Todos Status", "Planejado", "Em andamento", "Concluído"});
        
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

        JPanel tableFrame = new JPanel(new BorderLayout());
        tableFrame.setBackground(COLOR_SURFACE);
        tableFrame.add(scrollPane);

        painelTabelaSecao.add(painelTabelaHeading, BorderLayout.NORTH);
        painelTabelaSecao.add(tableFrame, BorderLayout.CENTER);

        JPanel painelConteudoCentral = new JPanel();
        painelConteudoCentral.setLayout(new BoxLayout(painelConteudoCentral, BoxLayout.Y_AXIS));
        painelConteudoCentral.setBackground(COLOR_CANVAS);
        painelConteudoCentral.add(painelFormularioCard);
        painelConteudoCentral.add(painelTabelaSecao);

        painelPrincipal.add(painelHeader, BorderLayout.NORTH);
        painelPrincipal.add(new JScrollPane(painelConteudoCentral), BorderLayout.CENTER);

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
        botaoLimpar.addActionListener(e -> limparFormulario());
        botaoCadastrar.addActionListener(e -> cadastrar());
        botaoAlterar.addActionListener(e -> alterarSelecionadoTabela());
        botaoExcluir.addActionListener(e -> excluir());

        // Eventos de Busca e Filtro em tempo real
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

        // Selecionar linha da tabela para preencher os campos do formulário
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

    private void aplicarFiltros() {
        String textoBusca = campoBusca.getText().toLowerCase().trim();
        String catSelecionada = (String) comboFiltroCategoria.getSelectedItem();
        String statusSelecionado = (String) comboFiltroStatus.getSelectedItem();

        List<Projeto> projetosFiltrados = service.listarProjetos().stream().filter(p -> {
            boolean matchBusca = p.getNome().toLowerCase().contains(textoBusca) || 
                                 p.getDescricao().toLowerCase().contains(textoBusca);
            
            boolean matchCat = catSelecionada.equals("Todas Categorias") || p.getCategoria().equalsIgnoreCase(catSelecionada);
            boolean matchStatus = statusSelecionado.equals("Todos Status") || p.getStatus().equalsIgnoreCase(statusSelecionado);

            return matchBusca && matchCat && matchStatus;
        }).collect(Collectors.toList());

        carregarTabela(projetosFiltrados);
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoDescricao.setText("");
        comboCategoria.setSelectedIndex(0);
        comboStatus.setSelectedIndex(0);
        tabela.clearSelection();
        campoNome.requestFocus();

        try {
            service.carregarProjetos();
            aplicarFiltros();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void cadastrar() {
        String nome = campoNome.getText();
        String descricao = campoDescricao.getText();
        String categoria = comboCategoria.getSelectedItem().toString();
        String status = comboStatus.getSelectedItem().toString();

        if (nome.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome.");
            return;
        }

        Projeto projeto = new Projeto(nome, descricao, categoria, status);
        service.adicionarProjeto(projeto);

        try {
            service.salvar();
            JOptionPane.showMessageDialog(this, "Projeto cadastrado com sucesso!");
            limparFormulario();
        } catch (Exception e) {
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
            JOptionPane.showMessageDialog(this, "Preencha o nome e a descrição.");
            return;
        }

        int id = (Integer) tabela.getValueAt(linha, 0);
        Projeto projeto = new Projeto(nome, descricao, categoria, status);
        boolean alterou = service.alterarProjeto(id, projeto);

        if (!alterou) {
            JOptionPane.showMessageDialog(this, "Não foi possível alterar o projeto.");
            return;
        }

        try {
            service.salvar();
            JOptionPane.showMessageDialog(this, "Projeto alterado com sucesso.");
            limparFormulario();
        } catch (Exception a) {
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
                JOptionPane.showMessageDialog(this, "ID inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Projeto projeto = service.buscarPorId(idExcluir);
        if (projeto == null) {
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
                JOptionPane.showMessageDialog(this, "Projeto excluído com sucesso!");
                limparFormulario();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void carregarTabela(List<Projeto> lista) {
        try {
            service.carregarProjetos();
        } catch (Exception e) {
            System.err.println(e);
        }

        modelo.setRowCount(0);

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