package shell.core;

import java.io.File;
import java.util.Map;
import shell.enums.ShellType;

public class ShellState {
    public static final Map<String, ShellType> BUILTINS =
            Map.of(
                    "type", ShellType.TYPE,
                    "echo", ShellType.ECHO,
                    "exit", ShellType.EXIT,
                    "pwd", ShellType.PWD,
                    "cd", ShellType.CD,
                    "history", ShellType.HISTORY);
    
    public static final Map<String, ShellType> EXTERNALS = Map.of("cat", ShellType.CAT);

    public static final String PATH = System.getenv("PATH");
    public static final String[] DIRECTORIES = PATH != null ? PATH.split(File.pathSeparator) : new String[0];
    
    private static File currentDir = new File(System.getProperty("user.dir"));

    public static File getCurrentDir() {
        return currentDir;
    }

    public static void setCurrentDir(File dir) {
        currentDir = dir;
        System.setProperty("user.dir", currentDir.getAbsolutePath());
    }
}