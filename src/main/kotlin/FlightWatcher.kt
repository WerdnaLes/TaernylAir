
import BoardingState.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.flow.*

val bannedPassengers = setOf("Nogartse")


fun main() {
    runBlocking {
        println("Getting the latest flight info...")
        val flights = fetchFlights()
        val flightDescriptions = flights.joinToString {
            "${it.passengerName} (${it.flightNumber})"
        }
        println("Found flights for $flightDescriptions")
        val flightsAtGate = MutableStateFlow(flights.size)
        launch {
            flightsAtGate
                .takeWhile { it > 0 }
                .onCompletion { println("Finished tracking all flights") }
                .collect { flightCount ->
                    println(
                        when {
                            flightCount > 1 -> "There are $flightCount flights being tracked"
                            else -> "There is $flightCount flight being tracked"
                        }
                    )
                }
        }
        launch {
            flights.forEach {
                watchFlight(it)
                flightsAtGate.value = flightsAtGate.value - 1
            }
        }
    }
}

suspend fun watchFlight(initialFlight: FlightStatus) {
    val passengerName = initialFlight.passengerName
    val currentFlight: Flow<FlightStatus> = flow {
        require(passengerName !in bannedPassengers) {
            "Cannot track $passengerName's flight. They are banned from the airport"
        }
        var flight = initialFlight
        while (flight.departureTimeInMinutes >= 0 &&
            !flight.isFlightCanceled
        ) {
            emit(flight)
            delay(200)
            flight = flight.copy(
                departureTimeInMinutes =
                flight.departureTimeInMinutes - 1
            )
        }
    }
    currentFlight
        .catch { throwable ->
            throwable.printStackTrace()
            System.err.println("Nogartse is banned")
        }
        .map { flight ->
            when (flight.boardingStatus) {
                FlightCanceled -> "Your flight was canceled"
                BoardingNotStarted -> "Boarding will start soon"
                WaitingToBoard -> "Other passengers are boarding"
                Boarding -> "You can now board the plane"
                BoardingEnded -> "The boarding doors have closed"
            } + " (Flight departs in ${flight.departureTimeInMinutes} minutes)"
        }
        .onCompletion { println("Finished tracking $passengerName's flight") }
        .collect { status ->
            println("$passengerName: $status")
        }
}

suspend fun fetchFlights(
    passengerNames: List<String> = listOf(
        "Satyricon",
        "Polarcubis",
        "Estragon",
        "Taernyl",
    ),
    numberOfWorkers: Int = 2
): List<FlightStatus> = coroutineScope {
    val passengerNamesChannel = Channel<String>()
    val fetchedFlightsChannel = Channel<FlightStatus>()

    launch {
        passengerNames.forEach {
            passengerNamesChannel.send(it)
            log("sent $it")
        }
        passengerNamesChannel.close()
    }

    launch {
        (1..numberOfWorkers).map {
            launch {
                log("fetched flight BEFORE")
                fetchFlightStatuses(
                    fetchChannel = passengerNamesChannel,
                    resultChannel = fetchedFlightsChannel
                )
                log("fetched flight AFTER")
            }
        }.joinAll()
        fetchedFlightsChannel.close()
    }

    fetchedFlightsChannel.toList()
}

suspend fun fetchFlightStatuses(
    fetchChannel: ReceiveChannel<String>,
    resultChannel: SendChannel<FlightStatus>
) {
    for (passengerName in fetchChannel) {
        val flight = fetchFlight(passengerName)
        log("Fetched flight: $flight")
        resultChannel.send(flight)
    }
}
