package zeroqu.org.dejavulyrics.builder;

import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import zeroqu.org.dejavulyrics.util.FieldName;

public class SuggestBuilderFactory {
    public static SuggestBuilder build(String prefix, FieldName fieldName, SuggestType suggestType) {
        switch (suggestType) {
            case AUTO_COMPLETE:
                return new SuggestBuilder().addSuggestion(fieldName.toString(),
                        SuggestBuilders.completionSuggestion(fieldName.toString())
                                .prefix(prefix, Fuzziness.AUTO));
            default:
                throw new IllegalArgumentException("Invalid field type for suggest builder");
        }
    }
}
