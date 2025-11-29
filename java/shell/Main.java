package shell;

import java.util.List;
import shell.commands.BuiltInCommand;
import shell.commands.ExternalCommand;
import shell.core.InputParser;
import shell.core.ShellState;
import shell.enums.ShellType;
import shell.history.HistoryManager;
import shell.pipeline.PipelineExecutor;

public class Main {

    public static void main(String[] args) throws Exception {
        HistoryManager historyManager = new HistoryManager();
        
        boolean exit = false;
        
        while (!exit) {
            System.out.print("$ ");
            System.out.flush();
            
            String input = historyManager.readLineWithHistory();
            
            if (input == null) {
                break;
            }
            
            if (input.trim().isEmpty()) continue;
            
            input = input.trim();
            
            historyManager.addCommand(input);

            // Check for pipeline
            if (input.contains(" | ")) {
                PipelineExecutor.execute(input, historyManager);
                continue;
            }

            List<String> parts = InputParser.parse(input);
            if (parts.isEmpty()) continue;

            String command = parts.get(0);
            String[] arguments = parts.subList(1, parts.size()).toArray(new String[0]);

            if (ShellState.EXTERNALS.containsKey(command)) {
                switch (ShellState.EXTERNALS.getOrDefault(command, ShellType.NONE)) {
                    case CAT -> BuiltInCommand.cat(arguments);
                    default -> ExternalCommand.execute(parts);
                }
            } else {
                switch (ShellState.BUILTINS.getOrDefault(command, ShellType.NONE)) {
                    case EXIT -> exit = true;
                    case ECHO -> BuiltInCommand.echo(arguments);
                    case TYPE -> BuiltInCommand.type(arguments);
                    case PWD -> BuiltInCommand.pwd();
                    case CD -> BuiltInCommand.cd(arguments);
                    case HISTORY -> executeHistory(arguments, historyManager);
                    default -> ExternalCommand.execute(parts);
                }
            }
        }
        
        historyManager.saveToFile();
        
        System.out.println();
    }

    private static void executeHistory(String[] args, HistoryManager historyManager) {
        if (args.length >= 2 && args[0].equals("-a")) {
            historyManager.appendToFile(args[1]);
            return;
        }
        
        if (args.length >= 2 && args[0].equals("-w")) {
            historyManager.writeToFile(args[1]);
            return;
        }
        
        if (args.length >= 2 && args[0].equals("-r")) {
            historyManager.readFromFile(args[1]);
            return;
        }
        
        int limit = historyManager.getHistory().size();
        if (args.length > 0) {
            try {
                limit = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // Use default
            }
        }
        
        List<String> history = historyManager.getHistory();
        int start = Math.max(0, history.size() - limit);
        for (int i = start; i < history.size(); i++) {
            System.out.printf("%5d  %s%n", i + 1, history.get(i));
        }
    }
}