package com.lazy.todo.payload.response;

import com.lazy.todo.payload.request.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

//our response from ChatGPT
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MotivationResponse {

    private List<Choice> choices;

}
