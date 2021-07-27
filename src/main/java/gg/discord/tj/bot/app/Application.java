package gg.discord.tj.bot.app;

import gg.discord.tj.bot.core.Bot;
import gg.discord.tj.bot.core.TJBot;

public class Application {
    public static Bot BOT_INSTANCE;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Required: token");

            return;
        }

        BOT_INSTANCE = new TJBot(args[0]);

        while (true) {
            try {
                BOT_INSTANCE.start();
            } catch (Throwable thr) {
                thr.printStackTrace();

                BOT_INSTANCE.reset();
//                System.gc();
            }
        }
    }
}
