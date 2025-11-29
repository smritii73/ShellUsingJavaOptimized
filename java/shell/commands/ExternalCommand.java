package shell.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import shell.core.ShellState;

public class ExternalCommand {
    
    // Windows cmd.exe built-in commands
    private static final String[] CMD_BUILTINS = {
        "dir", "date", "time", "vol", "label", "tree", "more",
        "copy", "move", "del", "ren", "cls", "find", "sort",
        "set", "path", "prompt", "mkdir", "rmdir", "md", "rd"
    };
    
    public static void execute(List<String> parts) throws IOException {
        String command = parts.get(0);
        
        // Check if it's a cmd.exe built-in
        boolean isCmdBuiltin = false;
        for (String builtin : CMD_BUILTINS) {
            if (command.equalsIgnoreCase(builtin)) {
                isCmdBuiltin = true;
                break;
            }
        }
        
        // Run cmd.exe built-ins through cmd.exe
        if (isCmdBuiltin) {
            List<String> cmdParts = new ArrayList<>();
            cmdParts.add("cmd.exe");
            cmdParts.add("/c");
            cmdParts.addAll(parts);
            
            runProcess(cmdParts);
            return;
        }
        
        // Try to find executable in PATH with various extensions
        String[] extensions = {".exe", ".bat", ".cmd", ""};
        
        for (String dir : ShellState.DIRECTORIES) {
            for (String ext : extensions) {
                File file = new File(dir, command + ext);
                if (file.exists()) {
                    runProcess(parts);
                    return;
                }
            }
        }
        
        // Command not found
        System.out.println(command + ": command not found");
    }
    
    private static void runProcess(List<String> parts) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(parts);
        pb.directory(ShellState.getCurrentDir());
        pb.inheritIO();
        try {
            Process program = pb.start();
            program.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}