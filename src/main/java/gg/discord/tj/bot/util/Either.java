package gg.discord.tj.bot.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class Either<L, R>
{
    private final L left;
    private final R right;

    private Either(L left, boolean unused) {
        this.left = left;
        this.right = null;
    }

    private Either(R right) {
        this.left = null;
        this.right = right;
    }

    public static <L> Either<L, ?> left(L obj) {
        return new Either<>(obj, true);
    }

    public static <R> Either<?, R> right(R obj) {
        return new Either<>(obj);
    }
}