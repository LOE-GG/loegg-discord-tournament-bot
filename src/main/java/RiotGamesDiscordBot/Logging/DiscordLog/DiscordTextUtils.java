package RiotGamesDiscordBot.Logging.DiscordLog;

public class DiscordTextUtils {

    public static String colorRed(String message) {

        return "```diff" + "\n" + '-' +
                message + '\n' +
                "```";
    }

    public static String colorGreen(String message) {
        return "```diff" + '\n' + "+ "
                + message +
                "```";
    }

    public static String codeBlock(String message) {
        return "`" + message + "`";
    }

    public static String underline(String message) {
        return "__" + message + "__";
    }

    public static String blockComment(String message) {
        return "> " + message;
    }
}
