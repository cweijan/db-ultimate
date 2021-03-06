package github.cweijan.ultimate.util.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import github.cweijan.ultimate.util.DateUtils;

import java.io.IOException;
import java.time.LocalTime;

/**
 * @author cweijan
 * @version 2019/9/5 11:38
 */
public class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
    @Override
    public LocalTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        return (LocalTime) DateUtils.toDateObject(jsonParser.getText().trim(), LocalTime.class);
    }
}
