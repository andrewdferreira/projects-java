package api;

import java.util.List;
import model.Projeto;
import service.ProjetoService;
import io.javalin.Javalin;

public class Api {

    public static void main(String[] args) {

        ProjetoService service = new ProjetoService();
        var app = Javalin.create(config -> {


            config.routes.get("/", ctx -> {
                ctx.result("API Sistemas de Projetos");
            });

            config.routes.get("/api/projetos", ctx -> {

                ctx.result("Lista de projetos");

                List<Projeto> projetos = service.listarProjetos();

                ctx.json(projetos);
            });

            config.routes.get("/api/projetos/{id}", ctx -> {
                    int id = Integer.parseInt(ctxPathParam("id"));

                    Projeto projeto = service.buscarPorId(id);

                    if (projeto == null) {
                        ctx.status(404);
                        return;
                    }
                    ctx.json(projeto);
                });

        }).start(7070);
    }
}