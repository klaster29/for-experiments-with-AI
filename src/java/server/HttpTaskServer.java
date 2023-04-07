package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import manager.TaskManager;
import task.Epic;
import task.Subtask;
import task.Task;

public class HttpTaskServer {

    private final TaskManager taskManager;
    private final Gson gson;
    private final HttpServer server;
    private final String host;
    private final int port;

    public HttpTaskServer(TaskManager taskManager, String host, int port) throws IOException {
        this.taskManager = taskManager;
        this.gson = new Gson();
        this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
        this.host = host;
        this.port = port;
        initHandlers();
    }

    private void initHandlers() {
        server.createContext("/tasks/task", new TaskHandler());
        server.createContext("/tasks/subtask", new SubtaskHandler());
        server.createContext("/tasks/epic", new EpicHandler());
        server.createContext("/tasks/history", new HistoryHandler());
        server.createContext("/tasks", new TaskListHandler());
    }

    public void start() {
        server.start();
        System.out.printf("Server started at %s:%d\n", host, port);
    }

    public void stop() {
        server.stop(0);
        System.out.print("Server stopped\n");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private <T> Optional<T> parseRequestBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        try {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            return Optional.of(gson.fromJson(requestBody, clazz));
        } catch (JsonSyntaxException e) {
            sendResponse(exchange, 400, "{\"error\": \"Bad request\"}");
            return Optional.empty();
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        if (query == null) {
            return null;
        }
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(param -> param[0], param -> param[1]));
    }

    private class TaskHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                case "PUT":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                    break;
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            if (Objects.isNull(params)) {
                sendResponse(exchange, 200, gson.toJson(taskManager.getTasks()));
            } else {
                int id = Integer.parseInt(params.get("id"));
                Optional<Task> task = Optional.ofNullable(taskManager.getTask(id));
                if (task.isPresent()) {
                    sendResponse(exchange, 200, gson.toJson(task.get()));
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Not found\"}");
                }
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            Optional<Task> optionalTask = parseRequestBody(exchange, Task.class);
            if (optionalTask.isPresent()) {
                Task task = optionalTask.get();
                int taskId = task.getId();
                    if (taskManager.getTask(taskId) != null) {
                        taskManager.updateTask(task);
                        sendResponse(exchange, 200, "{\"message\": \"Task updated\"}");
                    } else {
                        taskManager.addTask(task);
                        sendResponse(exchange, 200, "{\"message\": \"Task created\"}");
                    }
                } else {
                    sendResponse(exchange, 400, "{\"error\": \"Bad request\"}");
            }
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/tasks/task")) {
                taskManager.deleteTasks();
                sendResponse(exchange, 200, "{\"message\": \"All tasks deleted\"}");
            } else {
                int id = Integer.parseInt(parseQueryParams(exchange.getRequestURI().getQuery()).get("id"));
                if (taskManager.getTask(id) != null) {
                    taskManager.deleteTask(id);
                }
                sendResponse(exchange, 200, "{\"message\": \"Task deleted\"}");
            }
        }

    }

    private class TaskListHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if (method.equals("GET")) {
                Collection<Task> tasks = taskManager.getPrioritizedTasks();
                sendResponse(exchange, 200, gson.toJson(tasks));
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    private class SubtaskHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                case "PUT":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                    break;
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            if (Objects.isNull(params)) {
                sendResponse(exchange, 200, gson.toJson(taskManager.getSubtasks()));
            } else {
                int id = Integer.parseInt(params.get("id"));
                Optional<Task> task = Optional.ofNullable(taskManager.getSubtask(id));
                if (task.isPresent()) {
                    sendResponse(exchange, 200, gson.toJson(task.get()));
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Not found\"}");
                }
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            Optional<Subtask> optionalSubtask = parseRequestBody(exchange, Subtask.class);
            if (optionalSubtask.isPresent()) {
                Subtask subtask = optionalSubtask.get();
                if (taskManager.getSubtask(subtask.getId()) != null) {
                    taskManager.updateSubtask(subtask);
                    sendResponse(exchange, 200, "{\"message\": \"Subtask updated\"}");
                } else {
                    taskManager.addSubtask(subtask);
                    sendResponse(exchange, 200, "{\"message\": \"Subtask created\"}");
                }
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Bad request\"}");
            }
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            int subtaskId = Integer.parseInt(queryParams.get("id"));
            if (taskManager.getSubtask(subtaskId) != null) {
                taskManager.deleteSubtask(subtaskId);
            }
            sendResponse(exchange, 200, "{\"message\": \"Subtask deleted\"}");
        }
    }


    private class EpicHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                case "PUT":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                    break;
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            if (Objects.isNull(params)) {
                sendResponse(exchange, 200, gson.toJson(taskManager.getEpics()));
            } else {
                int id = Integer.parseInt(params.get("id"));
                Optional<Epic> epic = Optional.ofNullable(taskManager.getEpic(id));
                if (epic.isPresent()) {
                    sendResponse(exchange, 200, gson.toJson(epic.get()));
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Not found\"}");
                }
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            Optional<Epic> optionalEpic = parseRequestBody(exchange, Epic.class);
            if (optionalEpic.isPresent()) {
                Epic epic = optionalEpic.get();
                if (taskManager.getEpic(epic.getId()) != null) {
                    taskManager.updateEpic(epic);
                    sendResponse(exchange, 200, "{\"message\": \"Epic updated\"}");
                } else {
                    taskManager.addEpic(epic);
                    sendResponse(exchange, 200, "{\"message\": \"Epic created\"}");
                }
            } else {
                sendResponse(exchange, 400, "{\"error\": \"Bad request\"}");
            }
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            if (!queryParams.containsKey("id")) {
                sendResponse(exchange, 400, "{\"error\": \"Bad request\"}");
                return;
            }
            int epicId = Integer.parseInt(queryParams.get("id"));
            if (taskManager.getEpic(epicId) != null) {
                taskManager.deleteEpic(epicId);
            }
            sendResponse(exchange, 200, "{\"message\": \"Epic deleted\"}");
        }
    }

    private class HistoryHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if (method.equals("GET")) {
                sendResponse(exchange, 200, gson.toJson(taskManager.getHistory()));
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }
}

