package net.hashcoding.simplerpc.common.utils;

import java.io.*;

/**
 * Created by MaoChuan on 2017/5/15.
 */
public class IOUtils {
    public static boolean isExists(File file) {
        return file.exists();
    }

    public static boolean isFile(File file) {
        return file.isFile();
    }

    public static boolean isFile(String name) {
        File file = new File(name);
        return isExists(file) && isFile(file);
    }

    public static byte[] readIntoBytes(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        int length = fileInputStream.available();
        byte[] data = new byte[length];
        int size = 0;
        while (size < length) {
            int res = fileInputStream.read(data, size, length);
            if (res == -1) {
                throw new IOException("unexcept end of file");
            }
            size += res;
        }
        return data;
    }

    public static byte[] readIntoBytes(String filename) throws IOException {
        File file = new File(filename);
        return readIntoBytes(file);
    }

    public static String readIntoString(
            InputStream inputStream, String charset) throws IOException {
        StringBuilder builder = new StringBuilder();
        byte[] bytes = new byte[1024];
        int size = 0;
        while ((size = inputStream.read(bytes)) != -1) {
            String str = new String(bytes, 0, size, charset);
            builder.append(str);
        }
        return builder.toString();
    }

    public static void writeToFile(String filename, byte[] bytes) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(filename);
        outputStream.write(bytes);
        outputStream.close();
    }
}
