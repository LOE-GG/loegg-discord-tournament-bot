package RiotGamesDiscordBot.Logging;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public static void log(String message, Level level) {
        //Current Thread
        String threadName = Thread.currentThread().getName();

        //Current Time
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("America/Denver"));
        DateTimeFormatter inFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
        String formattedDate = zonedDateTime.format(inFormat);

        String logInfo = ConsoleColors.CYAN + "[ " + threadName + " : " + formattedDate + " ] " + ConsoleColors.RESET;

        String levelInfo;
        switch (level) {
            case WARNING:
                levelInfo = ConsoleColors.YELLOW_BOLD + "WARNING" + ConsoleColors.RESET;
                break;
            case ERROR:
                levelInfo = ConsoleColors.RED_BOLD + "ERROR" + ConsoleColors.RESET;
                break;
            case INFO:
            default:
                levelInfo = ConsoleColors.GREEN_BOLD + "INFO" + ConsoleColors.RESET;
                break;
        }

        System.out.println(logInfo + levelInfo + " " + message);
    }
}
