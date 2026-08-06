package org.neteasemod;

public class QuickPlayParseHelper {

    public static String SERVER_HOST = null;

    public static int SERVER_PORT = 25565;


    public static void parseLaunchArgs(String[] args) {
        SERVER_HOST = null;
        SERVER_PORT = 25565;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];


            if (arg.startsWith("--quickPlayMultiplayer=")) {
                String rawValue = arg.substring("--quickPlayMultiplayer=".length());
                splitHostPort(rawValue);
                return;
            }


            if ("--quickPlayMultiplayer".equals(arg)) {
                if (i + 1 < args.length) {
                    String rawValue = args[i + 1];
                    splitHostPort(rawValue);
                }
                return;
            }

            if ("--server".equals(arg) && i + 1 < args.length) {
                SERVER_HOST = args[i + 1];
            }


            if ("--port".equals(arg) && i + 1 < args.length) {
                try {
                    SERVER_PORT = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException ignore) {
                    SERVER_PORT = 25565;
                }
            }
        }
    }


    private static void splitHostPort(String raw) {
        String[] parts = raw.split(":", 2);
        SERVER_HOST = parts[0];
        if (parts.length >= 2) {
            try {
                SERVER_PORT = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignore) {
                SERVER_PORT = 25565;
            }
        }
    }

    public static boolean hasServerToConnect() {
        return SERVER_HOST != null && !SERVER_HOST.isBlank();
    }
}