package gg.discord.tj.bot.util;

public final class MessageTemplate {
    public static final String PLAINTEXT_MESSAGE_TEMPLATE = "```\n%s\n```",
        JAVA_MESSAGE_TEMPLATE = "```java\n%s\n```",
        KOTLIN_MESSAGE_TEMPLATE = "```kotlin\n%s\n```",
        SQL_MESSAGE_TEMPLATE = "```sql\n%s\n```",
        SHELL_MESSAGE_TEMPLATE = "```shell\n%s\n```",
        JAVASCRIPT_MESSAGE_TEMPLATE = "```javascript\n%s\n```",
        TAGLIST_MESSAGE_TEMPLATE = "All available tags:\n" + PLAINTEXT_MESSAGE_TEMPLATE;
}
