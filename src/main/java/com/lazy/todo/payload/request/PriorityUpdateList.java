package com.lazy.todo.payload.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PriorityUpdateList {

    private List<PriorityUpdate> priorityUpdates;

}
