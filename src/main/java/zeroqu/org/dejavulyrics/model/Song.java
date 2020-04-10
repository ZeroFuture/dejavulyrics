package zeroqu.org.dejavulyrics.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class Song implements Serializable {
    //elastic search fields
    private String song_artist;
    private String song_title;
    private String song_lyrics;
    private String song_url;
    private String song_document;

    //youtube fields
    private long views;
    private long publishDate;
    private String videoId;

    //composite score
    private double netScore;
}
