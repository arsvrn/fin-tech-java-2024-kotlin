import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

class NewsServiceThreadPoolTest {

    class MockNewsService : NewsService() {
        suspend fun getNews(page: Int, count: Int): List<News> {
            delay(100)
            return List(count) {
                News(
                    id = page * 100 + it,
                    title = "Title $it",
                    place = "Place $it",
                    description = "Description $it",
                    siteUrl = "https://example.com/$it",
                    favoritesCount = it,
                    commentsCount = it * 2,
                    publicationDateUnix = 1726670699
                )
            }
        }

        override fun saveNews(path: String, news: Collection<News>) {
            println("Сохранено ${news.size} новостей в $path")
        }
    }

    private suspend fun testWithWorkerCount(workerCount: Int, totalPages: Int): Long {
        val newsService = MockNewsService()
        val channel = Channel<List<News>>(Channel.UNLIMITED)

        val processor = GlobalScope.launch {
            val allNews = mutableListOf<News>()
            for (newsList in channel) {
                allNews.addAll(newsList)
            }
            println("Получено ${allNews.size} новостей.")
        }

        val time = measureTimeMillis {
            val workers = List(workerCount) { workerId ->
                GlobalScope.launch {
                    var page = workerId + 1
                    while (page <= totalPages) {
                        val news = newsService.getNews(page)
                        channel.send(news)
                        page += workerCount
                    }
                }
            }

            workers.forEach { it.join() }
            channel.close()

            processor.join()
        }

        println("Тест с $workerCount воркерами занял $time ms.")
        return time
    }

    @Test
    fun `тест с 1 воркером`() = runBlocking {
        val totalPages = 10
        val time = testWithWorkerCount(workerCount = 1, totalPages = totalPages)
        assertTrue(time > 0, "Время выполнения должно быть положительным")
    }

    @Test
    fun `тест с 2 воркерами`() = runBlocking {
        val totalPages = 10
        val time = testWithWorkerCount(workerCount = 2, totalPages = totalPages)
        assertTrue(time > 0, "Время выполнения должно быть положительным")
    }

    @Test
    fun `тест с 5 воркерами`() = runBlocking {
        val totalPages = 10
        val time = testWithWorkerCount(workerCount = 5, totalPages = totalPages)
        assertTrue(time > 0, "Время выполнения должно быть положительным")
    }

    @Test
    fun `тест с 10 воркерами`() = runBlocking {
        val totalPages = 10
        val time = testWithWorkerCount(workerCount = 10, totalPages = totalPages)
        assertTrue(time > 0, "Время выполнения должно быть положительным")
    }
}