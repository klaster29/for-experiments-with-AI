import manager.Managers;
import manager.TaskManager;
import server.HttpTaskServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager, "localhost", 8080);
        server.start();
    }

}