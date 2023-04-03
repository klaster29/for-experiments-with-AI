package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.Collection;
import java.util.List;

public interface TaskManager {

    Collection<Task> getTasks();

    Collection<Epic> getEpics();

    Collection<Subtask> getSubtasks();

    Collection<Subtask> getEpicSubtasks(int epicId);

    Task getTask(int taskId);

    Epic getEpic(int epicId);

    Subtask getSubtask(int subtaskId);

    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    void deleteTask(int taskId);

    void deleteEpic(int epicId);

    void deleteSubtask(int subtaskId);

    void deleteTasks();

    void deleteEpics();

    void deleteSubtasks();

    Collection<Task> getHistory();

    List<Task> getPrioritizedTasks();

}
