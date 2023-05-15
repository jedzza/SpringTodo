package com.lazy.todo.payload.response;

import com.lazy.todo.payload.request.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Choice {

    private int index;
    private Message message;

}