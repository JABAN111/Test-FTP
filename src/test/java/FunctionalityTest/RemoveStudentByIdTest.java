package FunctionalityTest;

import TestTask.Commands.AbstractCommand;
import TestTask.Commands.Exception.InvalidArgs;
import TestTask.Commands.RemoveStudentById;
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

import static org.testng.Assert.assertEquals;

public class RemoveStudentByIdTest {

    private AbstractCommand removeCommand;
    private FTPClientHandler ftp;
    private CollectionManager collectionManager;

    private static final String LOCAL_PATH = "src/test/resources/dataFromServer/output.json";
    private static final String FTP_HOST = "localhost";
    private static final int FTP_PORT = 21;
    private static final String FTP_USERNAME = "Boring3";
    private static final String FTP_PASSWORD = "PWD";
    private static final String REMOTE_FILE_PATH = "input.json";

    @BeforeMethod
    public void setUp() throws IOException, AuthorizationFailed {
        removeCommand = new RemoveStudentById();

        ftp = new FTPClientHandler(FTP_HOST, FTP_PORT);
        ftp.authorization(FTP_USERNAME, FTP_PASSWORD);
        ftp.getFileFromServer(REMOTE_FILE_PATH, LOCAL_PATH);

        collectionManager = CollectionManager.getInstance();
        collectionManager.getStudentList().clear();
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
    public Object[][] validStudentsNames() {
        return new Object[][]{
                {"Herman"},
                {"Johan"},
                {"Michael"},
                {"Sarah"},
                {"Anna"}
        };
    }

    @Test(dataProvider = "validStudentsNames")
    public void removeStudentTest(String studentName) throws InvalidArgs {
        collectionManager.getStudentList().add(new Student(studentName));
        int sizeBeforeUpdate = collectionManager.getStudentList().size();

        int lastId = collectionManager.getStudentList().size();
        System.out.println(removeCommand.execute(new String[]{"REMOVE_BY_ID", lastId + ""}));

        assertEquals(collectionManager.getStudentList().size(), sizeBeforeUpdate - 1);

    }

    @DataProvider
    public Object[][] invalidStudents() {
        return new Object[][]
                {{0},{-1},{-2121}
        };
    }

    @Test(dataProvider = "invalidStudents",expectedExceptions = InvalidArgs.class)
    public void invalidStudentTest(Integer studentId) throws InvalidArgs {
        System.out.println(removeCommand.execute(new String[]{"REMOVE_BY_ID", studentId+""}));
    }



}
