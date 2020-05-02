@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )
import groovyx.net.http.*

import java.time.Instant

def http = new HTTPBuilder('http://localhost:5050')
def random = new Random()
//def html = http.get(path : '/search', query : [q:'waffles'])

static Map createObject(Integer id) {
    return [
        id: id.toString(),
        value: UUID.randomUUID().toString()
    ]
}

def currentSecond = Instant.now().epochSecond
def rps = 0
while (true) {
    if (currentSecond != Instant.now().epochSecond) {
        currentSecond = Instant.now().epochSecond
        println("rps: $rps")
        rps = 0
    }

    def obj = createObject(random.nextInt(1_000_000))
    def resp
    try {
        switch (obj.id.toInteger()) {
            case { it % 2 == 0 }:
                http.post(
                        path: "/config/${obj.id}",
                        requestContentType: ContentType.JSON,
                        body: obj
                )
                break
            case { it % 5 == 0 }:
                http.get(path: "/config/${obj.id}")
                break
            default:
                http.post(
                        path: "/config/${obj.id}",
                        requestContentType: ContentType.JSON,
                        body: obj
                )
        }
        rps += 1
    } catch (HttpResponseException e) {
        if (e.message != "Not Found") {
            println("HTTP Execution problem: $e")
        }
    }
}
