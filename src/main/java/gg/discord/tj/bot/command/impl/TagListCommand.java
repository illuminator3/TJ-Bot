package gg.discord.tj.bot.command.impl;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;
import gg.discord.tj.bot.app.Application;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static gg.discord.tj.bot.util.MessageTemplate.TAGLIST_MESSAGE_TEMPLATE;

public class TagListCommand implements Command {
    private static final int NO_OF_DISPLAY_COLUMNS = 3;

    @Override
    public String getName() {
        return "taglist";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("tags");
    }

    @Override
    public String getDescription() {
        return "Print the list of available tags.";
    }

    @Override
    public Mono<Void> onExecute(CommandExecutionContext context) {
        var sortedListOfAvailableTags = Application.BOT_INSTANCE.getAvailableTags().keySet().stream()
            .sorted()
            .toList();
        int noOfDisplayRows = sortedListOfAvailableTags.size() % NO_OF_DISPLAY_COLUMNS == 0 ?
            sortedListOfAvailableTags.size() / NO_OF_DISPLAY_COLUMNS :
            (sortedListOfAvailableTags.size() / NO_OF_DISPLAY_COLUMNS) + 1;
        String[][] displayDataArray = new String[noOfDisplayRows][NO_OF_DISPLAY_COLUMNS];
        Column[] columns = new Column[NO_OF_DISPLAY_COLUMNS];

        Arrays.fill(columns, new Column().dataAlign(HorizontalAlign.LEFT));

        int i = 0, j = 0;

        for (String tag : sortedListOfAvailableTags) {
            displayDataArray[i++ % noOfDisplayRows][i % noOfDisplayRows == 0 ? j++ : j] = tag;
        }

        return context.message()
            .getChannel()
            .flatMap(channel -> channel == null ? Mono.empty() : channel.createMessage(String.format(TAGLIST_MESSAGE_TEMPLATE, AsciiTable.getTable(
                AsciiTable.NO_BORDERS, columns, displayDataArray))))
            .then();
    }
}
