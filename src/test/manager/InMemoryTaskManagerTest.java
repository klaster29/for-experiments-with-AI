package manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TaskStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

class InMemoryTaskManagerTest {

    TaskManager manager;
    public static int ZERO_SIZE = 0;
    public static int DEFAULT_EPIC_ID = 1;

    @Test
    @BeforeEach
    public void setup() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void getIdForNewTaskShouldIncrementId() {
        Task task = new Task();
        for (int i = 1; i < 4; i++) {
            manager.addTask(task);
            Assertions.assertEquals(i, task.getId());
        }
    }

    @Test
    void getTasksShouldReturnEmptyTasksCollection() {
        Assertions.assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    void getTasksShouldReturnCollectionWithThreeTasks() {
        addTasksInManager(manager, new Task(), 3);
        Assertions.assertEquals(3, manager.getTasks().size());
    }

    @Test
    void getEpicsShouldReturnEmptyEpicsCollection() {
        Assertions.assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    void getEpicsShouldReturnCollectionWithThreeEpics() {
        addTasksInManager(manager, new Epic(), 3);
        Assertions.assertEquals(3, manager.getEpics().size());
    }

    @Test
    void getSubtasksShouldReturnEmptySubtasksCollection() {
        Assertions.assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    void getSubtasksShouldReturnCollectionWithThreeSubtasks() {
        getManagerWithEpicAndSubtask();
        manager.addEpic(getDefaultEpic());
        Subtask subtask = new Subtask(DEFAULT_EPIC_ID, "", "", TaskStatus.NEW);
        addTasksInManager(manager, subtask, 2);
        Assertions.assertEquals(3, manager.getSubtasks().size());
    }

    @Test
    void getEpicSubtasks() {
        getManagerWithEpicAndSubtask();
        Assertions.assertEquals(1, manager.getSubtask(2).getEpicId());
    }

    @Test
    void getTaskShouldReturnNull() {
        Assertions.assertNull(manager.getTask(1));
    }

    @Test
    void getTaskShouldReturnTask() {
        manager.addTask(new Task());
        Assertions.assertEquals(1, manager.getTask(1).getId());
    }

    @Test
    void getEpicShouldReturnNull() {
        Assertions.assertNull(manager.getEpic(1));
    }

    @Test
    void getEpicShouldReturnEpic() {
        manager.addEpic(getDefaultEpic());
        Assertions.assertEquals(1, manager.getEpic(DEFAULT_EPIC_ID).getId());
    }

    @Test
    void getSubtaskShouldReturnNull() {
        Assertions.assertNull(manager.getSubtask(1));
    }

    @Test
    void getSubtaskShouldReturnSubtask() {
        getManagerWithEpicAndSubtask();
        Assertions.assertEquals(2, manager.getSubtask(2).getId());
    }

    @Test
    void addTaskShouldAddOneTask() {
        Assertions.assertEquals(ZERO_SIZE, manager.getTasks().size());
        manager.addTask(new Task());
        Assertions.assertEquals(1, manager.getTasks().size());
    }

    @Test
    void addTaskShouldAddFiveTasks() {
        Assertions.assertEquals(ZERO_SIZE, manager.getTasks().size());
        addTasksInManager(manager, new Task(), 5);
        Assertions.assertEquals(5, manager.getTasks().size());
    }

    @Test
    void addEpicShouldAddOneEpic() {
        Assertions.assertEquals(ZERO_SIZE, manager.getEpics().size());
        manager.addEpic(new Epic());
        Assertions.assertEquals(1, manager.getEpics().size());
    }

    @Test
    void addEpicShouldAddFiveEpics() {
        Assertions.assertEquals(ZERO_SIZE, manager.getEpics().size());
        addTasksInManager(manager, new Epic(), 5);
        Assertions.assertEquals(5, manager.getEpics().size());
    }

    @Test
    void addSubtaskShouldAddOneSubtask() {
        Assertions.assertEquals(ZERO_SIZE, manager.getSubtasks().size());
        getManagerWithEpicAndSubtask();
        Assertions.assertEquals(1, manager.getSubtasks().size());
    }

    @Test
    void addSubtaskShouldAddFiveSubtasks() {
        Assertions.assertEquals(ZERO_SIZE, manager.getSubtasks().size());
        manager.addEpic(new Epic());
        Subtask subtask = new Subtask(DEFAULT_EPIC_ID, "", "", TaskStatus.NEW);
        addTasksInManager(manager, subtask, 5);
        Assertions.assertEquals(5, manager.getSubtasks().size());
    }

    @Test
    void addSubtaskShouldThrowException() {
        Assertions.assertThrows(NullPointerException.class,
                () -> manager.addSubtask(new Subtask(0, "", "", TaskStatus.NEW)));
    }

    @Test
    void updateTaskShouldUpdateDescriptionAndNameAndStatus() {
        Task expectedTask = new Task();
        manager.addTask(expectedTask);
        Task currentTask = manager.getTask(1);

        Assertions.assertNull(currentTask.getDescription());
        Assertions.assertNull(currentTask.getName());
        Assertions.assertEquals(TaskStatus.NEW, currentTask.getStatus());

        expectedTask.setName("Task");
        expectedTask.setDescription("Description");
        expectedTask.setStatus(TaskStatus.DONE);
        manager.updateTask(expectedTask);

        Assertions.assertEquals(expectedTask, currentTask);
    }

    @Test
    void updateEpicShouldUpdateDescriptionAndNameAndStatusAndSubtasksQuantity() {
        Epic epic = new Epic();
        manager.addEpic(epic);

        Assertions.assertNull(manager.getEpic(DEFAULT_EPIC_ID).getDescription());
        Assertions.assertNull(manager.getEpic(DEFAULT_EPIC_ID).getName());
        Assertions.assertEquals(TaskStatus.NEW, manager.getEpic(DEFAULT_EPIC_ID).getStatus());
        Assertions.assertEquals(ZERO_SIZE, epic.getSubtaskIds().size());

        epic.setName("Name");
        epic.setDescription("Description");
        manager.updateEpic(epic);
        manager.addSubtask(new Subtask(DEFAULT_EPIC_ID, "", "", TaskStatus.DONE));

        Assertions.assertEquals(epic, manager.getEpic(DEFAULT_EPIC_ID));
        Assertions.assertEquals(1, epic.getSubtaskIds().size());
    }

    @Test
    void updateSubtaskShouldUpdateDescriptionAndNameAndStatusAndEpicId() {
        Subtask subtask = new Subtask(DEFAULT_EPIC_ID, "", "", TaskStatus.NEW);
        manager.addEpic(new Epic());
        manager.addEpic(new Epic());
        manager.addSubtask(subtask);

        int newEpicId = 2;
        subtask.setName("Name");
        subtask.setDescription("Description");
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        subtask.setEpicId(newEpicId);
        manager.updateSubtask(subtask);

        Assertions.assertEquals(subtask, manager.getSubtask(3));
    }

    @Test
    void testDeleteTask() {
        Assertions.assertEquals(ZERO_SIZE, manager.getTasks().size());
        addTasksInManager(manager, new Task(), 3);

        Assertions.assertEquals(3, manager.getTasks().size());

        manager.deleteTask(1);
        Assertions.assertEquals(2, manager.getTasks().size());

        manager.deleteTask(3);
        Assertions.assertEquals(1, manager.getTasks().size());

        manager.deleteTask(2);
        Assertions.assertEquals(ZERO_SIZE, manager.getTasks().size());
    }

    @Test
    void testDeleteEpic() {
        Assertions.assertEquals(ZERO_SIZE, manager.getEpics().size());
        manager.addEpic(getDefaultEpic());
        Assertions.assertEquals(1, manager.getEpics().size());
        manager.deleteEpic(DEFAULT_EPIC_ID);
        Assertions.assertEquals(ZERO_SIZE, manager.getEpics().size());
    }

    @Test
    void testDeleteSubtask() {
        Assertions.assertEquals(ZERO_SIZE, manager.getSubtasks().size());
        getManagerWithEpicAndSubtask();
        Assertions.assertEquals(1, manager.getSubtasks().size());
        manager.deleteSubtask(2);
        Assertions.assertEquals(ZERO_SIZE, manager.getSubtasks().size());
    }

    @Test
    void testDeleteTasks() {
        Assertions.assertEquals(ZERO_SIZE, manager.getTasks().size());
        addTasksInManager(manager, new Task(), 5);
        Assertions.assertEquals(5, manager.getTasks().size());
        manager.deleteTasks();
        Assertions.assertEquals(ZERO_SIZE, manager.getTasks().size());
    }

    @Test
    void testDeleteEpics() {
        Assertions.assertEquals(ZERO_SIZE, manager.getEpics().size());
        addTasksInManager(manager, getDefaultEpic(), 5);
        addThreeSubtaskWithStatus(manager, TaskStatus.NEW);
        Assertions.assertEquals(5, manager.getEpics().size());
        manager.deleteEpics();

        Assertions.assertEquals(ZERO_SIZE, manager.getSubtasks().size());
        Assertions.assertEquals(ZERO_SIZE, manager.getEpics().size());
    }

    @Test
    void testDeleteSubtasks() {
        Assertions.assertEquals(ZERO_SIZE, manager.getSubtasks().size());
        manager.addEpic(new Epic());
        Subtask subtask = new Subtask(DEFAULT_EPIC_ID, "", "", TaskStatus.NEW);
        addFiveTasksInManagerWithHistory(manager, subtask);
        Assertions.assertEquals(5, manager.getSubtasks().size());
        manager.deleteSubtasks();
        Assertions.assertEquals(ZERO_SIZE, manager.getSubtasks().size());
    }

    @Test
    void testGetHistory() {
        Assertions.assertEquals(ZERO_SIZE, manager.getHistory().size());
        addFiveTasksInManagerWithHistory(manager, new Task());
        Assertions.assertEquals(5, manager.getHistory().size());
    }

    @Test
    void shouldReturnUnmodifiableCollection() {
        manager.addEpic(getDefaultEpic());

        Collection<Integer> list = manager.getEpic(DEFAULT_EPIC_ID).getSubtaskIds();
        Assertions.assertThrows(UnsupportedOperationException.class, () -> list.add(2));
    }

    @Test
    void epicStatusShouldBeNewWhenSubtasksListIsEmpty() {
        manager.addEpic(getDefaultEpic());

        Assertions.assertTrue(manager.getEpicSubtasks(DEFAULT_EPIC_ID).isEmpty());
        Assertions.assertEquals(TaskStatus.NEW, manager.getEpic(1).getStatus());
    }

    @Test
    void epicShouldHaveNewStatusWhenAllSubtasksAreNew() {
        manager.addEpic(getDefaultEpic());
        addThreeSubtaskWithStatus(manager, TaskStatus.NEW);

        Assertions.assertEquals(TaskStatus.NEW, manager.getEpic(DEFAULT_EPIC_ID).getStatus());
    }

    @Test
    void epicShouldHaveDoneStatusWhenAllSubtasksAreDone() {
        manager.addEpic(getDefaultEpic());
        addThreeSubtaskWithStatus(manager, TaskStatus.DONE);

        Assertions.assertEquals(TaskStatus.DONE, manager.getEpic(DEFAULT_EPIC_ID).getStatus());
    }

    @Test
    void epicShouldHaveInProgressStatusWhenSubtasksHaveDifferentStatuses() {
        getManagerWithEpicAndSubtask();
        Subtask subtask = new Subtask(DEFAULT_EPIC_ID, "task5", "description task5", TaskStatus.DONE);
        manager.addSubtask(subtask);

        Assertions.assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(DEFAULT_EPIC_ID).getStatus());
    }

    @Test
    void epicShouldHaveInProgressStatusWhenSubtasksAreInProgress() {
        manager.addEpic(getDefaultEpic());
        addThreeSubtaskWithStatus(manager, TaskStatus.IN_PROGRESS);

        Assertions.assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(DEFAULT_EPIC_ID).getStatus());
    }

    @Test
    void getPrioritizedTasksShouldCorrectSortTwoTasks() {
        Task task = new Task();
        Task task1 = new Task();
        manager.addTask(task1);
        task.setStartTime(new Date());
        task.setDuration(60);
        manager.addTask(task);

        List<Task> expectedSequence = List.of(task, task1);

        Assertions.assertIterableEquals(expectedSequence, manager.getPrioritizedTasks());
    }

    @Test
    void getPrioritizedTasksShouldCorrectSortFiveTasks() {
        Task task1 = new Task();
        //String name, String description, TaskStatus status, int duration, Date startTime
        Task task2 = new Task("name", "description", TaskStatus.NEW, 0,new Date());
        task2.setStartTime(new Date());
        task2.setDuration(60);
        Epic epic = getDefaultEpic();
        Subtask subtask1 = new Subtask(3, "", "", TaskStatus.IN_PROGRESS);
        subtask1.setStartTime(Date.from(Instant.EPOCH));
        subtask1.setDuration(60);
        Subtask subtask2 = new Subtask(3, "", "", TaskStatus.NEW);
        subtask2.setStartTime(Date.from(Instant.ofEpochSecond(4000)));
        subtask2.setDuration(60);

        List<Task> expectedSequence = List.of(subtask1, subtask2, task2, task1);

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        Assertions.assertIterableEquals(expectedSequence, manager.getPrioritizedTasks());
    }

    @Test
    void testAddTShouldNotThrowException() {
        manager.addTask(new Task());
        manager.addEpic(getDefaultEpic());
        manager.addEpic(getDefaultEpic());
        Subtask subtask = new Subtask(2, "", "", TaskStatus.NEW);
        subtask.setStartTime(Date.from(Instant.EPOCH));
        subtask.setDuration(60);
        manager.addSubtask(subtask);
        Task task = new Task();
        task.setStartTime(Date.from(Instant.ofEpochSecond(4000)));
        task.setDuration(60);
        manager.addTask(task);
    }

    @Test
    void testAddTaskShouldThrowException() {
        Assertions.assertThrows(TaskIntersectionException.class,
                () -> {
                    Task task1 = new Task();
                    task1.setStartTime(Date.from(Instant.EPOCH));
                    Task task2 = new Task();
                    task2.setStartTime(Date.from(Instant.EPOCH));

                    manager.addTask(task1);
                    manager.addTask(task2);
                });
    }

    @Test
    void testAddSubtaskShouldThrowException() {
        Assertions.assertThrows(TaskIntersectionException.class,
                () -> {
                    manager.addEpic(new Epic());
                    Subtask subtask1 = new Subtask(1, "", "", TaskStatus.NEW);
                    subtask1.setStartTime(Date.from(Instant.EPOCH));
                    manager.addSubtask(subtask1);
                    Subtask subtask2 = new Subtask(1, "", "", TaskStatus.NEW);
                    subtask2.setStartTime(Date.from(Instant.EPOCH));
                    manager.addSubtask(subtask2);
                });
    }

    @Test
    void testAddTShouldThrowException() {
        Assertions.assertThrows(TaskIntersectionException.class,
                () -> {
                    Task task1 = new Task();
                    task1.setStartTime(Date.from(Instant.EPOCH));

                    manager.addTask(task1);
                    manager.addEpic(getDefaultEpic());

                    Subtask subtask = new Subtask(2, "", "", TaskStatus.NEW);
                    subtask.setStartTime(Date.from(Instant.EPOCH));
                    manager.addSubtask(subtask);
                });
    }

    @Test
    void testUpdateTaskShouldThrowException() {
        Assertions.assertThrows(TaskIntersectionException.class,
                () -> {
                    Task task = new Task();
                    task.setStartTime(Date.from(Instant.EPOCH));
                    manager.addTask(task);
                    Task task1 = new Task();
                    task1.setStartTime(new Date());
                    manager.addTask(task1);
                    task1.setStartTime(Date.from(Instant.EPOCH));

                    manager.updateTask(task1);
                });

    }

    @Test
    void testUpdateSubtaskShouldThrowException() {
        Assertions.assertThrows(TaskIntersectionException.class,
                () -> {
                    manager.addEpic(getDefaultEpic());
                    Subtask subtask1 = new Subtask(1, "", "", TaskStatus.NEW);
                    subtask1.setStartTime(Date.from(Instant.EPOCH));
                    Subtask subtask2 = new Subtask(1, "", "", TaskStatus.NEW);
                    manager.addSubtask(subtask1);
                    manager.addSubtask(subtask2);
                    subtask2.setStartTime(Date.from(Instant.EPOCH));
                    manager.updateSubtask(subtask2);
                });
    }

    @Test
    void testUpdateTShouldThrowException() {
        Assertions.assertThrows(TaskIntersectionException.class,
                () -> {
                    Task task = new Task();
                    manager.addTask(task);
                    task.setStartTime(Date.from(Instant.EPOCH));
                    manager.addEpic(getDefaultEpic());
                    Subtask subtask = new Subtask(2, "", "", TaskStatus.NEW);
                    manager.addSubtask(subtask);
                    manager.updateTask(task);
                    subtask.setStartTime(Date.from(Instant.EPOCH));
                    manager.updateSubtask(subtask);
                });
    }

    @Test
    public void testForBorisShouldNotThrowException() {
        Task task1 = new Task();
        Task task2 = new Task();
        Epic epic = new Epic();//id=3
        Subtask subtask1 = new Subtask(3, "", "", TaskStatus.NEW);
        Subtask subtask2 = new Subtask(3, "", "", TaskStatus.NEW);
        Task target = new Task();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy:HH-mm");
        //время для задачи
        String task1StartDate = "01/01/2000:04-02";
        int task1Duration = 10;

        String subtask1StartDate = "03/01/2000:04-00";
        int subtask1Duration = 5;
        //время для второй подзадачи
        String subtask2StartDate = "02/01/2000:04-00";
        int subtask2Duration = 20;
        //время для целевой задачи
        String targetStartDate = "1/01/2000:04-12";
        int targetDuration = 1;
        try {
            task1.setStartTime(formatter.parse(task1StartDate));
            task1.setDuration(task1Duration);

            subtask1.setStartTime(formatter.parse(subtask1StartDate));
            subtask1.setDuration(subtask1Duration);

            subtask2.setStartTime(formatter.parse(subtask2StartDate));
            subtask2.setDuration(subtask2Duration);

            target.setStartTime(formatter.parse(targetStartDate));
            target.setDuration(targetDuration);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addEpic(epic);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addTask(target);
    }

    private void getManagerWithEpicAndSubtask() {
        manager.addEpic(getDefaultEpic());
        manager.addSubtask(new Subtask(DEFAULT_EPIC_ID, "", "", TaskStatus.NEW));
    }

    private Epic getDefaultEpic() {
        return new Epic("Epic1", TaskStatus.NEW, "Description Epic1");
    }

    private void addTasksInManager(TaskManager manager, Task task, int quantityOfIterations) {
        switch (task.getType()) {
            case TASK:
                for (int i = 1; i <= quantityOfIterations; i++) {
                    manager.addTask(task);
                }
                break;
            case EPIC:
                for (int i = 1; i <= quantityOfIterations; i++) {
                    manager.addEpic((Epic) task);
                }
                break;
            case SUBTASK:
                for (int i = 1; i <= quantityOfIterations; i++) {
                    manager.addSubtask((Subtask) task);
                }
                break;
        }
    }

    private void addFiveTasksInManagerWithHistory(TaskManager manager, Task task) {
        switch (task.getType()) {
            case TASK:
                for (int i = 1; i <= 5; i++) {
                    manager.addTask(task);
                    manager.getTask(i);
                }
                break;
            case EPIC:
                for (int i = 1; i <= 5; i++) {
                    manager.addEpic((Epic) task);
                    manager.getEpic(i);
                }
                break;
            case SUBTASK:
                for (int i = 1; i <= 5; i++) {
                    manager.addSubtask((Subtask) task);
                    manager.getSubtask(i);
                }
                break;
        }
    }

    private void addThreeSubtaskWithStatus(TaskManager manager, TaskStatus status) {
        for (int i = 1; i < 3; i++) {
            String subtaskName = "subtask" + i;
            Subtask subtask = new Subtask(DEFAULT_EPIC_ID, subtaskName, "description " + subtaskName, status);
            manager.addSubtask(subtask);
        }
    }
}