package manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ManagersTest {

    @Test
    void testGetDefault() {
        TaskManager manager = Managers.getDefault("http://localhost:8080/tasks/task");
        Assertions.assertTrue(manager instanceof HttpTaskManager);
    }

    @Test
    void getDefaultHistory() {
        HistoryManager manager = Managers.getDefaultHistory();
        Assertions.assertTrue(manager instanceof InMemoryHistoryManager);
    }
}