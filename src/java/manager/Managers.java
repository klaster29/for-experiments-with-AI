package manager;

import java.net.URI;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getHttpTaskManager(URI taskDBFile) {
        return new HttpTaskManager(taskDBFile);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
