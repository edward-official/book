package org.zerock.week1.todo.service;

import org.zerock.week1.todo.dto.TodoDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum TodoService {
    INSTANCE;
    public void register(TodoDTO todoDTO) {
        System.out.println(String.format("DEBUG..........%s", todoDTO));
    }
    public List<TodoDTO> getList() {
        List<TodoDTO> todoDTOS = IntStream.range(0,10).mapToObj(i -> {
            TodoDTO dto = new TodoDTO();
            dto.setTno((long)i);
            dto.setTitle(String.format("Todo......%d", i));
            dto.setDueDate(LocalDate.now());

            return dto;
        }).collect(Collectors.toList());
        return todoDTOS;
    }
}
