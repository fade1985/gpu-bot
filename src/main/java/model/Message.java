package model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import deserializer.TextDeserializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class Message {

    private int id;
    private Date date;

    @JsonDeserialize(using = TextDeserializer.class)
    private Object text;
}
