package gg.discord.tj.bot.command.impl;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import gg.discord.tj.bot.app.Application;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TagListCommand
    implements Command
{
    private static final int NO_OF_DISPLAY_COLUMNS = 3;
    private static final String MESSAGE_TEMPLATE = "All available tags:\n```\n%s\n```";

    @Override
    public String getName()
    {
        return "taglist";
    }

    @Override
    public Collection<String> getAliasses()
    {
        return List.of("tags");
    }

    @Override
    public void onExecute(CommandExecutionContext context)
    {
        var sortedListOfAvailableTags = Application.BOT_INSTANCE.getAvailableTags().keySet().stream()
                .sorted()
                .collect(Collectors.toList());
        var noOfDisplayRows = sortedListOfAvailableTags.size() % NO_OF_DISPLAY_COLUMNS == 0 ?
                sortedListOfAvailableTags.size() / NO_OF_DISPLAY_COLUMNS :
                (sortedListOfAvailableTags.size() / NO_OF_DISPLAY_COLUMNS) + 1;
        var displayDataArray = new String[noOfDisplayRows][NO_OF_DISPLAY_COLUMNS];
        var columns = new Column[NO_OF_DISPLAY_COLUMNS];
        Arrays.fill(columns, new Column().dataAlign(HorizontalAlign.LEFT));
        int i = 0, j = 0;
        for (String tag : sortedListOfAvailableTags) {
            displayDataArray[i++ % noOfDisplayRows][i % noOfDisplayRows == 0 ? j++ : j] = tag;
        }
        Objects.requireNonNull(context.getMessage().getChannel().block())
                .createMessage(String.format(MESSAGE_TEMPLATE, AsciiTable.getTable(
                        AsciiTable.NO_BORDERS, columns, displayDataArray)))
                .block();
    }
}