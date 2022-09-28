
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

private const val BASE_URL = "http://kotlin-book.bignerdranch.com/2e"
private const val FLIGHT_ENDPOINT = "$BASE_URL/flight"
private const val LOYALTY_ENDPOINT = "$BASE_URL/loyalty"

suspend fun fetchFlight(passengerName: String): FlightStatus = coroutineScope {

    val client = HttpClient(CIO)
    val flightResponse = async {
        println("Started fetching flight info")
        client.get(FLIGHT_ENDPOINT).body<String>().also {
            println("Finished fetching flight info")
        }
    }
    val loyaltyResponse = async {
        println("Started fetching loyalty info")
        client.get(LOYALTY_ENDPOINT).body<String>().also {
            println("Finished fetching loyalty info")
        }
    }
    delay(500)
    println("Combining flight data")
    FlightStatus.parse(
        flightResponse = flightResponse.await(),
        loyaltyResponse = loyaltyResponse.await(),
        passengerName = passengerName
    )
}