package FilesHandling;


import TestTask.DataClasses.Student;
import TestTask.FileHandling.JsonParser;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class JSONParserTest {

    private static final String TEST_FILE_PATH = "test_students.json";

    @BeforeMethod
    public void setUp() throws IOException {
        String jsonContent = "{\"students\": [" +
                "{\"id\": 1, \"name\": \"John Doe\"}," +
                "{\"id\": 2, \"name\": \"Jane Smith\"}," +
                "{\"id\": 3, \"name\": \"Mike Brown\"}" +
                "]}";
        Files.write(Paths.get(TEST_FILE_PATH), jsonContent.getBytes());
    }

    @AfterMethod
    public void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE_PATH));
    }

    @DataProvider
    public Object[][] studentsDataProvider() {
        return new Object[][]{
                {Arrays.asList(
                        new Student(1, "John Doe"),
                        new Student(2, "Jane Smith"),
                        new Student(3, "Mike Brown")
                )}
        };
    }

    @Test(dataProvider = "studentsDataProvider")
    public void testReadJsonFile(List<Student> expectedStudents) throws IOException {
        List<Student> actualStudents = JsonParser.readJsonFile(TEST_FILE_PATH);
        assertEquals(actualStudents, expectedStudents);
    }

    @Test(dataProvider = "studentsDataProvider")
    public void testWriteStudentToFile(List<Student> students) throws IOException {
        String outputFilePath = "output_students.json";
        JsonParser.writeStudentToFile(students, outputFilePath);

        List<Student> actualStudents = JsonParser.readJsonFile(outputFilePath);
        assertEquals(actualStudents, students);

        Files.deleteIfExists(Paths.get(outputFilePath));
    }
}

