package manager;


import client.KVTaskClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import task.Epic;
import task.Subtask;
import task.Task;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTaskManager {

    private final Gson gson;
    private final KVTaskClient kvTaskClient;

    public HttpTaskManager(String url) {
        super();
        kvTaskClient = new KVTaskClient(url);
        gson = new Gson();
    }

    public void save() {
        kvTaskClient.put("tasks", gson.toJson(this.getTasks()));
        kvTaskClient.put("epics", gson.toJson(this.getEpics()));
        kvTaskClient.put("subtasks", gson.toJson(this.getSubtasks()));

        List<Integer> history = this.historyManager.getHistory().stream().map(Task::getId)
                .collect(Collectors.toList());
        kvTaskClient.put("history", gson.toJson(history));
    }

    public HttpTaskManager load(String url) {
        HttpTaskManager httpTaskManager = new HttpTaskManager(url);
        addTasksInManager(httpTaskManager, "tasks");
        addTasksInManager(httpTaskManager, "epics");
        addTasksInManager(httpTaskManager, "subtasks");
        addTasksInManager(httpTaskManager, "history");

        return httpTaskManager;
    }

    private void addTasksInManager(HttpTaskManager httpTaskManager, String key) {

        switch (key) {
            case "tasks":
                List<Task> tasks = gson.fromJson(kvTaskClient.load(key), new TypeToken<List<Task>>() {}.getType());
                if (!Objects.isNull(tasks)) {
                    tasks.forEach(httpTaskManager::addTask);
                }
                    break;
            case "epics":
                List<Epic> epics = gson.fromJson(kvTaskClient.load(key), new TypeToken<List<Epic>>() {}.getType());
                if (!Objects.isNull(epics)) {
                    epics.forEach(httpTaskManager::addEpic);
                }
                break;
            case "subtasks":
                List<Subtask> subtasks = gson.fromJson(kvTaskClient.load(key), new TypeToken<List<Subtask>>() {}.getType());
                if (!Objects.isNull(subtasks)) {
                    subtasks.forEach(httpTaskManager::addTask);
                }
                break;
            case "history":
                List<Integer> history = gson.fromJson(kvTaskClient.load("history"), new TypeToken<List<Integer>>() {}.getType());
                if (!Objects.isNull(history)) {
                    history.forEach(httpTaskManager::getTaskForId);
                }
                break;
            }
    }

    public Task getTaskForId(int id) {
        if (tasks.containsKey(id)) {
            historyManager.add(tasks.get(id));
            return tasks.get(id);
        }
        if (epics.containsKey(id)) {
            historyManager.add(epics.get(id));
            return epics.get(id);
        }
        if (subtasks.containsKey(id)) {
            historyManager.add(subtasks.get(id));
            return subtasks.get(id);
        }
        return null;
    }
}