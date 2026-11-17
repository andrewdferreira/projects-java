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
 * Integrado aos 5 tipos de logs estruturados em logs/:
 * - application.log: Inicialização e encerramento do serviço.
 * - access.log: Acessos recebidos na API.
 * - audit.log: Inclusão, Alteração e Exclusão de projetos.
 * - error.log: Falhas de validação, erros de persistência CSV e requisições inválidas.
 * - api.log: Registro de requisições HTTP (GET, POST, PUT, DELETE).
 */
public class Api {

    static {
        LogConfig.inicializar();
    }

    private static final Logger logger = LoggerFactory.getLogger(Api.class);

    public static void main() {
        LogConfig.application("INFO", "Iniciando servidor de API REST na porta 7070...");
        logger.info("Inicializando o serviço de projetos e a API REST...");

        ProjetoService service = new ProjetoService();
        try {
            service.carregarProjetos();
            LogConfig.application("INFO", "Carga inicial de dados concluída. Total de projetos: " + service.listarProjetos().size());
        } catch (Exception e) {
            LogConfig.error("LEITURA_CSV", "Erro ao carregar dados iniciais do CSV: " + e.getMessage(), e);
            logger.error("Erro crítico ao carregar dados iniciais do CSV: {}", e.getMessage(), e);
        }

        var app = Javalin.create(config -> {

            // Configuração do CORS
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });

            // Interceptor de requisições: Marca tempo de início e registra acesso
            config.routes.before(ctx -> {
                ctx.attribute("reqStartTime", System.currentTimeMillis());
                LogConfig.access(String.format("Requisição HTTP recebida: %s %s | Origem: %s",
                        ctx.method(), ctx.path(), ctx.ip()));
                logger.info("[CHAMADA DE API] Recebida requisição {} {} | IP: {}",
                        ctx.method(), ctx.path(), ctx.ip());
            });

            // Interceptor de respostas: Grava api.log com método, rota, status e latência
            config.routes.after(ctx -> {
                Long startTime = ctx.attribute("reqStartTime");
                long duracao = startTime != null ? (System.currentTimeMillis() - startTime) : 0;
                int statusCode = ctx.status().getCode();

                LogConfig.api(ctx.method().toString(), ctx.path(), statusCode, duracao, ctx.ip());
                logger.info("[RESPOSTA API] {} {} concluída com status HTTP {} ({}ms)",
                        ctx.method(), ctx.path(), statusCode, duracao);
            });

            // Tratamento global de exceções
            config.routes.exception(Exception.class, (e, ctx) -> {
                LogConfig.error("ERRO_INTERNO", "Falha não tratada ao processar " + ctx.method() + " " + ctx.path() + ": " + e.getMessage(), e);
                LogConfig.application("ERROR", "Exceção em rota " + ctx.path() + ": " + e.getMessage());
                logger.error("[ERRO NÃO TRATADO] Falha interna ao processar {} {}: {}",
                        ctx.method(), ctx.path(), e.getMessage(), e);
                ctx.status(500).json(new ErroResponse("Erro interno no servidor: " + e.getMessage()));
            });

            // Rota raiz de verificação de integridade
            config.routes.get("/", ctx -> {
                LogConfig.access("Verificação de status via rota raiz '/' por " + ctx.ip());
                ctx.result("API Sistema de Projetos");
            });

            // GET: Listar todos os projetos
            config.routes.get("/api/projetos", ctx -> {
                try {
                    service.carregarProjetos();
                    List<Projeto> projetos = service.listarProjetos();
                    ctx.json(projetos);
                } catch (Exception e) {
                    LogConfig.error("LEITURA_CSV", "Falha ao recarregar projetos no GET /api/projetos: " + e.getMessage(), e);
                    ctx.status(500).json(new ErroResponse("Erro ao ler dados dos projetos"));
                }
            });

            // GET: Buscar projeto por ID
            config.routes.get("/api/projetos/{id}", ctx -> {
                String idParam = ctx.pathParam("id");
                int id;
                try {
                    id = Integer.parseInt(idParam);
                } catch (NumberFormatException e) {
                    LogConfig.error("CAMPO_INVALIDO", "ID não numérico fornecido na rota: '" + idParam + "'", null);
                    ctx.status(400).json(new ErroResponse("O ID do projeto deve ser um número inteiro"));
                    return;
                }

                try {
                    service.carregarProjetos();
                    Projeto projeto = service.buscarPorId(id);

                    if (projeto == null) {
                        ctx.status(404).json(new ErroResponse("Projeto com ID " + id + " não encontrado"));
                        return;
                    }

                    ctx.json(projeto);
                } catch (Exception e) {
                    LogConfig.error("LEITURA_CSV", "Falha ao consultar projeto ID " + id + ": " + e.getMessage(), e);
                    ctx.status(500).json(new ErroResponse("Erro ao consultar projeto no banco de dados"));
                }
            });

            // GET: Categorias disponíveis
            config.routes.get("/api/categorias", ctx -> {
                String[] categorias = {"Web", "Software", "Mobile", "Desktop", "Outros"};
                ctx.json(categorias);
            });

            // GET: Status disponíveis
            config.routes.get("/api/status", ctx -> {
                String[] status = {"Planejado", "Em andamento", "Concluído", "Cancelado"};
                ctx.json(status);
            });

            // POST: Criação de novo projeto
            config.routes.post("/api/projetos", ctx -> {
                Projeto projeto;
                try {
                    projeto = ctx.bodyAsClass(Projeto.class);
                } catch (Exception e) {
                    LogConfig.error("CAMPO_INVALIDO", "Corpo JSON malformado no POST /api/projetos: " + e.getMessage(), e);
                    ctx.status(400).json(new ErroResponse("Corpo da requisição JSON inválido ou malformado"));
                    return;
                }

                if (projeto == null || projeto.getNome() == null || projeto.getNome().isBlank()) {
                    LogConfig.error("CAMPO_INVALIDO", "Tentativa de cadastro com campo 'nome' vazio no POST /api/projetos", null);
                    ctx.status(400).json(new ErroResponse("O nome do projeto é obrigatório"));
                    return;
                }

                try {
                    service.carregarProjetos();
                    service.adicionarProjeto(projeto);
                    service.salvar();

                    // AUDITORIA PRINCIPAL: Criação de projeto
                    LogConfig.audit("CRIADO", projeto.getId(),
                            String.format("Nome='%s', Categoria='%s', Status='%s'",
                                    projeto.getNome(), projeto.getCategoria(), projeto.getStatus()));

                    ctx.status(201).json(projeto);
                } catch (Exception e) {
                    LogConfig.error("LEITURA_CSV", "Falha ao persistir novo projeto no CSV: " + e.getMessage(), e);
                    ctx.status(500).json(new ErroResponse("Erro ao persistir novo projeto no banco de dados"));
                }
            });

            // PUT: Atualização de projeto existente
            config.routes.put("/api/projetos/{id}", ctx -> {
                String idParam = ctx.pathParam("id");
                int id;
                try {
                    id = Integer.parseInt(idParam);
                } catch (NumberFormatException e) {
                    LogConfig.error("CAMPO_INVALIDO", "ID não numérico fornecido no PUT: '" + idParam + "'", null);
                    ctx.status(400).json(new ErroResponse("O ID do projeto deve ser um número inteiro"));
                    return;
                }

                Projeto projetoAtualizado;
                try {
                    projetoAtualizado = ctx.bodyAsClass(Projeto.class);
                } catch (Exception e) {
                    LogConfig.error("CAMPO_INVALIDO", "JSON malformado no PUT /api/projetos/" + id + ": " + e.getMessage(), e);
                    ctx.status(400).json(new ErroResponse("Corpo da requisição JSON inválido ou malformado"));
                    return;
                }

                if (projetoAtualizado == null || projetoAtualizado.getNome() == null || projetoAtualizado.getNome().isBlank()) {
                    LogConfig.error("CAMPO_INVALIDO", "Campo 'nome' em branco no PUT /api/projetos/" + id, null);
                    ctx.status(400).json(new ErroResponse("O nome do projeto é obrigatório para alteração"));
                    return;
                }

                try {
                    service.carregarProjetos();
                    projetoAtualizado.setId(id);

                    boolean alterou = service.alterarProjeto(id, projetoAtualizado);

                    if (!alterou) {
                        LogConfig.error("CAMPO_INVALIDO", "Projeto ID " + id + " inexistente para alteração", null);
                        ctx.status(404).json(new ErroResponse("Projeto com ID " + id + " não encontrado"));
                        return;
                    }

                    service.salvar();

                    // AUDITORIA PRINCIPAL: Alteração de projeto
                    LogConfig.audit("ALTERADO", id,
                            String.format("Nome='%s', Categoria='%s', Status='%s'",
                                    projetoAtualizado.getNome(), projetoAtualizado.getCategoria(), projetoAtualizado.getStatus()));

                    ctx.status(200).json(projetoAtualizado);
                } catch (Exception e) {
                    LogConfig.error("LEITURA_CSV", "Falha ao salvar alterações do projeto ID " + id + ": " + e.getMessage(), e);
                    ctx.status(500).json(new ErroResponse("Erro ao salvar alterações do projeto"));
                }
            });

            // DELETE: Exclusão de projeto por ID
            config.routes.delete("/api/projetos/{id}", ctx -> {
                String idParam = ctx.pathParam("id");
                int id;
                try {
                    id = Integer.parseInt(idParam);
                } catch (NumberFormatException e) {
                    LogConfig.error("CAMPO_INVALIDO", "ID não numérico fornecido no DELETE: '" + idParam + "'", null);
                    ctx.status(400).json(new ErroResponse("O ID do projeto deve ser um número inteiro"));
                    return;
                }

                try {
                    service.carregarProjetos();
                    Projeto projeto = service.buscarPorId(id);

                    if (projeto == null) {
                        LogConfig.error("CAMPO_INVALIDO", "Projeto ID " + id + " não encontrado para exclusão", null);
                        ctx.status(404).json(new ErroResponse("Projeto com ID " + id + " não encontrado"));
                        return;
                    }

                    service.removerPorId(id);
                    service.salvar();

                    // AUDITORIA PRINCIPAL: Exclusão de projeto
                    LogConfig.audit("EXCLUIDO", id, "Nome='" + projeto.getNome() + "'");

                    ctx.status(200).json(projeto);
                } catch (Exception e) {
                    LogConfig.error("LEITURA_CSV", "Falha ao excluir projeto ID " + id + " no CSV: " + e.getMessage(), e);
                    ctx.status(500).json(new ErroResponse("Erro ao excluir projeto do banco de dados"));
                }
            });

        }).start(7070);

        LogConfig.application("INFO", "API REST iniciada com sucesso em http://localhost:7070");
        logger.info("API Sistema de Projetos iniciada com sucesso na porta 7070!");
    }
}
