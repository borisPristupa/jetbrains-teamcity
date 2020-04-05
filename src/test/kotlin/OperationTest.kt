import com.boris.internship.migration.Migrator
import com.boris.internship.migration.NewStorage
import com.boris.internship.migration.OldStorage
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class OperationTest {
    lateinit var httpClient: CloseableHttpClient

    @Test
    fun `test getting file names from a storage`() {
        val olds = OldStorage.listOfFiles(httpClient)
        assertEquals(olds.size, Migrator.fileAmount(OldStorage))
        assert(olds.all { it.isNotEmpty() })
    }

    @Test
    fun `test deleting file`() {
        val olds = Migrator.fileAmount(OldStorage)

        val fileName = OldStorage.listOfFiles(httpClient).take(1)[0]
        OldStorage.deleteFile(fileName, httpClient)

        assertEquals(olds - 1, Migrator.fileAmount(OldStorage))
    }

    @Test
    fun `test posting and reading a file`() {
        val testFileName = "/test.txt"
        val testFile = File(OperationTest::class.java.getResource(testFileName).toURI())

        assert(testFile.exists() && testFile.isFile && testFile.canRead())

        var news: Int = -2

        NewStorage.listOfFiles(httpClient)
            .also { news = it.size }
            .takeIf { it.contains(testFileName) }
            ?.also {
                NewStorage.deleteFile(testFileName, httpClient)
            }

        NewStorage.postFile(testFile, httpClient)
        val downloadedFile = NewStorage.getFile(testFileName, httpClient)

        assertEquals(testFile.name, downloadedFile.name)
        assertEquals(testFile.readText(), downloadedFile.readText())
        assertEquals(news + 1, NewStorage.listOfFiles(httpClient).size)
    }

    @Before
    fun init() {
        httpClient = HttpClients.createDefault()
    }

    @After
    fun close() {
        httpClient.close()
    }
}