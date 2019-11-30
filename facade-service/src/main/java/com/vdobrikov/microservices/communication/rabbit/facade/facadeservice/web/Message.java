package com.vdobrikov.microservices.communication.rabbit.facade.facadeservice.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Message {
    @NotEmpty
    private String topic;
    @Nullable
    private String forwardToTopic;
    @NotEmpty
    private String message;
}
