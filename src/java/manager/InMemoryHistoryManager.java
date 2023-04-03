package manager;

import task.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private final CustomLinkedList nodes = new CustomLinkedList();

    @Override
    public void add(Task task) {
        nodes.linkLast(task);
    }

    @Override
    public void remove(int id) {
        nodes.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return nodes.getTasks();
    }
}
