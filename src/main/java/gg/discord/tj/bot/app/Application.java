package gg.discord.tj.bot.app;

import gg.discord.tj.bot.core.TJBot;

public class Application
{
    public static void main(String[] args)
    {
        TJBot bot = new TJBot();

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