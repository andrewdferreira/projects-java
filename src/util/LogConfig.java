package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilitário para gerenciamento e persistência de logs na pasta /log.
 * Garante que a pasta 'log' exista na raiz do projeto e duplica todas as saídas
 * de log do SLF4J e do sistema para o arquivo 'log/sistema.log', mantendo a exibição
 * simultânea no console.
 */
public class LogConfig {

    private static boolean inicializado = false;
    public static final String LOG_DIR = "log";
    public static final String LOG_FILE = "log/sistema.log";

    public static synchronized void inicializar() {
        if (inicializado) return;

        try {
            Path dir = Path.of(LOG_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            File arquivoLog = new File(LOG_FILE);
            FileOutputStream fos = new FileOutputStream(arquivoLog, true);

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            String cabecalho = String.format("%n=======================================================%n" +
                    "=== SESSÃO DE LOG INICIADA EM: %s ===%n" +
                    "=======================================================%n", timestamp);
            fos.write(cabecalho.getBytes(StandardCharsets.UTF_8));
            fos.flush();

            PrintStream originalErr = System.err;

            // Stream duplo (Tee): envia para a saída de erro do console e para o arquivo log/sistema.log
            OutputStream teeStreamErr = new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    originalErr.write(b);
                    fos.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    originalErr.write(b, off, len);
                    fos.write(b, off, len);
                }

                @Override
                public void flush() throws IOException {
                    originalErr.flush();
                    fos.flush();
                }
            };

            System.setErr(new PrintStream(teeStreamErr, true, StandardCharsets.UTF_8));

        } catch (Exception e) {
            System.err.println("Aviso: Falha ao inicializar arquivo de log em 'log/sistema.log': " + e.getMessage());
        }

        inicializado = true;
    }

    /**
     * Registra uma linha de log diretamente no arquivo log/sistema.log.
     */
    public static synchronized void registrar(String nivel, String mensagem) {
        inicializar();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String linha = String.format("[%s] [%s] %s%n", timestamp, nivel, mensagem);
        try {
            Files.writeString(Path.of(LOG_FILE), linha, StandardCharsets.UTF_8, 
                    java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }
}
