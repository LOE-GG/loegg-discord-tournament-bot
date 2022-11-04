package RiotGamesDiscordBot;

import RiotGamesDiscordBot.Commands.DiscordBotCommands;
import RiotGamesDiscordBot.Tournament.TournamentManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.security.auth.login.LoginException;

@SpringBootApplication
public class TournamentBotApplication {
    private static JDA discordAPI;

    public static void main(String[] args) throws LoginException, InterruptedException {
        startDiscordAPI();
        TournamentManager.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> TournamentManager.getInstance().shutdown()));
        SpringApplication.run(TournamentBotApplication.class, args);
    }


    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
        };
    }

    public static void startDiscordAPI() throws LoginException, InterruptedException {
        if (discordAPI == null) {
            JDABuilder builder = JDABuilder.createDefault(System.getenv("DISCORD_BOT_TOKEN"));
            builder.setActivity(Activity.of(Activity.ActivityType.LISTENING, "You Bozos"));
            discordAPI = builder.build();
            discordAPI.awaitReady();

            discordAPI.addEventListener(new DiscordBotCommands(discordAPI));
        }

    }

}

