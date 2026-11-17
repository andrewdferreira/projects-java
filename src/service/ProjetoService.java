package service;

import java.util.ArrayList;
import java.util.List;

import model.Projeto;
import dao.ProjetoCSV;
import util.LogConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjetoService {

    private static final Logger logger = LoggerFactory.getLogger(ProjetoService.class);

    private List<Projeto> projetos;
    private ProjetoCSV dao;

    public ProjetoService() {
        projetos = new ArrayList<>();
        dao = new ProjetoCSV();
    }

    public boolean adicionarProjeto(Projeto projeto) {
        if (projeto == null || projeto.getNome() == null || projeto.getNome().isBlank()) {
            LogConfig.error("CAMPO_INVALIDO", "Tentativa de adicionar projeto inválido: nome ausente.", null);
            logger.warn("Tentativa de adicionar projeto inválido: nome ausente.");
            return false;
        }

        projeto.setId(proximoId());
        projetos.add(projeto);
        logger.info("Projeto adicionado com sucesso ao serviço: ID={}, Nome='{}', Categoria='{}', Status='{}'",
                projeto.getId(), projeto.getNome(), projeto.getCategoria(), projeto.getStatus());

        return true;
    }

    public List<Projeto> listarProjetos() {
        return projetos;
    }

    public Projeto buscarPorId(int id) {
        for (Projeto projeto : projetos) {
            if (projeto.getId() == id) {
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
            logger.info("Projeto com ID {} ('{}') removido com sucesso.", id, projeto.getNome());
            return true;
        }
        LogConfig.error("CAMPO_INVALIDO", "Falha ao remover: projeto com ID " + id + " não encontrado.", null);
        logger.warn("Falha ao remover: projeto com ID {} não foi encontrado.", id);
        return false;
    }

    public int contarPorCategoria(String categoria) {
        return buscarPorCategoria(categoria).size();
    }

    public boolean alterarStatus(int id, String novoStatus) {
        Projeto projeto = buscarPorId(id);

        if (projeto == null) {
            LogConfig.error("CAMPO_INVALIDO", "Falha ao alterar status: projeto ID " + id + " não encontrado.", null);
            logger.warn("Falha ao alterar status: projeto com ID {} não encontrado.", id);
            return false;
        }

        projeto.setStatus(novoStatus);
        logger.info("Status do projeto ID {} alterado para '{}'.", id, novoStatus);
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

    public void carregarProjetos() throws Exception {
        logger.debug("Carregando projetos do DAO...");
        try {
            projetos = dao.listar();
            logger.debug("Total de projetos carregados: {}", projetos.size());
        } catch (Exception e) {
            LogConfig.error("LEITURA_CSV", "Erro ao carregar projetos do arquivo CSV: " + e.getMessage(), e);
            throw e;
        }
    }

    public void salvar() throws Exception {
        logger.debug("Salvando {} projetos no DAO...", projetos.size());
        try {
            dao.salvar(projetos);
            carregarProjetos();
            logger.debug("Projetos salvos com sucesso.");
        } catch (Exception e) {
            LogConfig.error("LEITURA_CSV", "Erro ao salvar projetos no arquivo CSV: " + e.getMessage(), e);
            throw e;
        }
    }

    public int proximoId() {
        int maior = 0;

        for (Projeto projeto : projetos) {
            if (projeto.getId() > maior) {
                maior = projeto.getId();
            }
        }

        return maior + 1;
    }

    public boolean alterarProjeto(int id, Projeto projetoAtualizado) {
        Projeto projeto = buscarPorId(id);

        if (projeto == null) {
            LogConfig.error("CAMPO_INVALIDO", "Falha ao alterar: projeto com ID " + id + " não encontrado.", null);
            logger.warn("Falha ao alterar: projeto com ID {} não encontrado.", id);
            return false;
        }

        projeto.setNome(projetoAtualizado.getNome());
        projeto.setDescricao(projetoAtualizado.getDescricao());
        projeto.setCategoria(projetoAtualizado.getCategoria());
        projeto.setStatus(projetoAtualizado.getStatus());

        logger.info("Projeto ID {} alterado com sucesso no serviço: Nome='{}', Categoria='{}', Status='{}'",
                id, projeto.getNome(), projeto.getCategoria(), projeto.getStatus());
        return true;
    }

}
