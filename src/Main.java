import javax.swing.SwingUtilities;

import api.Api;
import util.LogConfig;
import view.TelaProjetos;

public class Main {
        public static void main(String[] args) throws Exception {
                Api.main();

                LogConfig.inicializar();

                SwingUtilities.invokeLater(() -> {
                        TelaProjetos tela = new TelaProjetos();
                        tela.setVisible(true);
                });
        }
}