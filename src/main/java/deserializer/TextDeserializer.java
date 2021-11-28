package deserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;
import model.Info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TextDeserializer extends JsonDeserializer<Object> {
    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonToken jsonToken = p.currentToken();

        if (jsonToken == JsonToken.START_ARRAY) {
            List<Object> dataList = new ArrayList<>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                if (p.hasToken(JsonToken.VALUE_STRING)) {
                    dataList.add(p.getValueAsString());
                } else {
                    dataList.add(p.readValueAs(Info.class));
                }
            }
            return dataList;
        }
        return "";
    }
}
