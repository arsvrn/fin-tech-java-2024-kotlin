import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.*
import java.io.IOException
import java.nio.charset.StandardCharsets

class NewsServiceTest {

    @Test
    fun `test getNews returns real data`() = runBlocking {
        val newsService = NewsService()

        val newsList = newsService.getNews(5)

        assertTrue(newsList.isNotEmpty(), "Список новостей не должен быть пустым")
    }

    @Test
    fun `test saveNews saves correct data to CSV file`() {
        val path = "test_news.csv"

        val newsList = listOf(
            News(1, "Новость 1", "Москва", "Описание 1", "https://example.com/1", 100, 10, 1726670699),
            News(2, "Новость 2", null, "Описание 2", "https://example.com/2", 50, 5, 1726670000)
        )

        val newsService = NewsService()
        newsService.saveNews(path, newsList)

        val file = File(path)
        assertTrue(file.exists())

        val content = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8)

        assertEquals("id,title,place,description,site_url,favorites_count,comments_count,publication_date", content[0])

        assertEquals("1,\"Новость 1\",\"Москва\",\"Описание 1\",https://example.com/1,100,10,2024-09-18", content[1])

        assertEquals("2,\"Новость 2\",\"\",\"Описание 2\",https://example.com/2,50,5,2024-09-18", content[2])

        file.delete()
    }

    @Test
    fun `test saveNews throws exception if file exists`() {
        val path = "test_news_exists.csv"

        File(path).createNewFile()

        val newsList = listOf(
            News(1, "Новость 1", "Москва", "Описание 1", "https://example.com/1", 100, 10, 1726670699)
        )
        val newsService = NewsService()
        assertThrows(IllegalArgumentException::class.java) {
            newsService.saveNews(path, newsList)
        }
        File(path).delete()
    }
}