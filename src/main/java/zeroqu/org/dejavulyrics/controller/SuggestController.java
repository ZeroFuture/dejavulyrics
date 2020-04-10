package zeroqu.org.dejavulyrics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import zeroqu.org.dejavulyrics.model.ResponseDocument;
import zeroqu.org.dejavulyrics.service.SuggestService;
import zeroqu.org.dejavulyrics.util.FieldName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping(path = "/suggest")
public class SuggestController {
    private final SuggestService suggestService;
    private static final Logger logger = LoggerFactory.getLogger(SuggestController.class.getName());

    @Autowired
    public SuggestController(SuggestService suggestService) {
        this.suggestService = suggestService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> suggestHandler(@RequestParam("query") String query, @RequestParam("field") String field) {
        FieldName fieldName;
        try {
            fieldName = FieldName.valueOf(field);
        } catch (Exception e) {
            logger.error(String.format("msg=\"Invalid field type\" query=\"%s\" field=%s error=%s",
                    query, field, e.getMessage()));
            return ResponseEntity.badRequest().build();
        }

        try {
            Map<String, Object> responseMap = new HashMap<>();
            List<ResponseDocument> result = suggestService.suggest(query, fieldName);
            if (result.size() == 0) {
                return ResponseEntity.badRequest().build();
            }
            responseMap.put("results", result);
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(responseMap),
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error(String.format("msg=\"Failed processing query, internal error\" query=\"%s\" field=%s error=%s",
                    query, field, e.getMessage()));
            return ResponseEntity.badRequest().build();
        }
    }
}
