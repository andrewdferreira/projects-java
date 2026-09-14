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

        service.adicionarProjeto(p1);

        for (Projeto projeto : service.listarProjetos()) {
            projeto.exibirDados();
        }

    }
}

