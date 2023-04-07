package manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {

    Path resultFilePath = Paths.get(RESULT_FILE_PATH);
    Path emptyFilePath = Paths.get(EMPTY_FILE_PATH);
    Path fileWithoutHistoryPath = Paths.get(FILE_WITHOUT_HISTORY);
    Path targetFilePath = Paths.get(TARGET_FILE_PATH);
    Path secondTargetFilePath = Paths.get(SECOND_TARGET_FILE_PATH);
    Path onePointHistoryFilePath = Paths.get(ONE_POINT_HISTORY_FILE_PATH);
    Path incorrectTimeFormatFile = Paths.get(INCORRECT_TIME_FORMAT_FILE);
    Path fileContainsCrossroadsTasks = Paths.get(FILE_CONTAINS_CROSSROADS_TASKS);


    public static String EMPTY_FILE_PATH = "src/testResources/emptyFile.csv";
    public static String FILE_WITHOUT_HISTORY = "src/testResources/fileWithoutHistory.csv";
    public static String TARGET_FILE_PATH = "src/testResources/target.csv";
    public static String RESULT_FILE_PATH = "src/testResources/result.csv";
    public static String SECOND_TARGET_FILE_PATH = "src/testResources/secondTarget.csv";
    public static String ONE_POINT_HISTORY_FILE_PATH = "src/testResources/fileWithOnePointHistory.csv";
    public static String INCORRECT_TIME_FORMAT_FILE = "src/testResources/incorrectTimeFormatFile.csv";
    public static String FILE_CONTAINS_CROSSROADS_TASKS = "src/testResources/fileContainsCrossroadsTasks.csv";
    public static long HEADER_SIZE = 63;
    public static int DEFAULT_EPIC_ID = 2;
    public static int DEFAULT_SUBTASK_ID = 3;

    @Test
    @BeforeEach
    @Override
    public void setup() {
        manager = new FileBackedTaskManager(resultFilePath.toFile());
    }

    @Test
    void testLoadFromFileMustLoadNothing() {
        manager = FileBackedTaskManager.loadFromFile(emptyFilePath.toFile());

        Assertions.assertEquals(ZERO_SIZE, manager.getTasks().size());
        Assertions.assertEquals(ZERO_SIZE, manager.getEpics().size());
        Assertions.assertEquals(ZERO_SIZE, manager.getSubtasks().size());
        Assertions.assertEquals(ZERO_SIZE, manager.getHistory().size());
    }

    @Test
    void testLoadFromFileWithoutHistory() {
        manager = FileBackedTaskManager.loadFromFile(fileWithoutHistoryPath.toFile());
        Assertions.assertEquals(ZERO_SIZE, manager.getHistory().size());
    }

    //переделать
    @Test
    void testLoadFromFileWithOnePointInHistory() {
        manager = FileBackedTaskManager.loadFromFile(onePointHistoryFilePath.toFile());
        Assertions.assertEquals(1, manager.getHistory().size());
    }

    @Test
    void testLoadFromFileMustLoadTasksCorrectly() {
        manager = FileBackedTaskManager.loadFromFile(targetFilePath.toFile());
        TaskManager expectedManager = new FileBackedTaskManager(resultFilePath.toFile());
        addDifferentTasksToManager(expectedManager);

        Assertions.assertEquals(manager.getTasks().size(), expectedManager.getTasks().size());
        Assertions.assertEquals(manager.getEpics().size(), expectedManager.getEpics().size());
        Assertions.assertEquals(manager.getSubtasks().size(), expectedManager.getSubtasks().size());

        for (int i = 0; i < manager.getTasks().size(); i++) {
            Assertions.assertEquals(manager.getTask(i), expectedManager.getTask(i));
        }
        for (int i = 0; i < manager.getEpics().size(); i++) {
            Assertions.assertEquals(manager.getEpic(i), expectedManager.getEpic(i));
        }
        for (int i = 0; i < manager.getSubtasks().size(); i++) {
            Assertions.assertEquals(manager.getSubtask(i), expectedManager.getSubtask(i));
        }
    }

    @Test
    void testLoadFromFileMustGiveAbilityToAppendTasksAfterDownloadingFromAFile() {

        manager = FileBackedTaskManager.loadFromFile(resultFilePath.toFile());
        manager.addTask(new Task("Task2", TaskStatus.NEW, "Description task2"));

        try {
            Assertions.assertTrue(fileEquals(secondTargetFilePath, resultFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSaveMustWriteTheCorrectDataToAFile() {
        manager = new FileBackedTaskManager(resultFilePath.toFile());
        addDifferentTasksToManager(manager);

        try {
            Assertions.assertTrue(fileEquals(targetFilePath, resultFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSaveMustConsistsOnlyHeader() {
        manager = new FileBackedTaskManager(resultFilePath.toFile());
        manager.addTask(new Task());
        manager.deleteTasks();

        try {
            Assertions.assertEquals(HEADER_SIZE, Files.size(resultFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testLoadFromFileMustThrowRuntimeException() {
        Assertions.assertThrows(RuntimeException.class,
                () -> FileBackedTaskManager.loadFromFile(incorrectTimeFormatFile.toFile()));
    }

    @Test
    void testLoadFromFileMustThrowTaskInterSectionException() {
        Assertions.assertThrows(TaskIntersectionException.class,
                () -> FileBackedTaskManager.loadFromFile(fileContainsCrossroadsTasks.toFile()));
    }

    private void addDifferentTasksToManager(TaskManager manager) {
        manager.addTask(new Task("Task1", TaskStatus.NEW, "Description task1"));
        manager.addEpic(new Epic("Epic2", TaskStatus.DONE, "Description epic2"));
        manager.addSubtask(new Subtask(DEFAULT_EPIC_ID, "Sub Task3", "Description sub task3", TaskStatus.DONE));
        manager.getEpic(DEFAULT_EPIC_ID);
        manager.getSubtask(DEFAULT_SUBTASK_ID);
    }

    private boolean fileEquals(Path expected, Path current) throws IOException {
        try (BufferedReader bf1 = Files.newBufferedReader(current);
             BufferedReader bf2 = Files.newBufferedReader(expected)) {

            String line1, line2;
            while ((line1 = bf1.readLine()) != null) {
                line2 = bf2.readLine();
                if (!line1.equals(line2) || (bf1.readLine()) != null && (bf2.readLine()) == null) {
                    return false;
                }
            }
            return bf2.readLine() == null;
        }
    }
}