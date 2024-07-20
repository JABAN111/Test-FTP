package FtpInteraction;

import TestTask.ServerHandling.AuthorizationFailed;
import TestTask.ServerHandling.FTPClientHandler;
import TestTask.ServerHandling.ResponseStatus;
import org.testng.annotations.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class FtpConnectingTest {
    private FTPClientHandler successfulFtpConnection;

    @BeforeMethod
    public void setUp() throws IOException, AuthorizationFailed {
        successfulFtpConnection = new FTPClientHandler("localhost", 21);
        successfulFtpConnection.authorization("Boring3", "pwd");
    }

    @AfterMethod
    public void tearDown() {
        successfulFtpConnection.disconnect();
    }

    @DataProvider
    public Object[][] validDataForFtpConnecting() {
        return new Object[][]{
                {"192.168.31.13", 21},
                {"localhost", 21}
        };
    }

    @DataProvider
    public Object[][] invalidDataForFtpConnecting() {
        return new Object[][]{
                {"312.32131.321.2.dam", 21},
                {"321231da", 21},
                {"localhost", 22}
        };
    }

    @Test(description = "Connecting to ftp server with correct ip and port",
            dataProvider = "validDataForFtpConnecting",
            groups = {"connecting"})
    public void testFtpConnectingWithValidData(String ip, int port) throws IOException {
        successfulFtpConnection = new FTPClientHandler(ip, port);
    }

    @Test(description = "Attempting to connect ftp server with invalid ip or/and port",
            dataProvider = "invalidDataForFtpConnecting",
            expectedExceptions = IOException.class,
            groups = {"connecting"})
    public void testFtpConnectingWithInvalidData(String ip, int port) throws IOException {
        new FTPClientHandler(ip, port);
    }

    @DataProvider
    public Object[][] validAuthDataForFtpConnecting() {
        return new Object[][]{
                {"Boring3", "pwd"}
        };
    }

    @Test(dataProvider = "validAuthDataForFtpConnecting", dependsOnMethods = "testFtpConnectingWithValidData")
    public void testFtpConnectingWithValidAuthData(String login, String pwd) throws AuthorizationFailed {
        assertEquals(successfulFtpConnection.authorization(login, pwd), ResponseStatus.SUCCESS);
    }

    @DataProvider
    public Object[][] invalidAuthDataForFtpConnecting() {
        return new Object[][]{
                {"Darma", "no"},
                {null, null},
                {"Boring", null},
                {null, "Boring"},
                {"", ""}
        };
    }

    @Test(dataProvider = "invalidAuthDataForFtpConnecting", dependsOnMethods = "testFtpConnectingWithValidData", expectedExceptions = AuthorizationFailed.class)
    public void testFtpConnectingWithInvalidAuthData(String login, String pwd) throws AuthorizationFailed {
        successfulFtpConnection.authorization(login, pwd);
    }

    @DataProvider
    public Object[][] validWayToLocalFiles() {
        return new Object[][]{
                {"input.json"},
                {"pom.xml"}
        };
    }

    @Test(dependsOnMethods = "testFtpConnectingWithValidAuthData", dataProvider = "validWayToLocalFiles")
    public void testSendingFileToTheServer(String pathToLocalFile) throws IOException {
        assertEquals(successfulFtpConnection.sendFile(pathToLocalFile), ResponseStatus.SUCCESS);
    }

    @DataProvider
    public Object[][] invalidWayToLocalFiles() {
        return new Object[][]{
                {null},
                {"coolFileThatDidn'tExist"},
                {"Немного кирилицы.txt"}
        };
    }

    @Test(dependsOnMethods = "testFtpConnectingWithValidAuthData", dataProvider = "invalidWayToLocalFiles", expectedExceptions = FileNotFoundException.class)
    public void testSendingFileToTheServerWithInvalidPath(String pathToLocalFile) throws IOException {
        successfulFtpConnection.sendFile(pathToLocalFile);
    }

    @DataProvider
    public Object[][] validWayToRemoteFilesWithLocal() {
        return new Object[][]{
                {"input.json", "input.json"},
                {"pom.xml", "newPom.xml"}
        };
    }

    @Test(dataProvider = "validWayToRemoteFilesWithLocal", dependsOnMethods = "testFtpConnectingWithValidAuthData")
    public void testSendingFileToTheServerWithValidPath(String pathToRemoteFile, String local) throws IOException {
        assertEquals(successfulFtpConnection.getFileFromServer(pathToRemoteFile, local), ResponseStatus.SUCCESS);
    }

    @DataProvider
    public Object[][] invalidWays() {
        return new Object[][]{
                {"coolFileThatDidn'tExist.ftp", "coolFileThatDidn'tExistLOCALLY.ftp"}
        };
    }

    @Test(dataProvider = "invalidWays", dependsOnMethods = "testFtpConnectingWithValidAuthData")
    public void testSendingFileToTheServerWithInvalidPaths(String pathToRemoteFile, String local) throws IOException {
        assertEquals(successfulFtpConnection.getFileFromServer(pathToRemoteFile, local), ResponseStatus.FAILURE);
    }

    @DataProvider
    public Object[][] emptyOrNullWays() {
        return new Object[][]{
                {null, null},
                {null, ""},
                {"", null}
        };
    }

    @Test(dataProvider = "emptyOrNullWays", expectedExceptions = FileNotFoundException.class, dependsOnMethods = "testFtpConnectingWithValidAuthData")
    public void testSendingFileToTheServerWithEmptyOrNull(String pathToRemoteFile, String local) throws IOException {
        successfulFtpConnection.getFileFromServer(pathToRemoteFile, local);
    }
}
