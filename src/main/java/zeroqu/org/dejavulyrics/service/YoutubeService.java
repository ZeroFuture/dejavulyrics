package zeroqu.org.dejavulyrics.service;


import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zeroqu.org.dejavulyrics.adapter.YoutubeAdapter;

import java.io.IOException;

@Service
public class YoutubeService {
    private final YoutubeAdapter youtubeAdapter;
    private final YouTube youtube;

    private static final Logger logger = LoggerFactory.getLogger(YoutubeService.class.getName());

    @Autowired
    public YoutubeService(YoutubeAdapter youtubeAdapter) {
        this.youtubeAdapter = youtubeAdapter;
        this.youtube = youtubeAdapter.youTubeClient();
    }

    public Video searchMostRelevantVideo(String query) throws IOException {
        YouTube.Search.List search = youtube.search().list("id,snippet");
        search.setKey(youtubeAdapter.getApiKey());
        search.setQ(query);
        search.setType("video");
        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
        search.setMaxResults(1L);
        SearchResult searchResult = search.execute().getItems().iterator().next();
        YouTube.Videos.List stats = youtube.videos().list("snippet,statistics");
        stats.setId(searchResult.getId().getVideoId());
        stats.setKey(youtubeAdapter.getApiKey());
        stats.setMaxResults(1L);
        VideoListResponse statsResults = stats.execute();
        logger.info(String.format("msg=\"Successfully received video result\" videoId=%s",
                searchResult.getId().getVideoId()));
        return statsResults.getItems().iterator().next();
    }
}
