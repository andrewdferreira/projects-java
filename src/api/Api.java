package api;

import service.ProjetoService;
import model.Projeto;
import util.LogConfig;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Servidor HTTP REST da aplicação utilizando Javalin.
 * Possui rastreamento e auditoria completos via SLF4J para todas as requisições,
 * operações de CRUD (criação, leitura, atualização e exclusão), validações e tratamentos de erro.
 */
public class Api {

    static {
        LogConfig.inicializar();
    }

    private static final Logger logger = LoggerFactory.getLogger(Api.class);

    public static void main() {
        logger.info("Inicializando o serviço de projetos e a API REST...");

        ProjetoService service = new ProjetoService();
        try {
            service.carregarProjetos();
            logger.info("Carga inicial de dados concluída. Total de projetos carregados: {}", service.listarProjetos().size());
        } catch (Exception e) {
            logger.error("Erro crítico ao carregar dados iniciais do CSV: {}", e.getMessage(), e);
        }

        var app = Javalin.create(config -> {

            // Configuração do CORS
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });

            // Interceptor de requisições: Loga todas as chamadas de API recebidas
            config.routes.before(ctx -> {
                logger.info("[CHAMADA DE API] Recebida requisição {} {} | IP: {}", 
                        ctx.method(), ctx.path(), ctx.ip());
            });

            // Interceptor de respostas: Loga o status HTTP final entregue ao cliente
            config.routes.after(ctx -> {
                logger.info("[RESPOSTA API] {} {} concluída com status HTTP {}", 
                        ctx.method(), ctx.path(), ctx.status());
            });

            // Tratamento global de exceções
            config.routes.exception(Exception.class, (e, ctx) -> {
                logger.error("[ERRO NÃO TRATADO] Falha interna ao processar {} {}: {}", 
                        ctx.method(), ctx.path(), e.getMessage(), e);
                ctx.status(500).json(new ErroResponse("Erro interno no servidor: " + e.getMessage()));
            });

            // Rota raiz de verificação
            config.routes.get("/", ctx -> {
                logger.info("Rota raiz '/' acessada para verificação de status da API.");
                ctx.result("API Sistema de Projetos");
            });

            // GET: Listar todos os projetos
            config.routes.get("/api/projetos", ctx -> {
                logger.info("[CONSULTA] Solicitada listagem completa de projetos.");
                try {
                    service.carregarProjetos();
                    List<Projeto> projetos = service.listarProjetos();
                    logger.info("[CONSULTA] Retornando {} projeto(s) cadastrado(s).", projetos.size());
                    ctx.json(projetos);
                } catch (Exception e) {
                    logger.error("[ERRO PERSISTÊNCIA] Falha ao recarregar projetos no GET /api/projetos: {}", e.getMessage(), e);
                    ctx.status(500).json(new ErroResponse("Erro ao ler dados dos projetos"));
                }
            });

            // GET: Buscar projeto por ID
            config.routes.get("/api/projetos/{id}", ctx -> {
                String idParam = ctx.pathParam("id");
                logger.info("[CONSULTA] Solicitada busca de projeto pelo ID: '{}'.", idParam);

                int id;
                try {
                    id = Integer.parseInt(idParam);
                } catch (NumberFormatException e) {
                    logger.warn("[VALIDAÇÃO] Falha na busca: ID informado '{}' não é um número inteiro válido.", idParam);
                    ctx.status(400).json(new ErroResponse("O ID do projeto deve ser um número inteiro"));
                    return;
                }

                try {
                    service.carregarProjetos();
                    Projeto projeto = service.buscarPorId(id);

                    if (projeto == null) {
                        logger.warn("[CONSULTA] Projeto com ID {} não foi localizado no sistema.", id);
                        ctx.status(404).json(new ErroResponse("Projeto com ID " + id + " não encontrado"));
                        return;
                    }

                    logger.info("[CONSULTA] Projeto ID {} localizado com sucesso: '{}'.", id, projeto.getNome());
                    ctx.json(projeto);
                } catch (Exception e) {
                    logger.error("[ERRO PERSISTÊNCIA] Falha ao consultar projeto ID {}: {}", id, e.getMessage(), e);
                    ctx.status(500).json(new ErroResponse("Erro ao consultar projeto no banco de dados"));
                }
            });

            // GET: Categorias disponíveis
            config.routes.get("/api/categorias", ctx -> {
                logger.info("[METADADOS] Solicitada lista de categorias disponíveis.");
                String[] categorias = {"Web", "Software", "Mobile", "Outros"};
                ctx.json(categorias);
            });

            // GET: Status disponíveis
            config.routes.get("/api/status", ctx -> {
                logger.info("[METADADOS] Solicitada lista de status disponíveis.");
                String[] status = {"Planejado", "Em andamento", "Concluído"};
                ctx.json(status);
            });

            // POST: Criação de novo projeto
            config.routes.post("/api/projetos", ctx -> {
                logger.info("[CRIAÇÃO] Recebida solicitação para criar novo projeto.");

                Projeto projeto;
                try {
                    projeto = ctx.bodyAsClass(Projeto.class);
                } catch (Exception e) {
                    logger.warn("[VALIDAÇÃO] Corpo JSON da requisição de criação é inválido: {}", e.getMessage());
                    ctx.status(400).json(new ErroResponse("Corpo da requisição JSON inválido ou malformado"));
                    return;
                }

                if (projeto == null || projeto.getNome() == null || projeto.getNome().isBlank()) {
                    logger.warn("[VALIDAÇÃO] Falha na criação: campo obrigatório 'nome' está nulo ou vazio.");
                    ctx.status(400).json(new ErroResponse("O nome do projeto é obrigatório"));
                    return;
                }

                try {
                    service.carregarProjetos();
                    service.adicionarProjeto(projeto);
                    service.salvar();

                    logger.info("[CRIAÇÃO SUCESSO] Projeto cadastrado com sucesso! ID={}: Nome='{}', Categoria='{}', Status='{}'",
                            projeto.getId(), projeto.getNome(), projeto.getCategoria(), projeto.getStatus());

                    ctx.status(201).json(projeto);
                } catch (Exception e) {
                    logger.error("[ERRO PERSISTÊNCIA] Falha ao salvar novo projeto: {}", e.getMessage(), e);
                    ctx.status(500).json(new ErroResponse("Erro ao persistir novo projeto no banco de dados"));
                }
            });

            // PUT: Atualização de projeto existente
            config.routes.put("/api/projetos/{id}", ctx -> {
                String idParam = ctx.pathParam("id");
                logger.info("[UPDATE] Recebida solicitação para atualizar projeto ID: '{}'.", idParam);

                int id;
                try {
                    id = Integer.parseInt(idParam);
                } catch (NumberFormatException e) {
                    logger.warn("[VALIDAÇÃO] Falha no update: ID '{}' não é um número válido.", idParam);
                    ctx.status(400).json(new ErroResponse("O ID do projeto deve ser um número inteiro"));
                    return;
                }

                Projeto projetoAtualizado;
                try {
                    projetoAtualizado = ctx.bodyAsClass(Projeto.class);
                } catch (Exception e) {
                    logger.warn("[VALIDAÇÃO] Corpo JSON inválido para atualização do projeto ID {}: {}", id, e.getMessage());
                    ctx.status(400).json(new ErroResponse("Corpo da requisição JSON inválido ou malformado"));
                    return;
                }

                if (projetoAtualizado == null || projetoAtualizado.getNome() == null || projetoAtualizado.getNome().isBlank()) {
                    logger.warn("[VALIDAÇÃO] Falha no update: campo obrigatório 'nome' não informado para projeto ID {}.", id);
                    ctx.status(400).json(new ErroResponse("O nome do projeto é obrigatório para alteração"));
                    return;
                }

                try {
                    service.carregarProjetos();
                    projetoAtualizado.setId(id);

                    boolean alterou = service.alterarProjeto(id, projetoAtualizado);

                    if (!alterou) {
                        logger.warn("[UPDATE FALHA] Projeto com ID {} não encontrado para alteração.", id);
                        ctx.status(404).json(new ErroResponse("Projeto com ID " + id + " não encontrado"));
                        return;
                    }

                    service.salvar();
                    logger.info("[UPDATE SUCESSO] Projeto ID {} alterado com sucesso! Nome='{}', Categoria='{}', Status='{}'",
                            id, projetoAtualizado.getNome(), projetoAtualizado.getCategoria(), projetoAtualizado.getStatus());

                    ctx.status(200).json(projetoAtualizado);
                } catch (Exception e) {
                    logger.error("[ERRO PERSISTÊNCIA] Falha ao persistir alterações do projeto ID {}: {}", id, e.getMessage(), e);
                    ctx.status(500).json(new ErroResponse("Erro ao salvar alterações do projeto"));
                }
            });

            // DELETE: Exclusão de projeto por ID
            config.routes.delete("/api/projetos/{id}", ctx -> {
                String idParam = ctx.pathParam("id");
                logger.info("[DELETE] Recebida solicitação para excluir projeto ID: '{}'.", idParam);

                int id;
                try {
                    id = Integer.parseInt(idParam);
                } catch (NumberFormatException e) {
                    logger.warn("[VALIDAÇÃO] Falha no delete: ID '{}' não é um número inteiro válido.", idParam);
                    ctx.status(400).json(new ErroResponse("O ID do projeto deve ser um número inteiro"));
                    return;
                }

                try {
                    service.carregarProjetos();
                    Projeto projeto = service.buscarPorId(id);

                    if (projeto == null) {
                        logger.warn("[DELETE FALHA] Projeto com ID {} não encontrado para exclusão.", id);
                        ctx.status(404).json(new ErroResponse("Projeto com ID " + id + " não encontrado"));
                        return;
                    }

                    service.removerPorId(id);
                    service.salvar();

                    logger.info("[DELETE SUCESSO] Projeto ID {} ('{}') excluído com sucesso do sistema.", id, projeto.getNome());
                    ctx.status(200).json(projeto);
                } catch (Exception e) {
                    logger.error("[ERRO PERSISTÊNCIA] Falha ao excluir projeto ID {}: {}", id, e.getMessage(), e);
                    ctx.status(500).json(new ErroResponse("Erro ao excluir projeto do banco de dados"));
                }
            });

        }).start(7070);

        logger.info("API Sistema de Projetos iniciada com sucesso na porta 7070!");
    }
}
