import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.*

open class NewsService : AutoCloseable {
    private val logger = LoggerFactory.getLogger(NewsService::class.java)
    private val client: HttpClient

    init {
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    suspend fun getNews(count: Int = 100): List<News> {
        return try {
            val response: HttpResponse = client.get("https://kudago.com/public-api/v1.4/news/") {
                parameter("page_size", count)
                parameter("order_by", "-publication_date")
                parameter("location", "spb")
                parameter(
                    "fields",
                    "id,title,place,description,site_url,favorites_count,comments_count,publication_date"
                )
            }

            val newsResponse: NewsResponse = response.body()
            logger.info("Successfully fetched ${newsResponse.results.size} news items.")
            newsResponse.results
        } catch (e: Exception) {
            logger.error("Error fetching news: ${e.message}", e)
            throw e
        }
    }

    open fun saveNews(path: String, news: Collection<News>) {
        val filePath = validateFilePath(path)

        try {
            Files.newBufferedWriter(filePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW).use { writer ->
                writeCsvHeader(writer)
                writeCsvContent(writer, news)
                logger.info("Successfully saved ${news.size} news items to $path.")
            }
        } catch (e: IOException) {
            logger.error("Ошибка при записи в файл: $path", e)
            throw RuntimeException("Ошибка при записи в файл: $path", e)
        }
    }

    private fun validateFilePath(path: String): Path {
        val filePath: Path
        try {
            filePath = Paths.get(path)
        } catch (e: InvalidPathException) {
            throw IllegalArgumentException("Некорректный путь: $path", e)
        }

        if (Files.exists(filePath)) {
            throw IllegalArgumentException("Файл по указанному пути уже существует.")
        }

        if (Files.isDirectory(filePath)) {
            throw IllegalArgumentException("Путь указывает на директорию, а не на файл.")
        }

        return filePath
    }

    private fun writeCsvHeader(writer: BufferedWriter) {
        writer.write("id,title,place,description,site_url,favorites_count,comments_count,publication_date")
        writer.newLine()
    }

    private fun writeCsvContent(writer: BufferedWriter, news: Collection<News>) {
        for (n in news) {
            writer.write(
                "${n.id},\"${n.title}\",\"${n.place ?: ""}\",\"${n.description}\",${n.siteUrl}," +
                        "${n.favoritesCount},${n.commentsCount},${n.publicationDate}"
            )
            writer.newLine()
        }
    }

    override fun close() {
        client.close()
    }
}

fun main() = runBlocking {
    val newsService = NewsService()
    val channel = Channel<List<News>>(Channel.UNLIMITED)
    val workerCount = 5
    val totalPages = 10

    val processor = launch {
        val path = "news.csv"
        val allNews = mutableListOf<News>()
        for (newsList in channel) {
            allNews.addAll(newsList)
        }
        newsService.saveNews(path, allNews)
        println("Все новости успешно сохранены в $path")
    }

    val workers = List(workerCount) { workerId ->
        launch {
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

    newsService.close()
}