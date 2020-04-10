package zeroqu.org.dejavulyrics.builder;

import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.*;
import zeroqu.org.dejavulyrics.util.FieldName;

public class QueryBuilderFactory {
    public static QueryBuilder build(String query, FieldName fieldName, QueryType queryType) {
        switch (queryType) {
            case MULTI_MATCH:
                return new MultiMatchQueryBuilder(query,
                        FieldName.song_artist.toString(), FieldName.song_title.toString())
                        .tieBreaker(1);
            case MULTI_FUZZY_MATCH:
                return new MultiMatchQueryBuilder(query,
                        FieldName.song_artist.toString(), FieldName.song_title.toString())
                        .tieBreaker(1)
                        .fuzziness(Fuzziness.AUTO);
            case MATCH_PHRASE:
                return new MatchPhraseQueryBuilder(fieldName.toString(), query);
            case MATCH_PREFIX_PHRASE:
                return new MatchPhrasePrefixQueryBuilder(fieldName.toString(), query)
                        .slop(10);
            case FUZZY_MATCH:
                return new MatchQueryBuilder(fieldName.toString(), query)
                        .fuzziness(Fuzziness.AUTO);
            default:
                throw new IllegalArgumentException("Invalid field type for query builder");
        }
    }
}
