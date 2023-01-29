package main.kotlin

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress


class Handler : HttpHandler {
    override fun handle(exchange: HttpExchange?) {
        val resp = "<h1>hello</h1>".toByteArray()
        exchange?.sendResponseHeaders(200, resp.size.toLong())
        exchange?.responseBody?.write(resp)
        exchange?.close()
    }
}

fun startServer() {
    val addr = InetSocketAddress(9999)
    val server = HttpServer.create(addr, 2);
    server.createContext("/", Handler())
    println("[${server.address}] ==> starting to accept connections")
    server.start()
}