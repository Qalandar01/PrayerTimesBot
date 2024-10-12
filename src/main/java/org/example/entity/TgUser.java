package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.State;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TgUser {
    private Long chatId;
    private State state = State.START;
    private Integer messageId;
    private String region;

    public TgUser(Long chatId) {
        this.chatId = chatId;
    }
}
