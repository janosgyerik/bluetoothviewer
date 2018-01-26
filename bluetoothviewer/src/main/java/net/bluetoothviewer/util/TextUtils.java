package net.bluetoothviewer.util;

public class TextUtils {
    private TextUtils() {
        // utility class, forbidden constructor
    }

    public static String bytesToHexDump(byte[] bytes) {
        if (bytes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() == 1) {
                sb.append('0');
            }
            sb.append(hexString).append(' ');
        }
        return sb.substring(0, sb.length() - 1);
    }
}
