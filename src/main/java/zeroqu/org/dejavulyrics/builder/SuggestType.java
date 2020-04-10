package zeroqu.org.dejavulyrics.builder;

import lombok.Getter;

public enum SuggestType {
    AUTO_COMPLETE(1);

    @Getter
    private int order;
    public static final int MAX_ORDER = 1;

    SuggestType(int order) {
        this.order = order;
    }
}
