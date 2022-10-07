import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

//fun simple(): Flow<Int> = flow {
//    for (i in 1..3) {
//        log("Emitting $i")
//        Thread.sleep(1000)
//        emit(i)
//    }
//}.onCompletion { log("Simple flow ended") }.flowOn(Dispatchers.Default)

fun simple(): Flow<Int> = (1..3).asFlow()

fun events(): Flow<Int> = (1..3).asFlow().onEach { delay(1500) }

fun foo(): Flow<Int> = flow {
    for (i in 1..5) {
        log("Emitting $i")
        emit(i)
    }
}

fun main() = runBlocking<Unit> {
//    val nums = (1..3).asFlow().onEach { delay(300) }
//    val strs = flowOf("one", "two", "three").onEach { delay(400) }
//    val startTime = System.currentTimeMillis()
//    nums.combine(strs) { a, b ->
//        "$a -> $b"
//    }.collect { value ->
//        log("$value at ${System.currentTimeMillis() - startTime} ms from start")
//    }
//    val startTime = System.currentTimeMillis()
//    (1..3).asFlow().onEach { delay(100) }
//        .flatMapLatest { requestFlow(it) }
//        .collect { log("$it at ${System.currentTimeMillis() - startTime} ms from start") }
//    simple()
//        .onEach { value ->
//            check(value <= 1) { "Collected $value" }
//            log("$value")
//        }
//        .catch { e -> log("Caught $e") }
//        .collect()
//    simple()
//        .onCompletion { cause ->
//            log("Flow completed with $cause")
//        }
//        .collect { value ->
//            check(value <= 1) { "Collected $value" }
//            log("$value")
//        }
    (1..5).asFlow().cancellable().collect { value ->
        if (value == 3) cancel()
        log("$value")
    }
}