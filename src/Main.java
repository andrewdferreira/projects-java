import model.Projeto;
import service.ProjetoService;

public class Main {

    public static void main(String[] args) {

        ProjetoService service = new ProjetoService();

        Projeto p1 = new Projeto(
            1,
            "Sistema Acadêmico",
            "Sistema para gerenciamento acadêmico",
            "Software",
            "Em desenvolvimento"
        );

        Projeto p2 = new Projeto(
            2,
            "Site Institucional",
            "Website institucional",
            "Web",
            "Concluído"
        );

        Projeto p3 = new Projeto(
            3,
            "Aplicativo Mobile",
            "Aplicativo de serviços",
            "Mobile",
            "Planejado"
        );

        Projeto p4 = new Projeto(
                4,
                "Portal de Eventos",
                "",
                "Web",
                "Em desenvolvimento"
        );

        Projeto p5 = new Projeto(
                5,
                "Sistema Financeiro",
                "",
                "Software",
                "Concluído"
        );


        service.adicionarProjeto(p1);
        service.adicionarProjeto(p2);
        service.adicionarProjeto(p3);
        service.adicionarProjeto(p4);
        service.adicionarProjeto(p5);

        System.out.println("LISTA DE PROJETOS");

        for (Projeto projeto : service.listarProjetos()) {
            projeto.exibirDados();
            System.out.println("------------------------");
        }

        System.out.println();

        System.out.println(
                "TOTAL DE PROJETOS: "
                + service.listarProjetos().size()
        );

        System.out.println();

        System.out.println("BUSCA PELO ID 3");

        Projeto encontrado = service.buscarPorId(3);

        if (encontrado != null) {
            encontrado.exibirDados();
        }

        System.out.println();

        System.out.println("PROJETOS WEB");

        for (Projeto projeto : service.buscarPorCategoria("Web")) {
            projeto.exibirDados();
            System.out.println("----------------------------------");
        }

        System.out.println();

        System.out.println("PROJETOS CONCLUÍDOS");

        for (Projeto projeto : service.buscarPorStatus("Concluído")) {
            projeto.exibirDados();
            System.out.println("------------------------------------");
        }

        System.out.println();

        service.removerPorId(4);

        System.out.println();

        Projeto p6 = new Projeto(
                1,
                "",
                "",
                "",
                ""
        );

        service.adicionarProjeto(p6);

        System.out.println();

        System.out.println("PROJETO DEPOIS DA ALTERAÇÃO:");

        for (Projeto projeto : service.listarProjetos()) {
            projeto.exibirDados();
            System.out.println("------------------------");
        }
    }
}

