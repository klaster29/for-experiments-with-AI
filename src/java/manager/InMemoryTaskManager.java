package manager;

import task.*;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final SortedSet<Task> sortedTaskSet = new TreeSet<>();
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    private int newTaskId = 0;

    protected int getIdForNewTask() {
        return ++newTaskId;
    }

    @Override
    public Collection<Task> getTasks() {
        return Collections.unmodifiableCollection(tasks.values());
    }

    @Override
    public Collection<Epic> getEpics() {
        return Collections.unmodifiableCollection(epics.values());
    }

    @Override
    public Collection<Subtask> getSubtasks() {
        return Collections.unmodifiableCollection(subtasks.values());
    }

    @Override
    public Collection<Subtask> getEpicSubtasks(int epicId) {
        if (!epics.containsKey(epicId)) {
            return Collections.emptyList();
        }
        Epic epic = epics.get(epicId);
        if (epic.getSubtaskIds().isEmpty()) {
            return Collections.emptyList();
        }
        return subtasks.values().stream().filter(subtask -> subtask.getEpicId() == epicId).collect(Collectors.toList());
    }

    @Override
    public Task getTask(int taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void addTask(Task task) throws ManagerSaveException {
        if (hasTaskCrossroad(task)) {
            throw new TaskIntersectionException("Время задачи пересекается с уже добавленными. Метод addTask");
        }
        task.setId(getIdForNewTask());
        sortedTaskSet.add(task);
        tasks.put(task.getId(), task);
    }

    @Override
    public void addEpic(Epic epic) throws ManagerSaveException {
        epic.setId(getIdForNewTask());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void addSubtask(Subtask subtask) throws ManagerSaveException {
        if (hasTaskCrossroad(subtask)) {
            throw new TaskIntersectionException("Время задачи пересекается с уже добавленными. Метод addSubtask");
        }
        subtask.setId(getIdForNewTask());
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask.getId());
        calculateEpicTimes(subtask.getEpicId());
        addSubtaskAndUpdateHisEpic(subtask);
    }

    @Override
    public void updateTask(Task task) throws ManagerSaveException {
        if (hasTaskCrossroad(task)) {
            throw new TaskIntersectionException("Время задачи пересекается с уже добавленными. Метод updateTask");
        }
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            addToSortedTaskSet(task);
            sortedTaskSet.add(task);
        }
    }

    @Override
    public void updateEpic(Epic newEpic) {
        if (epics.containsKey(newEpic.getId())) {
            Epic epic = epics.get(newEpic.getId());
            epic.setName(newEpic.getName());
            epic.setDescription(newEpic.getDescription());
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) throws ManagerSaveException {
        int epicId = subtask.getEpicId();
        if (hasTaskCrossroad(subtask)) {
            throw new TaskIntersectionException("Время задачи пересекается с уже добавленными. Метод updateSubtask");
        }
        int subtaskId = subtask.getId();
        epics.forEach((integer, epic) -> epic.deleteSubtask(subtaskId));
        if (subtasks.containsKey(subtaskId)) {
            subtasks.put(subtaskId, subtask);
            addToSortedTaskSet(subtask);
        }
        epics.get(epicId).addSubtask(subtaskId);
        calculateEpicTimes(epicId);
        updateEpicStatus(epicId);
    }

    @Override
    public void deleteTask(int taskId) {
        sortedTaskSet.remove(tasks.get(taskId));
        tasks.remove(taskId);
        historyManager.remove(taskId);
    }

    @Override
    public void deleteEpic(int epicId) {
        Epic epic = epics.get(epicId);
        sortedTaskSet.remove(epic);
        sortedTaskSet.stream()
                .filter(task -> task.getType() == TaskType.SUBTASK)
                .filter(subtask -> ((Subtask) subtask).getEpicId() == epicId)
                .forEach(task -> sortedTaskSet.remove(epic));

        epic.getSubtaskIds().forEach(historyManager::remove);
        historyManager.remove(epicId);
        epics.remove(epicId);
        subtasks.values().removeIf(subtask -> subtask.getEpicId() == epicId);
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        int epicId = subtask.getEpicId();

        subtasks.remove(subtaskId);
        epics.get(epicId).deleteSubtask(subtaskId);
        sortedTaskSet.remove(subtask);

        updateEpicStatus(epicId);
        historyManager.remove(subtaskId);
    }

    @Override
    public void deleteTasks() {
        tasks.forEach((k, v) -> historyManager.remove(k));
        tasks.clear();
        List<Task> taskList = sortedTaskSet.stream().filter(task -> task.getType() == TaskType.TASK).collect(Collectors.toList());
        taskList.forEach(sortedTaskSet::remove);
    }

    @Override
    public void deleteEpics() {
        epics.forEach((k, v) -> historyManager.remove(k));
        deleteSubtasks();
        epics.clear();
        List<Task> taskList = sortedTaskSet.stream().filter(task -> task.getType() == TaskType.EPIC && task.getType() == TaskType.SUBTASK).collect(Collectors.toList());
        taskList.forEach(sortedTaskSet::remove);
    }

    @Override
    public void deleteSubtasks() {
        subtasks.forEach((k, v) -> historyManager.remove(k));
        subtasks.clear();
        epics.forEach((epicId, epic) -> epic.clearSubtasks());
        List<Task> taskList = sortedTaskSet.stream().filter(task -> task.getType() == TaskType.SUBTASK).collect(Collectors.toList());
        taskList.forEach(sortedTaskSet::remove);

        updateAllEpicsStatus();
    }


    @Override
    public Collection<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return List.copyOf(sortedTaskSet);
    }

    private boolean hasTaskCrossroad(Task task) {
        if (task.getStartTime() == null) {
            return false;
        }
        long taskStartTime = task.getStartTime().getTime();
        long taskEndTime = task.getEndTime().getTime();

        for (Task targetTask : sortedTaskSet) {
            if (task.getType() == TaskType.EPIC || targetTask.getType() == TaskType.EPIC) {
                continue;
            }

            if (targetTask.getId() == task.getId()) {
                continue;
            }

            if (targetTask.getStartTime() == null) {
                continue;
            }
            long targetTaskStartTime = targetTask.getStartTime().getTime();
            long targetTaskEndTime = targetTask.getEndTime().getTime();

            if ((targetTaskStartTime >= taskStartTime && targetTaskStartTime < taskEndTime) ||
                    (targetTaskEndTime > taskStartTime && targetTaskEndTime <= taskEndTime) ||
                    (targetTaskStartTime >= taskStartTime && targetTaskEndTime <= taskEndTime)) {
                return true;
            }
        }
        return false;
    }

    protected void updateEpicStatus(int epicId) {
        if (epics.get(epicId).getSubtaskIds().isEmpty()) {
            epics.get(epicId).setStatus(TaskStatus.NEW);
            return;
        }
        boolean isNew = true;
        if (subtasks.isEmpty()) {
            epics.get(epicId).setStatus(TaskStatus.NEW);
            return;
        }
        for (int subtaskId : epics.get(epicId).getSubtaskIds()) {
            if (subtasks.containsKey(subtaskId)) {
                if (!(subtasks.get(subtaskId).getStatus() == TaskStatus.NEW)) {
                    isNew = false;
                    break;
                }
            }
        }
        if (isNew) {
            epics.get(epicId).setStatus(TaskStatus.NEW);
            return;
        }

        boolean isDone = true;
        for (int subtaskId : epics.get(epicId).getSubtaskIds()) {
            if (!(subtasks.get(subtaskId).getStatus() == TaskStatus.DONE)) {
                isDone = false;
                break;
            }
        }
        if (isDone) {
            epics.get(epicId).setStatus(TaskStatus.DONE);
            return;
        }

        epics.get(epicId).setStatus(TaskStatus.IN_PROGRESS);
    }

    private void addSubtaskAndUpdateHisEpic(Subtask subtask) {
        sortedTaskSet.add(subtask);
        updateEpic(epics.get(subtask.getEpicId()));
        updateEpicStatus(subtask.getEpicId());
    }

    private void addToSortedTaskSet(Task task) {
        sortedTaskSet.removeIf(targetTask -> task.getId() == targetTask.getId());
        sortedTaskSet.add(task);
    }

    private void updateAllEpicsStatus() {
        for (Epic epic : epics.values()) {
            updateEpicStatus(epic.getId());
        }
    }

    private void calculateEpicTimes(Integer epicId) {
        Epic epic = epics.get(epicId);
        List<Subtask> list = subtasks.values().stream().filter(subtask -> Objects.equals(subtask.getEpicId(), epicId)).collect(Collectors.toList());
        if (!list.isEmpty()) {
            epic.setStartTime(findMinStartTime(list));
            epic.setDuration(calculateEpicDuration(list));
            epic.setEndTime(findMaxEndTime(list));
        }
    }

    private Date findMaxEndTime(List<Subtask> list) {
        Date date = null;
        for (Subtask subtask : list) {
            Date subtaskEndTime = subtask.getEndTime();
            if (subtaskEndTime != null) {
                if (date == null) {
                    date = subtaskEndTime;
                    continue;
                }
                if (subtaskEndTime.getTime() > date.getTime()) {
                    date = subtaskEndTime;
                }
            }
        }
        return date;
    }

    private Date findMinStartTime(List<Subtask> list) {
        Date minDate = null;
        for (Subtask subtask : list) {
            Date subtaskStartTime = subtask.getStartTime();
            if (subtaskStartTime == null) {
                continue;
            }
            if (minDate == null || subtaskStartTime.before(minDate)) {
                minDate = subtaskStartTime;
            }
        }
        return minDate;
    }

    private int calculateEpicDuration(List<Subtask> list) {
        int epicDuration = 0;
        for (Subtask subtask : list) {
            epicDuration += subtask.getDuration();
        }
        return epicDuration;
    }
}
