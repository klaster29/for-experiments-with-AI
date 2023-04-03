package manager;

public class ManagerSaveException extends NullPointerException {

    public ManagerSaveException(String message, Exception e) {
        super(message);
        e.printStackTrace();
    }

    public ManagerSaveException(String message) {
        super(message);
    }
}
