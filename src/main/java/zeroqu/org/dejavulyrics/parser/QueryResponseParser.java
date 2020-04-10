package zeroqu.org.dejavulyrics.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zeroqu.org.dejavulyrics.builder.QueryType;
import zeroqu.org.dejavulyrics.model.ResponseDocument;

import java.util.Arrays;
import java.util.Map;

public class QueryResponseParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static Logger logger = LoggerFactory.getLogger(QueryResponseParser.class.getName());

    public static void parse(SearchHit[] hits, Map<String, ResponseDocument> originalResponse, QueryType queryType) {
        Arrays.stream(hits).forEach(
                hit -> {
                    if (!originalResponse.containsKey(hit.getId())) {
                        try {
                            ResponseDocument responseDocument = objectMapper.readValue(hit.toString(),
                                    ResponseDocument.class);
                            responseDocument.setQueryType(queryType);
                            originalResponse.put(hit.getId(), responseDocument);
                        } catch (Exception e) {
                            logger.error(String.format("msg=\"Failed to parse search response, skipping result\" error=%s",
                                    e.getMessage()));
                        }
                    }
                }
        );
    }
}
