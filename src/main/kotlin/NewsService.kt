import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.*

class NewsService {
    private val logger = LoggerFactory.getLogger(NewsService::class.java)

    suspend fun getNews(count: Int = 100): List<News> {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

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
        } finally {
            client.close()
        }
    }

    fun saveNews(path: String, news: Collection<News>) {
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

        try {
            Files.newBufferedWriter(filePath, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW).use { writer ->
                writer.write("id,title,place,description,site_url,favorites_count,comments_count,publication_date")
                writer.newLine()

                for (n in news) {
                    writer.write("${n.id},\"${n.title}\",\"${n.place ?: ""}\",\"${n.description}\",${n.siteUrl},${n.favoritesCount},${n.commentsCount},${n.publicationDate}")
                    writer.newLine()
                }

                logger.info("Successfully saved ${news.size} news items to $path.")
            }
        } catch (e: IOException) {
            logger.error("Ошибка при записи в файл: $path", e)
            throw RuntimeException("Ошибка при записи в файл: $path", e)
        }
    }
}