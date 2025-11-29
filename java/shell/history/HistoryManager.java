package shell.history;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryManager {
    private final List<String> commandHistory = new ArrayList<>();
    private final String histFile;
    private int historyLoadedCount = 0;
    private final Map<String, Integer> fileAppendIndex = new HashMap<>();

    public HistoryManager() {
        this.histFile = System.getenv("HISTFILE");
        loadFromFile();
    }

    public void addCommand(String command) {
        commandHistory.add(command);
    }

    public List<String> getHistory() {
        return commandHistory;
    }

    public int getHistoryLoadedCount() {
        return historyLoadedCount;
    }

    public void loadFromFile() {
        if (histFile == null || histFile.isEmpty()) {
            return;
        }
        
        File file = new File(histFile);
        if (!file.exists()) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    commandHistory.add(line);
                    historyLoadedCount++;
                }
            }
        } catch (IOException e) {
            // Silently ignore errors
        }
    }

    public void saveToFile() {
        if (histFile == null || histFile.isEmpty()) {
            return;
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(histFile))) {
            for (String cmd : commandHistory) {
                writer.write(cmd);
                writer.newLine();
            }
        } catch (IOException e) {
            // Silently ignore errors
        }
    }

    public void readFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    commandHistory.add(line);
                    historyLoadedCount++;
                }
            }
        } catch (IOException e) {
            System.err.println("history: " + filename + ": cannot read file");
        }
    }

    public void writeToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false))) {
            for (String cmd : commandHistory) {
                writer.write(cmd);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("history: " + filename + ": cannot write to file");
        }
    }

    public void appendToFile(String filename) {
        try {
            int startIndex = fileAppendIndex.getOrDefault(filename, historyLoadedCount);
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
                for (int i = startIndex; i < commandHistory.size(); i++) {
                    writer.write(commandHistory.get(i));
                    writer.newLine();
                }
            }
            
            fileAppendIndex.put(filename, commandHistory.size());
        } catch (IOException e) {
            System.err.println("history: " + filename + ": cannot write to file");
        }
    }

    public String readLineWithHistory() throws IOException {
        boolean rawModeEnabled = false;
        try {
            String[] enableCmd = {"/bin/sh", "-c", "stty raw -echo < /dev/tty"};
            Process p = Runtime.getRuntime().exec(enableCmd);
            p.waitFor();
            rawModeEnabled = (p.exitValue() == 0);
        } catch (Exception e) {
            // Couldn't enable raw mode
        }
        
        if (!rawModeEnabled) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        }
        
        try {
            StringBuilder line = new StringBuilder();
            int historyIndex = commandHistory.size();
            String savedLine = "";
            
            while (true) {
                int c = System.in.read();
                
                if (c == -1) {
                    return null;
                }
                
                if (c == '\n' || c == '\r') {
                    System.out.print("\r\n");
                    System.out.flush();
                    return line.toString();
                }
                
                if (c == 127 || c == 8) {
                    if (line.length() > 0) {
                        line.deleteCharAt(line.length() - 1);
                        System.out.print("\b \b");
                        System.out.flush();
                    }
                    continue;
                }
                
                if (c == 27) {
                    int next1 = System.in.read();
                    if (next1 == '[') {
                        int next2 = System.in.read();
                        
                        if (next2 == 'A') {
                            if (historyIndex > 0) {
                                if (historyIndex == commandHistory.size()) {
                                    savedLine = line.toString();
                                }
                                historyIndex--;
                                clearLine(line.length());
                                line.setLength(0);
                                line.append(commandHistory.get(historyIndex));
                                System.out.print(line);
                                System.out.flush();
                            }
                        } else if (next2 == 'B') {
                            if (historyIndex < commandHistory.size()) {
                                historyIndex++;
                                clearLine(line.length());
                                line.setLength(0);
                                if (historyIndex == commandHistory.size()) {
                                    line.append(savedLine);
                                } else {
                                    line.append(commandHistory.get(historyIndex));
                                }
                                System.out.print(line);
                                System.out.flush();
                            }
                        }
                    }
                    continue;
                }
                
                if (c == 3) {
                    System.out.print("^C\r\n");
                    System.out.flush();
                    return "";
                }
                
                if (c == 4) {
                    if (line.length() == 0) {
                        return null;
                    }
                    continue;
                }
                
                if (c >= 32 && c < 127) {
                    line.append((char) c);
                    System.out.print((char) c);
                    System.out.flush();
                }
            }
        } finally {
            try {
                String[] restoreCmd = {"/bin/sh", "-c", "stty sane < /dev/tty"};
                Runtime.getRuntime().exec(restoreCmd).waitFor();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private void clearLine(int length) {
        for (int i = 0; i < length; i++) {
            System.out.print("\b \b");
        }
        System.out.flush();
    }
}