package FunctionalityTest;

import TestTask.Commands.AbstractCommand;
import TestTask.Commands.AddStudentCommand;
import TestTask.DataClasses.Student;
import TestTask.FileHandling.JsonParser;
import TestTask.Managers.CollectionManager;
import TestTask.ServerHandling.AuthorizationFailed;
import TestTask.ServerHandling.FTPClientHandler;
import org.testng.annotations.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class AddingTest {

    private AbstractCommand addCommand;
    private FTPClientHandler ftp;
    private CollectionManager collectionManager;
    private List<Student> studentsFromTheInit;

    private static final String LOCAL_PATH = "src/test/resources/dataFromServer/output.json";
    private static final String FTP_HOST = "localhost";
    private static final int FTP_PORT = 21;
    private static final String FTP_USERNAME = "Boring3";
    private static final String FTP_PASSWORD = "pwd";
    private static final String REMOTE_FILE_PATH = "input.json";

    @BeforeMethod
    public void setUp() throws IOException, AuthorizationFailed {
        addCommand = new AddStudentCommand();

        // Инициализация FTP соединения и загрузка файла
        ftp = new FTPClientHandler(FTP_HOST, FTP_PORT);
        ftp.authorization(FTP_USERNAME, FTP_PASSWORD);
        ftp.getFileFromServer(REMOTE_FILE_PATH, LOCAL_PATH);

        // Инициализация CollectionManager и загрузка данных студентов
        collectionManager = CollectionManager.getInstance();
        collectionManager.setStudentList(JsonParser.readJsonFile(LOCAL_PATH));
        studentsFromTheInit = new ArrayList<>(collectionManager.getStudentList());
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
    public Object[][] validStudents() {
        return new Object[][]{
                {"Herman"},
                {"Johan"},
                {"Michael"},
                {"Sarah"},
                {"Anna"}
        };
    }

    @Test(dataProvider = "validStudents")
    public void addStudent(String studentName) {
        int sizeBeforeUpdate = studentsFromTheInit.size();
        System.out.println(addCommand.execute(new String[]{"ADD_STUDENT", studentName}));
        assertEquals(collectionManager.getStudentList().size(), sizeBeforeUpdate + 1);
    }

    @DataProvider
    public Object[][] invalidStudents() {
        return new Object[][]{
                {null}, {""}, {"                                             "}, {"\t\t\t\t\t\t\t\t\t\t\t\t"}, {""}
        };
    }

    @Test(dataProvider = "invalidStudents", expectedExceptions = IllegalArgumentException.class)
    public void invalidStudentTest(String studentName) {
        System.out.println(addCommand.execute(new String[]{"ADD_STUDENT", studentName}));
    }


    @Test(dataProvider = "validStudents")
    public void uniqueIDTest(String studentName) {
        HashSet<Integer> set = new HashSet<>();
        addCommand.execute(new String[]{"ADD_STUDENT", studentName});
        for(Student student : collectionManager.getStudentList()){
            assertTrue(set.add(student.getId()));
        }
    }

}
