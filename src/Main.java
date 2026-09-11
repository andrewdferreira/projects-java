import model.Projeto;

public class Main {
    public static void main(String[] args) {
        Projeto projeto1 = new Projeto(
            1,
            "Sistema Acadêmico",
            "Sistema para gerenciamento acadêmico",
            "Software",
            "Em desenvolvimento"
        );

        Projeto projeto2 = new Projeto(
            2,
            "Site Institucional",
            "Website de uma Instituição",
            "Web",
            "Concluído"
        );
        
        System.out.println("Projeto 1:");
        projeto1.exibirDados();
        System.out.println("Projeto 2:");
        projeto2.exibirDados();
    }
}
