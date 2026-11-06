package api;

import service.ProjetoService;
import model.Projeto;
import api.ErroResponse;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;

import java.io.IOException;
import java.util.List;

public class Api {
    public static void main() {
        ProjetoService service = new ProjetoService();
        try {
            service.carregarProjetos();
        } catch (Exception e) {
            System.err.println(e);
        }

        var app = Javalin.create(config -> {

            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                // Libera para qualquer origem em desenvolvimento
                });
            });

            config.routes.get("/", ctx -> {
                ctx.result("API Sistema de Projetos");
            });

            config.routes.get("/api/projetos", ctx -> {
                try {
                    service.carregarProjetos();
                } catch (Exception e) {
                    System.err.println(e);
                }
                List<Projeto> projetos = service.listarProjetos();
                ctx.json(projetos);
            });

            config.routes.get("/api/projetos/{id}", ctx -> {
                try {
                    service.carregarProjetos();
                } catch (Exception e) {
                    System.err.println(e);
                }

                int id = Integer.parseInt(ctx.pathParam("id"));
                Projeto projeto = service.buscarPorId(id);

                if (projeto == null) {
                    ctx.status(404);
                    return;
                }

                ctx.json(projeto);
            });

            config.routes.get("api/categorias", ctx -> {
                String[] categorias = new String[4];
                categorias[0] = "Web";
                categorias[1] = "Software";
                categorias[2] = "Mobile";
                categorias[3] = "Outros";

                ctx.json(categorias);
            });

            config.routes.get("api/status", ctx -> {
               String[] status = new String[3];
               status[0] = "Em planejamento";
               status[1] = "Em desenvolvimento";
               status[2] = "Concluído";

               ctx.json(status);
            });

            config.routes.post("api/projetos", ctx -> {
                try {
                    service.carregarProjetos();
                } catch (Exception e) {
                    System.err.println(e);
                }
                Projeto projeto = ctx.bodyAsClass(Projeto.class);

                if (projeto.getNome() == null || projeto.getNome().isBlank()) {
                    ctx.status(404);

                    ctx.json(new ErroResponse("Nome é obrigatório"));
                    return;
                }
                service.adicionarProjeto(projeto);
                service.salvar();
                ctx.status(201);
                ctx.json(projeto);
            });

            config.routes.put("/api/projetos/{id}", ctx -> {
                try {
                    service.carregarProjetos();
                } catch (Exception e) {
                    System.err.println(e);
                }
               int id = Integer.parseInt(ctx.pathParam("id"));
               Projeto projeto = ctx.bodyAsClass(Projeto.class);

               projeto.setId(id);

               boolean alterou = service.alterarProjeto(id, projeto);

               if (!alterou) {
                   ctx.status(404);
                   return;
               }

               service.salvar();
               ctx.json(projeto);
            });

            config.routes.delete("/api/projetos/{id}", ctx -> {
                try {
                    service.carregarProjetos();
                } catch (Exception e) {
                    System.err.println(e);
                }
                int id = Integer.parseInt(ctx.pathParam("id"));
                Projeto projeto = service.buscarPorId(id);

                if (projeto == null) {
                    ctx.status(404);
                    return;
                }

                service.removerPorId(id);
                service.salvar();
                ctx.status(404);
            });

        }).start(7070);
    }

}
