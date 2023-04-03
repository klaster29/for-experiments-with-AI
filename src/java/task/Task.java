package task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public class Task implements Comparable<Task> {

    private int id;
    private String name;
    private String description;
    private TaskStatus status = TaskStatus.NEW;
    private int duration = 0;
    private Date startTime;

    public Task(String name, TaskStatus status, String description) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task() {
    }

    public Task(String name, String description, TaskStatus status, int duration, Date startTime) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getName() {
        return name;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        if (Objects.isNull(startTime)) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        calendar.add(Calendar.MINUTE, duration);
        return calendar.getTime();
    }

    protected String printFormatDate(Date date) {
        if (date == null) {
            return "null";
        }
        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy:HH-mm");
        return formatDate.format(date);
    }

    @Override
    public String toString() {
        return id + ","
                + getType().toString() + ","
                + name + ","
                + status.toString() + ","
                + description + ","
                + printFormatDate(getStartTime()) + ","
                + duration + ","
                + printFormatDate(getEndTime());
    }

    @Override
    public int compareTo(Task o) {
        Date thisStartTime = this.getStartTime();
        Date oStartTime = o.getStartTime();

        if (thisStartTime == null && oStartTime != null) {
            return 1;
        }
        if (thisStartTime != null && oStartTime == null) {
            return -1;
        }
        if (thisStartTime == null) {
            return Comparator.comparingInt(Task::getId).compare(this, o);
        }

        return Comparator.comparing(Task::getStartTime)
                .thenComparingInt(Task::getId)
                .compare(this, o);
    }
}
