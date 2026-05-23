package com.codevault.services;

import com.codevault.ui.ConsolePanel;

import javax.swing.SwingWorker;
import java.io.*;
import java.util.List;

/**
 * CodeRunnerService — handles compiling and running Java code.
 *
 * ─── HOW IT WORKS ────────────────────────────────────────────────────────────
 *
 *  1. Take the code string from the editor.
 *  2. Write it to a temp file:  /tmp/codevault_run/Main.java
 *  3. Compile it using:         javac /tmp/codevault_run/Main.java
 *  4. Run it using:             java -cp /tmp/codevault_run Main
 *  5. Capture all output and errors, send them to the ConsolePanel.
 *
 * ─── IMPORTANT CONCEPTS ──────────────────────────────────────────────────────
 *
 *  Runtime.getRuntime().exec(String[])
 *    Launches an external process (like a terminal command) from inside Java.
 *    We pass a String[] instead of a single String to avoid shell-injection
 *    bugs and handle paths with spaces correctly.
 *
 *  Process
 *    Represents the running external program. You can:
 *      - Read its output via  process.getInputStream()
 *      - Read its errors via  process.getErrorStream()
 *      - Wait for it to end:  process.waitFor()  → returns exit code (0 = OK)
 *
 *  BufferedReader + InputStreamReader
 *    Wraps the raw byte stream from the process into readable text lines.
 *    InputStreamReader converts bytes → characters.
 *    BufferedReader lets us call readLine() to get one line at a time.
 *
 *  SwingWorker<Void, String>
 *    Swing is single-threaded. If we run javac/java on the main thread,
 *    the entire window freezes until execution finishes.
 *    SwingWorker runs our heavy work on a BACKGROUND thread.
 *    The two type parameters are:
 *      Void   → the final return type of doInBackground() (we return nothing)
 *      String → the type we publish() mid-execution to update the UI live
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class CodeRunnerService {

    private final ConsolePanel consolePanel;

    // Temp directory where we write and compile the user's code
    private static final String TEMP_DIR  = System.getProperty("java.io.tmpdir")
                                            + File.separator + "codevault_run"
                                            + File.separator;
    private static final String FILE_NAME = "Main.java";
    private static final String CLASS_NAME = "Main";

    public CodeRunnerService(ConsolePanel consolePanel) {
        this.consolePanel = consolePanel;
    }

    /**
     * runCode() — the public entry point.
     *
     * @param code       the Java source code from the editor
     * @param onComplete a Runnable called when execution finishes
     *                   (used by MainFrame to re-enable the Run button)
     */
    public void runCode(String code, Runnable onComplete) {

        // Clear old output and show a starting message immediately (on the EDT)
        consolePanel.clearConsole();
        consolePanel.appendOutput("▶  Starting...\n");

        // Build and launch the SwingWorker
        new SwingWorker<Void, String>() {

            // ── STEP A: runs on a BACKGROUND thread ──────────────────────────
            @Override
            protected Void doInBackground() throws Exception {

                // 1. Write code to temp file
                File tempDir = new File(TEMP_DIR);
                if (!tempDir.exists()) {
                    tempDir.mkdirs();   // create /tmp/codevault_run/ if needed
                }
                File sourceFile = new File(TEMP_DIR + FILE_NAME);
                try (FileWriter writer = new FileWriter(sourceFile)) {
                    writer.write(code);
                }
                publish("[INFO] Saved to: " + sourceFile.getAbsolutePath());

                // 2. Compile with javac
                publish("[Compiling...]");
                Process compileProcess = Runtime.getRuntime().exec(new String[]{
                    "javac",
                    "-d", TEMP_DIR,                 // output .class files here
                    sourceFile.getAbsolutePath()    // the source file to compile
                });

                // Read compilation errors (javac writes errors to stderr)
                String compileErrors = readStream(compileProcess.getErrorStream());

                // waitFor() blocks until javac finishes; returns exit code
                // exit code 0 = success, anything else = compilation failed
                int compileExitCode = compileProcess.waitFor();

                if (compileExitCode != 0) {
                    // Compilation failed — show the errors and stop
                    publish("\n[✗ Compilation Error]");
                    for (String line : compileErrors.split("\n")) {
                        publish(line);
                    }
                    return null;    // exits doInBackground(), triggers done()
                }
                publish("[✔ Compiled successfully]\n");

                // 3. Run the compiled class with java
                publish("[Running output:]");
                publish("─────────────────────────────");
                Process runProcess = Runtime.getRuntime().exec(new String[]{
                    "java",
                    "-cp", TEMP_DIR,    // classpath: where to find .class files
                    CLASS_NAME          // the class with main() to run
                });

                // Read standard output (what System.out.println produces)
                String programOutput = readStream(runProcess.getInputStream());
                // Read runtime errors (exceptions, stack traces, System.err)
                String runtimeErrors = readStream(runProcess.getErrorStream());

                int runExitCode = runProcess.waitFor();

                // Show program output line by line
                if (!programOutput.isEmpty()) {
                    for (String line : programOutput.split("\n")) {
                        publish(line);
                    }
                }

                // Show runtime errors if any
                if (!runtimeErrors.isEmpty()) {
                    publish("\n[✗ Runtime Error]");
                    for (String line : runtimeErrors.split("\n")) {
                        publish(line);
                    }
                }

                publish("─────────────────────────────");
                publish("Process finished with exit code " + runExitCode);

                // 4. Cleanup temp files after running
                cleanUp(sourceFile);

                return null;    // Void return — nothing to pass to done()
            }

            // ── STEP B: called on the EDT with batched publish() chunks ──────
            // This is how background-thread output reaches the UI safely.
            @Override
            protected void process(List<String> chunks) {
                for (String line : chunks) {
                    consolePanel.appendOutput(line);
                }
            }

            // ── STEP C: called on the EDT when doInBackground() finishes ─────
            @Override
            protected void done() {
                if (onComplete != null) {
                    onComplete.run();   // re-enables the Run button in MainFrame
                }
                try {
                    get();  // rethrows any exception thrown in doInBackground()
                } catch (java.util.concurrent.ExecutionException e) {
                    consolePanel.appendOutput("[Unexpected Error] " + e.getCause().getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        }.execute();    // .execute() submits the worker to a background thread pool
    }

    /**
     * readStream() — reads all text from a process stream into a single String.
     *
     * Why InputStreamReader + BufferedReader?
     *   InputStream gives us raw bytes. InputStreamReader decodes them into
     *   characters using the system charset. BufferedReader buffers reads and
     *   gives us the convenient readLine() method.
     */
    private String readStream(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    /** Deletes the temp .java file after execution to keep things tidy. */
    private void cleanUp(File sourceFile) {
        if (sourceFile.exists()) {
            sourceFile.delete();
        }
        // Also delete the compiled .class file
        File classFile = new File(TEMP_DIR + CLASS_NAME + ".class");
        if (classFile.exists()) {
            classFile.delete();
        }
    }
}
