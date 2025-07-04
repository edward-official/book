package org.zerock.week1.todo.dto;

import java.time.LocalDate;

public class TodoDTO {
    private Long tno;
    private String title;
    private LocalDate dueDate;
    private Boolean finished;

    public Long getTno() {return tno;}
    public void setTno(Long tno) {this.tno = tno;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public LocalDate getDueDate() {return dueDate;}
    public void setDueDate(LocalDate dueDate) {this.dueDate = dueDate;}
    public boolean isFinished() {return finished;}
    public void setFinished(Boolean finished) {this.finished = finished;}

    @Override
    public String toString() {
        return String.format("DTO{tno=%s, title='%s', dueDate=%s, finished=%s}", tno, title, dueDate, finished);
    }
}
