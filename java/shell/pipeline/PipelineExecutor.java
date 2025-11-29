package shell.pipeline;

import shell.core.InputParser;
import shell.core.ShellState;
import shell.history.HistoryManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static shell.pipeline.StreamUtils.*;

public class PipelineExecutor {
    
    public static void execute(String input, HistoryManager historyManager) throws IOException, InterruptedException {
        String[] commands = input.split("\\s*\\|\\s*");
        
        List<List<String>> parsedCommands = new ArrayList<>();
        for (String cmd : commands) {
            List<String> parsed = InputParser.parse(cmd.trim());
            if (!parsed.isEmpty()) {
                parsedCommands.add(parsed);
            }
        }
        
        if (parsedCommands.size() < 2) {
            System.out.println("Pipeline requires at least 2 commands");
            return;
        }
        
        executeMixedPipeline(parsedCommands);
    }
    
    private static void executeMixedPipeline(List<List<String>> commands) throws IOException, InterruptedException {
        PipedOutputStream[] pipeOuts = new PipedOutputStream[commands.size() - 1];
        PipedInputStream[] pipeIns = new PipedInputStream[commands.size() - 1];
        
        for (int i = 0; i < commands.size() - 1; i++) {
            pipeOuts[i] = new PipedOutputStream();
            pipeIns[i] = new PipedInputStream(pipeOuts[i], 65536);
        }
        
        List<Thread> threads = new ArrayList<>();
        List<Process> processes = new ArrayList<>();
        
        for (int i = 0; i < commands.size(); i++) {
            List<String> cmd = commands.get(i);
            String cmdName = cmd.get(0);
            String[] args = cmd.subList(1, cmd.size()).toArray(new String[0]);
            
            InputStream cmdInput;
            OutputStream cmdOutput;
            
            if (i == 0) {
                cmdInput = System.in;
            } else {
                cmdInput = pipeIns[i - 1];
            }
            
            if (i == commands.size() - 1) {
                cmdOutput = System.out;
            } else {
                cmdOutput = pipeOuts[i];
            }
            
            if (ShellState.BUILTINS.containsKey(cmdName)) {
                final InputStream finalInput = cmdInput;
                final OutputStream finalOutput = cmdOutput;
                final int index = i;
                
                Thread builtinThread = new Thread(() -> {
                    try {
                        executeBuiltinInPipeline(cmdName, args, finalInput, finalOutput);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (index < commands.size() - 1) {
                            closeQuietly(finalOutput);
                        }
                    }
                });
                builtinThread.start();
                threads.add(builtinThread);
            } else {
                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.directory(ShellState.getCurrentDir());
                
                Process process = pb.start();
                processes.add(process);
                
                if (i > 0) {
                    final InputStream in = cmdInput;
                    final OutputStream out = process.getOutputStream();
                    Thread inputThread = new Thread(() -> {
                        pipeData(in, out, true);
                    });
                    inputThread.start();
                    threads.add(inputThread);
                } else {
                    closeQuietly(process.getOutputStream());
                }
                
                if (i < commands.size() - 1) {
                    final InputStream in = process.getInputStream();
                    final OutputStream out = cmdOutput;
                    Thread outputThread = new Thread(() -> {
                        pipeData(in, out, true);
                    });
                    outputThread.start();
                    threads.add(outputThread);
                } else {
                    Thread outputThread = new Thread(() -> {
                        pipeData(process.getInputStream(), System.out, false);
                    });
                    outputThread.start();
                    threads.add(outputThread);
                }
                
                Thread errorThread = new Thread(() -> {
                    pipeData(process.getErrorStream(), System.err, false);
                });
                errorThread.start();
                threads.add(errorThread);
            }
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        for (Process p : processes) {
            p.waitFor();
        }
    }
    
    private static void executeBuiltinInPipeline(String cmdName, String[] args, 
                                                  InputStream input, OutputStream output) throws IOException {
        PrintStream out = new PrintStream(output, true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        
        switch (ShellState.BUILTINS.get(cmdName)) {
            case ECHO -> {
                if (args.length > 0) {
                    out.println(String.join(" ", args));
                } else {
                    out.println();
                }
            }
            case TYPE -> {
                while (reader.ready() && reader.readLine() != null) {
                    // Consume input
                }
                
                if (args.length >= 1) {
                    String cmdToCheck = args[0];
                    if (ShellState.BUILTINS.containsKey(cmdToCheck)) {
                        out.println(cmdToCheck + " is a shell builtin");
                    } else {
                        boolean found = false;
                        for (String dir : ShellState.DIRECTORIES) {
                            File file = new File(dir, cmdToCheck);
                            if (file.exists() && file.canExecute()) {
                                out.println(cmdToCheck + " is " + file.getAbsolutePath());
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            out.println(cmdToCheck + ": not found");
                        }
                    }
                }
            }
            case PWD -> {
                out.println(ShellState.getCurrentDir().getAbsolutePath());
            }
            case HISTORY -> {
                if (args.length >= 2 && (args[0].equals("-a") || args[0].equals("-w") || args[0].equals("-r"))) {
                    return;
                }
                
                // History display not implemented in pipeline for simplicity
            }
            case CAT -> {
                if (args.length == 0) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.println(line);
                    }
                } else {
                    for (String file : args) {
                        try {
                            Path filePath = Paths.get(file);
                            String content = Files.readString(filePath);
                            out.print(content);
                        } catch (IOException e) {
                            System.err.println("cat: " + file + ": No such file or directory");
                        }
                    }
                }
            }
            default -> {
                // Other built-ins
            }
        }
        
        out.flush();
    }
}