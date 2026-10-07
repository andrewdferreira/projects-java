package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import service.ProjetoService;
import model.Projeto;

public class TelaProjetos extends JFrame {

    private JTextField campoNome;
    private JTextField campoDescricao;

    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;

    private JTable tabela;
    private DefaultTableModel modelo;

    private JButton botaoCadastrar;
    private JButton botaoLimpar;

    private ProjetoService service;
    private Projeto projeto;

    public TelaProjetos() {
        service = new ProjetoService();

        setTitle("Sistema de Projetos");
        setSize(800, 500);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        criarComponentes();

        criarEventos();
        carregarTabela();
    }

    private void criarComponentes() {

        campoNome = new JTextField(20);

        campoDescricao = new JTextField(20);

        comboCategoria = new JComboBox<>();

        comboCategoria.addItem("Web");
        comboCategoria.addItem("Software");
        comboCategoria.addItem("Mobile");
        comboCategoria.addItem("Outro");

        comboStatus = new JComboBox<>();

        comboStatus.addItem("Planejado");
        comboStatus.addItem("Em desenvolvimento");
        comboStatus.addItem("Concluído");

        botaoCadastrar = new JButton("Cadastrar");

        botaoLimpar = new JButton("Limpar");

        modelo = new DefaultTableModel();

        modelo.addColumn("ID");
        modelo.addColumn("Nome");
        modelo.addColumn("Categoria");
        modelo.addColumn("Status");

        tabela = new JTable(modelo);

        JPanel painelFormulario = new JPanel();

        painelFormulario.add(new JLabel("Nome:"));
        painelFormulario.add(campoNome);

        painelFormulario.add(new JLabel("Descrição:"));
        painelFormulario.add(campoDescricao);

        painelFormulario.add(new JLabel("Categoria:"));
        painelFormulario.add(comboCategoria);

        painelFormulario.add(new JLabel("Status:"));
        painelFormulario.add(comboStatus);

        painelFormulario.add(botaoCadastrar);
        painelFormulario.add(botaoLimpar);

        setLayout(new BoxLayout(
                getContentPane(),
                BoxLayout.Y_AXIS
        ));

        add(painelFormulario);

        add(new JScrollPane(tabela));
    }

    private void criarEventos() {

        botaoLimpar.addActionListener(e -> limparFormulario());

        botaoCadastrar.addActionListener(e -> cadastrar());
    }

    private void limparFormulario() {

        campoNome.setText("");

        campoDescricao.setText("");

        comboCategoria.setSelectedIndex(0);

        comboStatus.setSelectedIndex(0);

        campoNome.requestFocus();

        try {
            service.carregarProjetos();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void cadastrar() {

        String nome = campoNome.getText();
        String descricao = campoDescricao.getText();
        String categoria =
                comboCategoria.getSelectedItem().toString();

        String status =
                comboStatus.getSelectedItem().toString();

        if (nome.isBlank()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Informe o nome."
            );

            return;
        }

        Projeto projeto = new Projeto(
                nome,
                descricao,
                categoria,
                status
        );

        service.adicionarProjeto(projeto);

        try {
            service.salvar();

            JOptionPane.showMessageDialog(
                    this,
                    "Projeto cadastrado com sucesso!"
            );
            limparFormulario();
            carregarTabela();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erro ao salvar o projeto: " + e.getMessage(),
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void carregarTabela() {
        try {
            service.carregarProjetos();
        } catch (Exception e) {
            System.err.println(e);
        }

        modelo.setRowCount(0);

        for (Projeto projeto : service.listarProjetos()) {

            modelo.addRow(
                    new Object[]{
                            projeto.getId(),
                            projeto.getNome(),
                            projeto.getCategoria(),
                            projeto.getStatus()
                    }
            );
        }

        limparFormulario();
    }
}