package shell.core;

import java.util.ArrayList;
import java.util.List;

public class InputParser {
    
    public static List<String> parse(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingle = false;
        boolean inDouble = false;
        boolean escape = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escape) {
                if (inSingle) {
                    current.append('\\').append(c);
                } else if (inDouble) {
                    switch (c) {
                        case '$', '`', '"', '\\', '\n' -> current.append(c);
                        default -> current.append('\\').append(c);
                    }
                } else {
                    current.append(c);
                }
                escape = false;
                continue;
            }

            if (c == '\\') {
                escape = true;
                continue;
            }

            if (c == '\'' && !inDouble) {
                inSingle = !inSingle;
                continue;
            }

            if (c == '"' && !inSingle) {
                inDouble = !inDouble;
                continue;
            }

            if (Character.isWhitespace(c) && !inSingle && !inDouble) {
                if (current.length() > 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
                continue;
            }

            current.append(c);
        }

        if (escape) {
            current.append('\\');
        }

        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
    }
}