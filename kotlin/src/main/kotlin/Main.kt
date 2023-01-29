import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.net.InetSocketAddress

fun defaultThread() = runBlocking {
    launch {
        println("defaultThread executes on thread ${Thread.currentThread().name}")
    }
}

fun defaultDispatcherThread() = runBlocking {
    launch(Dispatchers.Default) {
        println("defaultDispatcherThread executes on thread ${Thread.currentThread().name}")
    }
}

fun ownThread() = runBlocking {
    launch(newSingleThreadContext("ownThread")) {
        println("ownThread executes on thread ${Thread.currentThread().name}")
    }
}

fun fanInFanOut() = runBlocking {
    val channel = Channel<Int>()
    // fan-in, fan-out
    launch {
        for (x in 1..10) channel.send(x)
    }

    launch {
        repeat(10) { println("Got: ${channel.receive()}") }
    }
    println("it's done")
}

fun <T, U> invertMap(m: MutableMap<T, U>): MutableMap<U, T> {
    val out = mutableMapOf<U, T>()
    for ((k, v) in m) {
        out[v] = k
    }
    return out
}

fun genLeaky(vararg nums: Int): Channel<Int> {
    val out = Channel<Int>()
    val unused = GlobalScope.async {
        for (n in nums) {
            out.send(n)
        }
        out.close()
    }
    return out
}

fun sqLeaky(inChannel: Channel<Int>): Channel<Int> {
    val out = Channel<Int>()
    val unused = GlobalScope.async {
        for (n in inChannel) {
            out.send(n * n)
        }
        out.close()
    }
    return out
}

fun gen(vararg nums: Int): Pair<Channel<Int>, Job> {
    val out = Channel<Int>()
    val job = GlobalScope.async {
        for (n in nums) {
            out.send(n)
        }
        out.close()
    }
    return Pair(out, job)
}

fun sq(inChannel: Channel<Int>): Pair<Channel<Int>, Job> {
    val out = Channel<Int>()
    val job = GlobalScope.async {
        for (y in inChannel) {
            out.send(y * y)
        }
        out.close()
    }
    return Pair(out, job)
}

// func main() {
//    // Set up the pipeline and consume the output.
//    for n := range sq(sq(gen(2, 3))) {
//        fmt.Println(n) // 16 then 81
//    }
//}
suspend fun leakyCoroutinesTest() {
    for (x in sqLeaky(sqLeaky(genLeaky(2, 3)))) {
        println(x)
    }
}

suspend fun nonLeakyCoroutinesTest() {
    val (c, genJob) = gen(2, 3)
    val (sqC, sqJob) = sq(c)
    val (sqCC, sqqJob) = sq(sqC)
    for (x in sqCC) {
        println(x)
    }
    listOf(genJob, sqJob, sqqJob).joinAll()
}

fun main(args: Array<String>) = runBlocking {
    leakyCoroutinesTest()
    nonLeakyCoroutinesTest()
}