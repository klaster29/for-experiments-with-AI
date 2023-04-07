import client.KVTaskClient;
import manager.Managers;
import manager.TaskManager;
import server.HttpTaskServer;
import server.KVServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        /*// создаем объект сервера
        KVServer server = new KVServer();
        // запускаем сервер на порту 8078
        server.start();
        */
        KVServer server = new KVServer();
        server.start();

        // Создаем клиента и регистрируемся
        KVTaskClient client = new KVTaskClient("http://localhost:8078");
        String apiToken = client.getApiToken(); // получаем API токен
        System.out.println("API Token: " + apiToken);

// сохраняем данные на сервере
        client.put("task1", "{ \"title\": \"Task 1\", \"description\": \"Description of task 1\" }");
        client.put("task2", "{ \"title\": \"Task 2\", \"description\": \"Description of task 2\" }");

// получаем данные с сервера
        String task1 = client.load("task1");
        String task2 = client.load("task2");

        System.out.println("Task 1: " + task1);
        System.out.println("Task 2: " + task2);
    }

}