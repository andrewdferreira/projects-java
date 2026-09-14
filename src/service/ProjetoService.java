package service;

import java.util.ArrayList;
import java.util.List;

import model.Projeto;

public class ProjetoService {
    private List<Projeto> projetos;
    // Ainda não instanciado

    public ProjetoService() {
        projetos = new ArrayList<>();
    // Método construtor instanciou o objeto
    }

    public void adicionarProjeto(Projeto projeto) {
        projetos.add(projeto);
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
                return true;
            }
        return false;
    }
}