package gg.discord.tj.bot.app;

import gg.discord.tj.bot.core.Bot;
import gg.discord.tj.bot.core.TJBot;

public class Application
{
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.out.println("Required: token");

            return;
        }

        Bot bot = new TJBot(args[0]);

        while (true)
        {
            try
            {
                bot.start();
            } catch (Throwable thr)
            {
                thr.printStackTrace();

                bot.reset();
                System.gc();
            }
        }
    }
}