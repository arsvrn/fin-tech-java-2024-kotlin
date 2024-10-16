import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.exp

private val logger = LoggerFactory.getLogger("NewsLogger")

@Serializable
data class News(
    val id: Int,
    val title: String,
    val place: String?,
    val description: String?,
    @JsonNames("site_url")
    val siteUrl: String?,
    @JsonNames("favorites_count")
    val favoritesCount: Int?,
    @JsonNames("comments_count")
    val commentsCount: Int?,
    @JsonNames("publication_date")
    val publicationDateUnix: Long
) {
    val publicationDate: LocalDate by lazy {
        try {
            Instant.ofEpochSecond(publicationDateUnix)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        } catch (e: Exception) {
            logger.error("Error converting Unix timestamp to LocalDate: ${e.message}", e)
            throw e
        }
    }

    val rating: Double by lazy {
        try {
            calculateRating()
        } catch (e: Exception) {
            logger.error("Error calculating rating: ${e.message}", e)
            throw e
        }
    }

    private fun calculateRating(): Double {
        val favorites = favoritesCount ?: 0
        val comments = commentsCount ?: 0
        return 1.0 / (1 + exp(-(favorites.toDouble() / (comments + 1))))
    }
}

fun List<News>.getMostRatedNews(count: Int, period: ClosedRange<LocalDate>): List<News> {
    return try {
        this
            .filter { it.publicationDate in period }
            .sortedByDescending { it.rating }
            .take(count)
    } catch (e: Exception) {
        logger.error("Error while filtering and sorting news: ${e.message}", e)
        throw e
    }
}