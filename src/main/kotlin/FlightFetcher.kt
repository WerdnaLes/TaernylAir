import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URL

private const val BASE_URL = "http://kotlin-book.bignerdranch.com/2e"
private const val FLIGHT_ENDPOINT = "$BASE_URL/flight"
private const val LOYALTY_ENDPOINT = "$BASE_URL/loyalty"

fun main() {
    runBlocking {
        println("Started")
        launch {
            val flight = fetchFlight()
            println(flight)
        }
        println("Finished")
    }

    //    val seconds = (listInMillis / 1000) % 60
//    val minutes = ((listInMillis / 1000) % 3600) / 60
//    val hours = (listInMillis / 1000) / 3600
//    println("List completed in ${"%d:%02d:%02d".format(Locale.US, hours, minutes, seconds)}")
}

suspend fun fetchFlight(): String {
    val client = HttpClient(CIO)
    val flightResponse = client.get(FLIGHT_ENDPOINT).body<String>()
    val loyaltyResponse = client.get(LOYALTY_ENDPOINT).body<String>()
    return "$flightResponse\n$loyaltyResponse"
}