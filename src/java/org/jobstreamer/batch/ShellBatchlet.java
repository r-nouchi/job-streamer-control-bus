package org.jobstreamer.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.batch.api.AbstractBatchlet;
import javax.batch.operations.BatchRuntimeException;
import javax.batch.runtime.context.StepContext;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * A builtin batchlet for running a shellscript.
 *
 * @author kawasima
 */
public class ShellBatchlet extends AbstractBatchlet {
    private static final Logger logger = LoggerFactory.getLogger(ShellBatchlet.class);
    private Process process;

    @Any
    @Inject
    StepContext stepContext;

    private Path createTemporaryScript(URL resourceUrl, String script) throws IOException {
        URLConnection connection = resourceUrl.openConnection();
        Path scriptFile = null;
        try (InputStream in = connection.getInputStream()) {
            scriptFile = Files.createTempFile(
                    Paths.get(script).getFileName().toString(),
                    ".exe");
            Files.copy(in, scriptFile, StandardCopyOption.REPLACE_EXISTING);
            scriptFile.toFile().setExecutable(true);
        }

        return scriptFile;
    }

    private String executeScript(Path script) {
        ProcessBuilder pb = new ProcessBuilder(script.toAbsolutePath().toString());
        pb.redirectErrorStream(true);

        try {
            logger.info("Start process");
            process = pb.start();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                    logger.info(line);
                }
            }
        } finally {
            if (process != null) {
                try {
                    process.waitFor();
                    return Integer.toString(process.exitValue());
                } catch (InterruptedException e) {
                    throw new IllegalStateException("process is interrupted.");
                }
            } else {
                throw new IllegalStateException("process won't start.");
            }
        }
    }

    @Override
    public String process() throws Exception {
        String script = stepContext.getProperties().getProperty("script");
        if (script == null) {
            logger.error("script is null");
            throw new IllegalStateException("script is null");
        }

        URL resourceUrl = getClass().getClassLoader().getResource(script);
        if (resourceUrl == null) {
            logger.error("resource [" + script + "] is not found.");
            throw new IllegalStateException("resource [" + script + "] is not found.");
        }

        Path temporaryScript = createTemporaryScript(resourceUrl, script);
        try {
            return executeScript(temporaryScript);
        } finally {
            if (temporaryScript != null) {
                Files.deleteIfExists(temporaryScript);
            }
        }
    }

    @Override
    public void stop() {
        try {
            logger.info("Stopping job");
            process.getOutputStream().close();
            process.getInputStream().close();
            logger.info("Destroy process");
            process.destroy();
        } catch (IOException e) {
            throw new BatchRuntimeException("Process stop failure.", e);
        }
    }

}
