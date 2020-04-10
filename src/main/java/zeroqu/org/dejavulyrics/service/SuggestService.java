package zeroqu.org.dejavulyrics.service;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zeroqu.org.dejavulyrics.adapter.ElasticSearchAdapter;
import zeroqu.org.dejavulyrics.builder.SuggestBuilderFactory;
import zeroqu.org.dejavulyrics.builder.SuggestType;
import zeroqu.org.dejavulyrics.model.ResponseDocument;
import zeroqu.org.dejavulyrics.parser.SuggestResponseParser;
import zeroqu.org.dejavulyrics.util.FieldName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SuggestService extends BasicService {
    private static final Logger logger = LoggerFactory.getLogger(SuggestService.class.getName());

    @Autowired
    protected SuggestService(ElasticSearchAdapter elasticSearchAdapter, YoutubeService youtubeService) {
        super(elasticSearchAdapter, youtubeService);
    }

    public List<ResponseDocument> suggest(String query, FieldName fieldName) throws IOException {
        Map<String, ResponseDocument> elasticResponse = new ConcurrentHashMap<>();

        logger.info(String.format("msg=\"Start autocompletion suggest\" query=\"%s\" field=%s",
                query, fieldName));

        SuggestBuilder suggestBuilder = SuggestBuilderFactory.build(query, fieldName, SuggestType.AUTO_COMPLETE);
        SearchResponse searchResponse = client.search(
                new SearchRequest(INDEX_NAME).source(
                        new SearchSourceBuilder().suggest(suggestBuilder)), RequestOptions.DEFAULT);
        logger.info(String.format("msg=\"Successfully received autocompletion suggest response\" query=\"%s\" field=%s",
                query, fieldName));

        CompletionSuggestion completionSuggestion = searchResponse.getSuggest().getSuggestion(fieldName.toString());
        SuggestResponseParser.parse(completionSuggestion.getOptions(), elasticResponse, SuggestType.AUTO_COMPLETE);

        List<ResponseDocument> responseDocuments = new ArrayList<>(elasticResponse.values());
        sortByStandardScore(responseDocuments);

        return responseDocuments;
    }
}
