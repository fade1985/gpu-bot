package model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Chat {

    private List<Message> messages;
}
