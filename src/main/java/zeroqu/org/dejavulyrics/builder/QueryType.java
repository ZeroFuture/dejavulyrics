package zeroqu.org.dejavulyrics.builder;

import lombok.Getter;

public enum QueryType {
    MATCH_PHRASE(5),
    MATCH_PREFIX_PHRASE(4),
    FUZZY_MATCH(3),
    MULTI_MATCH(2),
    MULTI_FUZZY_MATCH(1);

    @Getter
    private int order;

    public static final int MAX_ORDER = 5;

    QueryType(int order) {
        this.order = order;
    }
}
