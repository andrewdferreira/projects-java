package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utilitário central de gerenciamento e persistência de logs na pasta logs/.
 * Atende aos requisitos:
 * - application.log: Inicialização, encerramento, erros gerais.
 * - access.log: Acessos gerais (API e interfaces).
 * - audit.log (Principal): Inclusão, Alteração, Exclusão (ex: PROJETO 15 CRIADO/ALTERADO/EXCLUIDO).
 * - error.log: Erros isolados (ex: conexão API, leitura CSV, campo inválido).
 * - api.log: Requisições HTTP (GET/POST/DELETE/PUT).
 */
public class LogConfig {

    private static boolean inicializado = false;

    public static final String LOG_DIR = "logs";
    public static final String FILE_APPLICATION = "logs/application.log";
    public static final String FILE_ACCESS = "logs/access.log";
    public static final String FILE_AUDIT = "logs/audit.log";
    public static final String FILE_ERROR = "logs/error.log";
    public static final String FILE_API = "logs/api.log";

    public static synchronized void inicializar() {
        if (inicializado) return;

        try {
            Path dir = Path.of(LOG_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // Hook de desligamento para registrar encerramento
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                application("INFO", "Sistema encerrado com sucesso.");
            }));

            // Tratamento global de exceções não capturadas
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                application("FATAL", "Erro fatal não tratado na thread [" + t.getName() + "]: " + e.getMessage());
                error("FATAL", "Exceção não tratada na thread [" + t.getName() + "]", e);
            });

            inicializado = true;
            application("INFO", "Sistema inicializado com sucesso. Módulos de logs ativos.");

        } catch (Exception e) {
            System.err.println("Aviso: Falha ao inicializar diretório 'logs/': " + e.getMessage());
        }
    }

    private static String agora() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

    private static synchronized void gravar(String arquivo, String linha) {
        inicializar();
        try {
            Files.writeString(
                    Path.of(arquivo),
                    linha + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (Exception e) {
            System.err.println("Falha ao gravar em " + arquivo + ": " + e.getMessage());
        }
    }

    /**
     * application.log: Inicialização, encerramento, erros gerais.
     */
    public static void application(String nivel, String mensagem) {
        String linha = String.format("[%s] [%s] %s", agora(), nivel.toUpperCase(), mensagem);
        gravar(FILE_APPLICATION, linha);
    }

    /**
     * access.log: Acessos gerais de usuários e rotas.
     */
    public static void access(String mensagem) {
        String linha = String.format("[%s] [ACCESS] %s", agora(), mensagem);
        gravar(FILE_ACCESS, linha);
    }

    /**
     * audit.log (Principal): Inclusão, Alteração, Exclusão
     * Exemplo de saída:
     * [2026-11-17 10:00:00.000] [AUDIT] PROJETO 15 CRIADO - Nome='...' Categoria='...'
     * [2026-11-17 10:01:00.000] [AUDIT] PROJETO 15 ALTERADO - Nome='...'
     * [2026-11-17 10:02:00.000] [AUDIT] PROJETO 15 EXCLUIDO
     */
    public static void audit(String acao, int idProjeto, String detalhes) {
        String det = (detalhes != null && !detalhes.isBlank()) ? " - " + detalhes : "";
        String linha = String.format("[%s] [AUDIT] PROJETO %d %s%s", agora(), idProjeto, acao.toUpperCase(), det);
        gravar(FILE_AUDIT, linha);
    }

    /**
     * error.log: Erros isolados (ex: conexão API, leitura CSV, campo inválido).
     */
    public static void error(String categoria, String mensagem, Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] [ERROR] [%s] %s", agora(), categoria.toUpperCase(), mensagem));
        if (t != null) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            sb.append(System.lineSeparator()).append("    Detalhes: ").append(sw);
        }
        gravar(FILE_ERROR, sb.toString());
    }

    /**
     * api.log: Requisições (GET/POST/DELETE/PUT).
     */
    public static void api(String metodo, String path, int status, long duracaoMs, String ip) {
        String linha = String.format("[%s] [API] %s %s -> Status: %d (%dms) | IP: %s",
                agora(), metodo.toUpperCase(), path, status, duracaoMs, ip);
        gravar(FILE_API, linha);
    }

    /**
     * Método retrocompatível de log.
     */
    public static void registrar(String nivel, String mensagem) {
        application(nivel, mensagem);
    }
}
