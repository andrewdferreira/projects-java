package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import model.Projeto;
import dao.ProjetoCSV;

public class ProjetoService {
    private List<Projeto> projetos;
    private ProjetoCSV dao;
    // Ainda não instanciado
    //Que guerra

    public ProjetoService() {
        projetos = new ArrayList<>();
    // Método construtor instanciou o objeto
    }

    public boolean adicionarProjeto(Projeto projeto) {
        Projeto existente = buscarPorId(projeto.getId());

        if (projeto.getNome() == null || projeto.getNome().isBlank()) {
            return false;
        }
        if (existente != null) {
            return false;
        }

        projetos.add(projeto);

        return true;
    }

    public List<Projeto> listarProjetos() {
        return projetos;
    }

    public Projeto buscarPorId(int id) {
        for (Projeto projeto : projetos) {
            if(projeto.getId() == id) {
                return projeto;
            }
        }
        return null;
    }

    public List<Projeto> buscarPorCategoria(String categoria) {
        List<Projeto> resultado = new ArrayList<>();

        for (Projeto projeto : projetos) {
            if (projeto.getCategoria().equalsIgnoreCase(categoria)) {
                resultado.add(projeto);
            }
        }

        return resultado;
    }

    public List<Projeto> buscarPorStatus(String status) {
        List<Projeto> resultado = new ArrayList<>();

        for (Projeto projeto : projetos) {
            if (projeto.getStatus().equalsIgnoreCase(status)) {
                resultado.add(projeto);
            }
        }

        return resultado;
    }

    public boolean removerPorId(int id) {
        Projeto projeto = buscarPorId(id);

        if (projeto != null) {
                projetos.remove(projeto);
                System.out.println("Projeto com ID " + id + " removido com sucesso");
                return true;
            }
        return false;
    }

    public int contarPorCategoria(String categoria) {
        return buscarPorCategoria(categoria).size();
    }

    public boolean alterarStatus(int id, String novoStatus) {
        Projeto projeto = buscarPorId(id);

        if (projeto == null) {
            return false;
        }

        projeto.setStatus(novoStatus);
        return true;
    }

    public List<Projeto> buscarPorNome(String texto) {
        List<Projeto> resultado = new ArrayList<>();

        for (Projeto projeto : projetos) {
            if (projeto.getNome().toLowerCase().contains(texto.toLowerCase())) {
                resultado.add(projeto);
            }
        }

        return resultado;
    }


}