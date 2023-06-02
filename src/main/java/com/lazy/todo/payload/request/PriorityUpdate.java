package com.lazy.todo.payload.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PriorityUpdate {

    private Long id;
    private int priority;
}
