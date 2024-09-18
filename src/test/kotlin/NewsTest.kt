import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class NewsTest {
    @Test
    fun `test getMostRatedNews with valid data`() {
        val newsList = listOf(
            News(1, "Новость 1", null, "Описание 1", "https://example.com/1", 100, 10, 1726670699),
            News(2, "Новость 2", null, "Описание 2", "https://example.com/2", 50, 5, 1726670000),
            News(3, "Новость 3", null, "Описание 3", "https://example.com/3", 200, 20, 1726669000),
            News(4, "Новость 4", null, "Описание 4", "https://example.com/4", 10, 1, 1726672000),
        )

        val period = LocalDate.of(2024, 9, 1)..LocalDate.of(2024, 9, 30)

        val topRatedNews = newsList.getMostRatedNews(3, period)

        assertEquals(3, topRatedNews.size)

        assertEquals("Новость 3", topRatedNews[0].title)
        assertEquals("Новость 1", topRatedNews[1].title)
        assertEquals("Новость 2", topRatedNews[2].title)

        assertEquals(0.9999546021312976, topRatedNews[0].rating, 0.0001)
    }

    @Test
    fun `test getMostRatedNews with empty news list`() {
        val newsList = emptyList<News>()

        val period = LocalDate.of(2024, 9, 1)..LocalDate.of(2024, 9, 30)

        val topRatedNews = newsList.getMostRatedNews(3, period)

        kotlin.test.assertTrue(topRatedNews.isEmpty())
    }

    @Test
    fun `test getMostRatedNews with news outside period`() {
        val newsList = listOf(
            News(1, "Новость 1", null, "Описание 1", "https://example.com/1", 100, 10, 1609459200), // 1 января 2021
            News(2, "Новость 2", null, "Описание 2", "https://example.com/2", 50, 5, 1612137600),  // 1 февраля 2021
        )

        val period = LocalDate.of(2024, 9, 1)..LocalDate.of(2024, 9, 30)

        val topRatedNews = newsList.getMostRatedNews(3, period)

        kotlin.test.assertTrue(topRatedNews.isEmpty())
    }
}