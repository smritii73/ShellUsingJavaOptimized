package shell.commands;
import shell.core.*;
import java.io.File;

public class BuiltInCommand {
    
    public static void type(String[] args) {
        if (args.length >= 1) {
            String cmdToCheck = args[0];
            if (ShellState.BUILTINS.containsKey(cmdToCheck)) {
                System.out.println(cmdToCheck + " is a shell builtin");
            } else {
                boolean found = false;
                for (String dir : ShellState.DIRECTORIES) {
                    File file = new File(dir, cmdToCheck);
                    if (file.exists() && file.canExecute()) {
                        System.out.println(cmdToCheck + " is " + file.getAbsolutePath());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.out.println(cmdToCheck + ": not found");
                }
            }
        } else {
            System.out.println("type: missing argument");
        }
    }

    public static void echo(String[] args) {
        if (args.length > 0) {
            System.out.println(String.join(" ", args));
        } else {
            System.out.println();
        }
    }

    public static void pwd() {
        System.out.println(ShellState.getCurrentDir().getAbsolutePath());
    }

    public static void cd(String[] args) {
        if (args.length >= 1) {
            String target = args[0];
            File targetDir;
            
            if (target.equals("~")) {
                String home = System.getenv("HOME");
                targetDir = (home != null) ? new File(home) : new File(System.getProperty("user.home"));
            } else if (new File(target).isAbsolute()) {
                targetDir = new File(target);
            } else {
                targetDir = new File(ShellState.getCurrentDir(), target);
            }
            
            try {
                java.nio.file.Path normalizedPath = targetDir.toPath().toRealPath();
                targetDir = normalizedPath.toFile();
            } catch (java.io.IOException e) {
                // If we can't normalize, use the original
            }
            
            if (targetDir.exists() && targetDir.isDirectory()) {
                ShellState.setCurrentDir(targetDir);
            } else {
                System.out.println("cd: " + target + ": No such file or directory");
            }
        } else {
            System.out.println("cd: missing argument");
        }
    }

    public static void cat(String[] files) {
        for (String file : files) {
            try {
                java.nio.file.Path filePath = java.nio.file.Paths.get(file);
                String content = java.nio.file.Files.readString(filePath);
                System.out.print(content);
            } catch (java.io.IOException e) {
                System.out.println("cat: " + file + ": No such file or directory");
            }
        }
    }
}