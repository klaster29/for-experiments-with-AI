package task;

import java.util.*;

public class Epic extends Task {

    private final List<Integer> subtaskIds = new ArrayList<>();
    Date endTime;

    public Epic(String title, TaskStatus status, String description) {
        super(title, status, description);
    }

    public Epic() {
    }

    public Collection<Integer> getSubtaskIds() {
        return Collections.unmodifiableCollection(subtaskIds);
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void deleteSubtask(int subtaskId) {
        subtaskIds.removeIf(subtask -> subtask == subtaskId);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Override
    public Date getEndTime() {
        return endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

}