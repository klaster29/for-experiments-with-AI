package manager;

public class Managers {

    public static TaskManager getDefault(String url) {
        HttpTaskManager manager = new HttpTaskManager(url);
        return manager.load(url);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
