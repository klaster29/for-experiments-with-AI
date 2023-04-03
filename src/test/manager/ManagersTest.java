package manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ManagersTest {

    @Test
    void testGetDefault() {
        TaskManager manager = Managers.getDefault();
        Assertions.assertTrue(manager instanceof InMemoryTaskManager);
    }

    @Test
    void getDefaultHistory() {
        HistoryManager manager = Managers.getDefaultHistory();
        Assertions.assertTrue(manager instanceof InMemoryHistoryManager);
    }
}