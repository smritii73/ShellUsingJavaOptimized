package shell.pipeline;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {
    public static void pipeData(InputStream in, OutputStream out, boolean closeOut) {
        byte[] buffer = new byte[8192];
        try {
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
                out.flush();
            }
        } catch (IOException e) {
            // Stream closed or error
        } finally {
            closeQuietly(in);
            if (closeOut) {
                closeQuietly(out);
            }
        }
    }
    
    public static void closeQuietly(Closeable c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (IOException ignored) {
        }
    }
}