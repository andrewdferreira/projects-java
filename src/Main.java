import javax.swing.SwingUtilities;

import api.Api;
import util.LogConfig;
import view.TelaProjetos;

public class Main {
    public static void main(String[] args) throws Exception {
        // Inicializa o sistema central de logs em logs/
        LogConfig.inicializar();
        LogConfig.application("INFO", "Iniciando aplicação completa (API REST + GUI Swing)...");

        // Inicializa a API Javalin
        Api.main();

        // Inicializa a Interface Gráfica Swing
        SwingUtilities.invokeLater(() -> {
            try {
                TelaProjetos tela = new TelaProjetos();
                tela.setVisible(true);
                LogConfig.access("Interface Gráfica Swing carregada e exibida com sucesso.");
            } catch (Exception e) {
                LogConfig.error("GUI_SWING", "Erro ao inicializar interface gráfica Swing: " + e.getMessage(), e);
            }
        });
    }
}
