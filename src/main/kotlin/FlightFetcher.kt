import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.*

private const val BASE_URL = "http://kotlin-book.bignerdranch.com/2e"
private const val FLIGHT_ENDPOINT = "$BASE_URL/flight"
private const val LOYALTY_ENDPOINT = "$BASE_URL/loyalty"

fun main() {
    runBlocking {
        println("Started")
        launch {
            var flight = fetchFlight("Satyricon")

            while (flight.status == "Canceled") {
                println("The flight ${flight.flightNumber} is canceled, searching for another flight...")
                flight = fetchFlight("Satyr")
            }
            println(flight)
        }
        println("Finished")
    }

//    val seconds = (listInMillis / 1000) % 60
//    val minutes = ((listInMillis / 1000) % 3600) / 60
//    val hours = (listInMillis / 1000) / 3600
//    println("List completed in ${"%d:%02d:%02d".format(Locale.US, hours, minutes, seconds)}")
}

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