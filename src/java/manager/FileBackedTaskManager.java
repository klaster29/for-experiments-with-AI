package manager;

import task.*;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private File taskDBFile;

    private final static String FILE_HEADER = "id,type,name,status,description,startTime,duration,endTime,epic";

    public FileBackedTaskManager(String taskDBFile) {
        super();
        this.taskDBFile = Path.of(URI.create(taskDBFile)).toFile();
    }

    public FileBackedTaskManager() {}

    public static FileBackedTaskManager loadFromFile(File file) throws RuntimeException {

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());
        if (file.length() < FILE_HEADER.length()) {
            return manager;
        }
        try {
            String data = Files.readString(Path.of(String.valueOf(file)), StandardCharsets.UTF_8);
            List<String> separatedData = new ArrayList<>(List.of(data.split(System.lineSeparator())));
            System.out.println(System.lineSeparator());

            String lastString = separatedData.get(separatedData.size() - 1);
            boolean haveHistory = hasHistory(separatedData);
            manager.prepareSeparatedData(separatedData, haveHistory);
            manager.createCorrectTasksAndPutInCollection(separatedData, manager);
            if (haveHistory) {
                List<Integer> historyIds = historyFromString(lastString);
                manager.fillHistoryFromListIds(historyIds, manager);
            }
        } catch (IOException | NullPointerException e) {
            throw new ManagerSaveException("Something went wrong in loadFromFile() method", e);
        }
        return manager;
    }

    @Override
    public Task getTask(int taskId) {
        Task task = super.getTask(taskId);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = super.getEpic(epicId);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtask(int subtaskId) {
        Subtask subtask = super.getSubtask(subtaskId);
        save();
        return subtask;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int taskId) {
        super.deleteTask(taskId);
        save();
    }

    @Override
    public void deleteEpic(int epicId) {
        super.deleteEpic(epicId);
        save();
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        super.deleteSubtask(subtaskId);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    private static Boolean hasHistory(List<String> separatedData) {
        return separatedData.get(separatedData.size() - 2).equals("");
    }

    private void createCorrectTasksAndPutInCollection(List<String> separatedData, FileBackedTaskManager manager)
            throws RuntimeException{
        for (String str : separatedData) {
            Task task = manager.taskFromString(str);
            switch (task.getType()) {
                case TASK:
                    manager.addTask(task);
                    break;
                case SUBTASK:
                    manager.addSubtask((Subtask) task);
                    break;
                case EPIC:
                    manager.addEpic((Epic) task);
                    break;
            }
        }
    }

    private void prepareSeparatedData(List<String> separatedData, boolean haveHistory) {
        if (haveHistory) {
            separatedData.remove(0);
            separatedData.remove(separatedData.size() - 1);
            separatedData.remove(separatedData.size() - 1);
        } else {
            separatedData.remove(0);
        }
    }

    private void fillHistoryFromListIds(List<Integer> historyIds, FileBackedTaskManager manager) {
        for (Integer id : historyIds) {
            if (manager.tasks.containsKey(id)) {
                manager.getTask(id);
            }
            if (manager.subtasks.containsKey(id)) {
                manager.getSubtask(id);
            }
            if (manager.epics.containsKey(id)) {
                manager.getEpic(id);
            }
        }
    }

    private Task taskFromString(String str) throws RuntimeException {
        String[] task = str.split(",");
        Date startDate = stringToDate(task[5].strip());
        switch (TaskType.valueOf(task[1])) {
            case TASK:
                return new Task(task[2].strip(), task[4].strip(), toTaskStatus(task[3].strip()),
                        Integer.parseInt(task[6].strip()), startDate);
            case SUBTASK:
                return new Subtask(task[2].strip(), task[4].strip(), toTaskStatus(task[3].strip()),
                        Integer.parseInt(task[6].strip()), startDate, Integer.parseInt(task[8].strip()));
            case EPIC:
                return new Epic(task[2].strip(), toTaskStatus(task[3].strip()), task[4].strip());
        }
        throw new NullPointerException("Метод taskFromString не выполняет ни одно из условий" + task[1]);
    }

    private Date stringToDate(String date) throws RuntimeException {
        if (date.equals("null")) {
            return null;
        }
        Date result;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            result = formatter.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException("Не получилось преобразовать строку в дату - шаблон следующий: dd/MM/yyyy", e);
        }
        return result;
    }

    private TaskStatus toTaskStatus(String status) {

        if (status.equals(TaskStatus.NEW.toString())) {
            return TaskStatus.NEW;
        }
        if (status.equals(TaskStatus.IN_PROGRESS.toString())) {
            return TaskStatus.IN_PROGRESS;
        }
        if (status.equals(TaskStatus.DONE.toString())) {
            return TaskStatus.DONE;
        }
        throw new NullPointerException("Метод toTaskStatus не выполняет ни одно из условий" + status);
    }

    private static List<Integer> historyFromString(String lastString) {
        List<Integer> historyIds = new ArrayList<>();
        String[] historyStringIds = lastString.split(",");
        Arrays.stream(historyStringIds).forEach(str -> historyIds.add(Integer.parseInt(str.strip())));
        return historyIds;
    }

    private void save() {
        try (Writer writer = new FileWriter(taskDBFile.toString(), StandardCharsets.UTF_8)) {
            printHeader(writer);
            if (!tasks.isEmpty()) {
                saveTasksInFile(writer, tasks);
            }
            if (!epics.isEmpty()) {
                saveTasksInFile(writer, epics);
            }
            if (!subtasks.isEmpty()) {
                saveTasksInFile(writer, subtasks);
            }
            if (!historyManager.getHistory().isEmpty()) {
                writer.write(historyToString(historyManager));
            }
            writer.flush();
        } catch (IOException e) {
            throw new ManagerSaveException("Something went wrong in save() method", e);
        }
    }

    private void printHeader(Writer writer) throws IOException {
        writer.write(FILE_HEADER);
    }

    private void saveTasksInFile(Writer writer, HashMap<Integer, ? extends Task> tasks) {
        if (!tasks.isEmpty()) {
            tasks.values().forEach(task -> {
                try {
                    writer.write(System.lineSeparator() + task.toString());
                } catch (IOException e) {
                    throw new ManagerSaveException("Something went wrong in saveTasksInFile() method", e);
                }
            });
        }
    }

    private static String historyToString(HistoryManager manager) {
        List<Integer> resultList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        builder.append(String.valueOf(System.lineSeparator()).repeat(2));
        manager.getHistory().forEach(task -> resultList.add(task.getId()));
        for (int i = 0; i < resultList.size(); i++) {
            if (i == resultList.size() - 1) {
                builder.append(resultList.get(i));
                return builder.toString();
            }
            builder.append(resultList.get(i)).append(",");
        }

        return builder.toString();
    }
}
