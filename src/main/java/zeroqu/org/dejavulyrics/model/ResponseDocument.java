package zeroqu.org.dejavulyrics.model;

import lombok.Data;
import zeroqu.org.dejavulyrics.builder.QueryType;
import zeroqu.org.dejavulyrics.builder.SuggestType;

import java.io.Serializable;

@Data
public class ResponseDocument implements Serializable {
    private String _index;
    private String _type;
    private String _id;
    private double _score;
    private Song _source;
    private String text;
    private QueryType queryType;
    private SuggestType suggestType;
}
