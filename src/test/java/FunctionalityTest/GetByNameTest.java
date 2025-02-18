package FunctionalityTest;

import TestTask.Commands.AbstractCommand;
import TestTask.Commands.Exception.InvalidArgs;
import TestTask.Commands.GetListByNameCommand;
import TestTask.ConfigReader;
import TestTask.DataClasses.Student;
import TestTask.FileHandling.JsonParser;
import TestTask.Managers.CollectionManager;
import TestTask.ServerHandling.Exceptions.AuthorizationFailed;
import TestTask.ServerHandling.FTPClientHandler;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class GetByNameTest {
    private AbstractCommand getByName;
    private FTPClientHandler ftp;
    private CollectionManager collectionManager;

    private static final String LOCAL_PATH = ConfigReader.getProperty("local.path");
    private static final String FTP_HOST = ConfigReader.getProperty("ftp.host");
    private static final int FTP_PORT = Integer.parseInt(ConfigReader.getProperty("ftp.port"));
    private static final String FTP_USERNAME = ConfigReader.getProperty("ftp.username");
    private static final String FTP_PASSWORD = ConfigReader.getProperty("ftp.password");
    private static final String REMOTE_FILE_PATH = ConfigReader.getProperty("remote.file.path");

    @BeforeMethod
    public void setUp() throws IOException, AuthorizationFailed {
        getByName = new GetListByNameCommand();

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

    @DataProvider
    public Object[][] validNames() {
        return new Object[][]{
                {"Herman"},
                {"Johan"},
                {"Michael"},
                {"Sarah"},
                {"Anna"}
        };
    }

    @Test(dataProvider = "validNames")
    public void getByIdTest(String nameStudent) throws InvalidArgs {
        collectionManager.getStudentList().add(new Student(nameStudent));

        for (int i = 0; i < collectionManager.getStudentList().size(); i++) {
            assertFalse(getByName.execute(new String[]{"GET_BY_NAME", nameStudent}).isEmpty());
        }
    }

    @DataProvider
    public Object[][] invalidNames() {
        return new Object[][]{{null},{" "}};
    }
    @Test(dataProvider = "invalidNames")
    public void getByNameTest(String nameStudent) throws InvalidArgs {
        assertTrue(getByName.execute(new String[]{"GET_BY_NAME", nameStudent}).isEmpty());
        System.out.println(collectionManager.getStudentList());
    }
}
