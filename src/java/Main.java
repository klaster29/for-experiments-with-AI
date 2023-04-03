import manager.FileBackedTaskManager;
import manager.TaskManager;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        String datePattern = "dd/MM/yyyy";
        Path path = Paths.get("src/testResources/result.csv");
        TaskManager manager = FileBackedTaskManager.loadFromFile(path.toFile());

        /* косяк №1   InMemoryTaskManager
        * все еще не дает добавить задачу, у которой время начала = времени конца ранее добавленной задачи.
        Попробуй в testForBorisShouldNotThrowException сделать, чтобы задача target начиналась сразу после задачи task1:
        String targetStartDate = "1/01/2000:04-12";
        Ошибки быть не должно, т. к. target начинается сразу после окончания task1 (01/01/2000:04-02 + 10 мин),
        * но возникает manager.ManagerSaveException: Время задачи пересекается с уже добавленными.
        Кстати, лучше для этого создать отдельное исключение с говорящим названием, например: TaskIntersectionException*/






        /* косяк №2 FileBackedTaskManager method stringToDate
        Date представляет собой время в UTC и не хранит временную зону (Time Zone),
        поэтому при конвертации Date из строки нужно всегда указывать Time Zone,
        т. к. иначе будет использована зона по умолчанию, а в разных окружениях она может быть разная.
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        * */

        /*косяк №3 там же
        лучше не глушить ошибку, а пробросить RuntimeException, чтобы не терять стектрейс:
        throw new RuntimeException(e);
        * */
    }
}