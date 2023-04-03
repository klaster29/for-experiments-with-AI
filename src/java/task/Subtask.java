package task;

import java.util.Date;

public class Subtask extends Task {

    private int epicId;
    public Subtask(int epicId, String title, String description, TaskStatus status) {
        super(title, status, description);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, TaskStatus status,
                   int duration, Date startTime, int epicId) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return getId() + ","
                + getType().toString() + ","
                + getName() + ","
                + getStatus().toString() + ","
                + getDescription() + ","
                + printFormatDate(getStartTime()) + ","
                + getDuration() + ","
                + printFormatDate(getEndTime()) + ","
                + getEpicId();
    }
}
