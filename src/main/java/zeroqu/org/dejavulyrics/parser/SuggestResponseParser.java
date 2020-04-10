package zeroqu.org.dejavulyrics.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zeroqu.org.dejavulyrics.builder.SuggestType;
import zeroqu.org.dejavulyrics.model.ResponseDocument;

import java.util.List;
import java.util.Map;

public class SuggestResponseParser {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static Logger logger = LoggerFactory.getLogger(SuggestResponseParser.class.getName());

    public static void parse(List<CompletionSuggestion.Entry.Option> options, Map<String, ResponseDocument> originalResponse, SuggestType suggestType) {
        options.forEach(
                option -> {
                    String id = option.getHit().getId();
                    if (!originalResponse.containsKey(id)) {
                        try {
                            ResponseDocument responseDocument = objectMapper.readValue(option.getHit().toString(),
                                    ResponseDocument.class);
                            responseDocument.setText(option.getText().string());
                            responseDocument.setSuggestType(suggestType);
                            originalResponse.put(id, responseDocument);
                        } catch (Exception e) {
                            logger.error(String.format("msg=\"Failed to parse suggest response, skipping result\" error=%s",
                                    e.getMessage()));
                        }
                    }
                }
        );
    }
}
