package zeroqu.org.dejavulyrics.service;

import com.google.api.services.youtube.model.Video;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zeroqu.org.dejavulyrics.adapter.ElasticSearchAdapter;
import zeroqu.org.dejavulyrics.model.ResponseDocument;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public abstract class BasicService {
    protected final RestHighLevelClient client;
    protected final YoutubeService youtubeService;

    protected static final String INDEX_NAME = "dejavulyrics";
    private static final Logger logger = LoggerFactory.getLogger(BasicService.class.getName());

    @Autowired
    protected BasicService(ElasticSearchAdapter elasticSearchAdapter, YoutubeService youtubeService) {
        this.client = elasticSearchAdapter.restClient();
        this.youtubeService = youtubeService;
    }

    protected void generateNetScore(Map<String, ResponseDocument> elasticResponse) {
        elasticResponse.values().forEach(response -> {
            String youtubeQuery = response.get_source().getSong_title() + " " + response.get_source().getSong_artist();
            try {
                Video video = youtubeService.searchMostRelevantVideo(youtubeQuery);
                response.get_source().setPublishDate(video.getSnippet().getPublishedAt().getValue());
                response.get_source().setViews(video.getStatistics().getViewCount().longValue());
                response.get_source().setVideoId(video.getId());
            } catch (Exception e) {
                logger.error(String.format("msg=\"Failed to receive song information from Youtube\" " +
                        "song=\"%s\" error=%s", youtubeQuery, e.getMessage()));
                response.get_source().setPublishDate(0L);
                response.get_source().setViews(0L);
                response.get_source().setVideoId("rmtjl6E_HsI");
            }
        });

        logger.info(String.format("msg=\"Start generating net score\""));
        long maxViews = elasticResponse.values().stream()
                .map(response -> response.get_source().getViews())
                .max(Long::compareTo)
                .get();

        double maxStandardScore = elasticResponse.values().stream()
                .map(ResponseDocument::get_score)
                .max(Double::compareTo)
                .get();

        elasticResponse.values().forEach(response -> {
            double normalizedStandardScore = response.get_score() / maxStandardScore;
            double normalizedPublishDate = response.get_source().getPublishDate() * 1.0 / new Date().getTime();
            double normalizedViews = maxViews == 0L ? 0.0 : response.get_source().getViews() * 1.0 / maxViews;
//            double searchTier = response.getQueryType() == null ?
//                    response.getSuggestType().getOrder() * 1.0 / SuggestType.MAX_ORDER :
//                    response.getQueryType().getOrder() * 1.0 / QueryType.MAX_ORDER;
            double netScore = 0.5 * normalizedStandardScore + 0.2 * normalizedPublishDate + 0.3 * normalizedViews;
            response.get_source().setNetScore(netScore);
        });
    }

    protected void sortByNetScore(List<ResponseDocument> responseDocuments) {
        responseDocuments.sort((a, b) -> {
            if (a.getQueryType() != null) {
                if (b.getQueryType().getOrder() == a.getQueryType().getOrder()) {
                    return Double.compare(b.get_source().getNetScore(), a.get_source().getNetScore());
                }
                return b.getQueryType().getOrder() - a.getQueryType().getOrder();
            } else {
                if (b.getSuggestType().getOrder() == a.getSuggestType().getOrder()) {
                    return Double.compare(b.get_source().getNetScore(), a.get_source().getNetScore());
                }
                return b.getSuggestType().getOrder() - a.getSuggestType().getOrder();
            }
        }
        );
    }

    protected void sortByStandardScore(List<ResponseDocument> responseDocuments) {
        responseDocuments.sort((a, b) -> {
            if (a.getQueryType() != null) {
                if (b.getQueryType().getOrder() == a.getQueryType().getOrder()) {
                    return Double.compare(b.get_score(), a.get_score());
                }
                return b.getQueryType().getOrder() - a.getQueryType().getOrder();
            } else {
                if (b.getSuggestType().getOrder() == a.getSuggestType().getOrder()) {
                    return Double.compare(b.get_score(), a.get_score());
                }
                return b.getSuggestType().getOrder() - a.getSuggestType().getOrder();
            }
        });
    }
}
