package zeroqu.org.dejavulyrics.service;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zeroqu.org.dejavulyrics.adapter.ElasticSearchAdapter;
import zeroqu.org.dejavulyrics.builder.QueryBuilderFactory;
import zeroqu.org.dejavulyrics.builder.QueryType;
import zeroqu.org.dejavulyrics.model.ResponseDocument;
import zeroqu.org.dejavulyrics.parser.QueryResponseParser;
import zeroqu.org.dejavulyrics.util.FieldName;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SearchService extends BasicService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class.getName());
    private static final int MIN_RESPONSE_SIZE = 5;

    @Autowired
    protected SearchService(ElasticSearchAdapter elasticSearchAdapter, YoutubeService youtubeService) {
        super(elasticSearchAdapter, youtubeService);
    }

    public List<ResponseDocument> search(String query, FieldName fieldName) throws IOException {
        Map<String, ResponseDocument> elasticResponse = new ConcurrentHashMap<>();

        if (fieldName.equals(FieldName.song_composite)) {
            //title artist composite search
            logger.info(String.format("msg=\"Start multi match query\" query=\"%s\" field=%s",
                    query, fieldName));

            QueryBuilder multiMatchQuery = QueryBuilderFactory.build(query, fieldName, QueryType.MULTI_MATCH);
            SearchResponse multiMatchResponse = client.search(
                    new SearchRequest(INDEX_NAME).source(
                            new SearchSourceBuilder().query(multiMatchQuery)), RequestOptions.DEFAULT);

            logger.info(String.format("msg=\"Successfully received multi fuzzy match query response\" query=\"%s\" field=%s",
                    query, fieldName));
            QueryResponseParser.parse(multiMatchResponse.getHits().getHits(), elasticResponse, QueryType.MULTI_MATCH);

            if (elasticResponse.size() < MIN_RESPONSE_SIZE) {
                logger.info(String.format("msg=\"Start multi fuzzy match query\" query=\"%s\" field=%s",
                        query, fieldName));

                QueryBuilder multiFuzzyMatchQuery = QueryBuilderFactory.build(query, fieldName,
                        QueryType.MULTI_FUZZY_MATCH);
                SearchResponse multiFuzzyMatchResponse = client.search(
                        new SearchRequest(INDEX_NAME).source(
                                new SearchSourceBuilder().query(multiFuzzyMatchQuery)), RequestOptions.DEFAULT);

                logger.info(String.format("msg=\"Successfully received multi fuzzy match query response\" query=\"%s\" field=%s",
                        query, fieldName));
                QueryResponseParser.parse(multiFuzzyMatchResponse.getHits().getHits(), elasticResponse,
                        QueryType.MULTI_FUZZY_MATCH);
            }
        } else {
            //title, artist, document field search
            logger.info(String.format("msg=\"Start phrase match query\" query=\"%s\" field=%s", query, fieldName));

            QueryBuilder matchPhraseQueryBuilder = QueryBuilderFactory.build(query, fieldName, QueryType.MATCH_PHRASE);
            SearchResponse matchPhraseSearchResponse = client.search(
                    new SearchRequest(INDEX_NAME).source(
                            new SearchSourceBuilder().query(matchPhraseQueryBuilder)), RequestOptions.DEFAULT);
            QueryResponseParser.parse(matchPhraseSearchResponse.getHits().getHits(), elasticResponse, QueryType.MATCH_PHRASE);

            logger.info(String.format("msg=\"Successfully received phrase match query response\" query=\"%s\" field=%s",
                    query, fieldName));

            //fall back to phrase prefix
            if (elasticResponse.size() < MIN_RESPONSE_SIZE) {
                logger.info(String.format("msg=\"Start phrase prefix match query\" query=\"%s\" field=%s", query, fieldName));

                QueryBuilder matchPrefixPhraseQueryBuilder = QueryBuilderFactory.build(query, fieldName,
                        QueryType.MATCH_PREFIX_PHRASE);
                SearchResponse matchPrefixPhraseSearchResponse = client.search(
                        new SearchRequest(INDEX_NAME).source(
                                new SearchSourceBuilder().query(matchPrefixPhraseQueryBuilder)), RequestOptions.DEFAULT);
                QueryResponseParser.parse(matchPrefixPhraseSearchResponse.getHits().getHits(), elasticResponse,
                        QueryType.MATCH_PREFIX_PHRASE);

                logger.info(String.format("msg=\"Successfully received phrase prefix match query response\" query=\"%s\" field=%s",
                        query, fieldName));
            }

            //fall back to fuzzy match
            if (elasticResponse.size() < MIN_RESPONSE_SIZE) {
                logger.info(String.format("msg=\"Start fuzzy match query\" query=\"%s\" field=%s", query, fieldName));

                QueryBuilder fuzzyMatchQueryBuilder = QueryBuilderFactory.build(query, fieldName, QueryType.FUZZY_MATCH);
                SearchResponse fuzzyMatchSearchResponse = client.search(
                        new SearchRequest(INDEX_NAME).source(
                                new SearchSourceBuilder().query(fuzzyMatchQueryBuilder)), RequestOptions.DEFAULT);
                QueryResponseParser.parse(fuzzyMatchSearchResponse.getHits().getHits(), elasticResponse,
                        QueryType.FUZZY_MATCH);

                logger.info(String.format("msg=\"Successfully received fuzzy match query response\" query=\"%s\" field=%s",
                        query, fieldName));
            }
        }


        List<ResponseDocument> responseDocuments = new ArrayList<>(elasticResponse.values());

//        responseDocuments.forEach(responseDocument -> responseDocument.get_source().setVideoId("rmtjl6E_HsI"));

        generateNetScore(elasticResponse);
        sortByNetScore(responseDocuments);
//        sortByStandardScore(responseDocuments);


        if (elasticResponse.size() == 0) {
            logger.warn(String.format("msg=\"Failed to retrieve any relevant document\" query=\"%s\" field=%s",
                    query, fieldName.toString()));
            return new ArrayList<>();
        }

        logger.info(String.format("msg=\"Successfully sorted response based on net score\" query=\"%s\" field=%s",
                query, fieldName.toString()));

        return responseDocuments;
    }
}