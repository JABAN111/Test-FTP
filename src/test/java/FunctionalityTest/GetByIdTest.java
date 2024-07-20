package FunctionalityTest;

import TestTask.Commands.AbstractCommand;
import TestTask.Commands.AddStudentCommand;
import TestTask.Commands.GetStudentByIdCommand;
import TestTask.DataClasses.Student;
import TestTask.FileHandling.JsonParser;
import TestTask.Managers.CollectionManager;
import TestTask.ServerHandling.AuthorizationFailed;
import TestTask.ServerHandling.FTPClientHandler;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class GetByIdTest {

    private AbstractCommand getById;
    private FTPClientHandler ftp;
    private CollectionManager collectionManager;

    private static final String LOCAL_PATH = "src/test/resources/dataFromServer/output.json";
    private static final String FTP_HOST = "localhost";
    private static final int FTP_PORT = 21;
    private static final String FTP_USERNAME = "Boring3";
    private static final String FTP_PASSWORD = "pwd";
    private static final String REMOTE_FILE_PATH = "input.json";

    @BeforeMethod
    public void setUp() throws IOException, AuthorizationFailed {
        getById = new GetStudentByIdCommand();

        // Инициализация FTP соединения и загрузка файла
        ftp = new FTPClientHandler(FTP_HOST, FTP_PORT);
        ftp.authorization(FTP_USERNAME, FTP_PASSWORD);
        ftp.getFileFromServer(REMOTE_FILE_PATH, LOCAL_PATH);

        // Инициализация CollectionManager и загрузка данных студентов
        collectionManager = CollectionManager.getInstance();
        collectionManager.setStudentList(JsonParser.readJsonFile(LOCAL_PATH));
    }

    @AfterMethod
    public void tearDown() throws IOException {
        // Завершение FTP соединения
        if (ftp != null) {
            JsonParser.writeStudentToFile(collectionManager.getStudentList(), REMOTE_FILE_PATH);
            ftp.sendFile(REMOTE_FILE_PATH);
            ftp.disconnect();
        }
    }

    @Test
    public void getByIdTest()  {
        for (int i = 0; i < collectionManager.getStudentList().size(); i++) {
            assertTrue(getById.execute(new String[]{"GET_BY_ID",i+""}).size()<=1);
        }
    }
}
