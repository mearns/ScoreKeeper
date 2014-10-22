package scorekeeper.site24x7

import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.Method.GET

class Site24x7Client {
    private final String url

    Site24x7Client(String url) {
        this.url = url
    }

    List<Site24x7Monitor> getMonitors() {
        def http = new HTTPBuilder(url)
        List<Site24x7Monitor> monitors = new ArrayList<>()

        http.request(GET, XML) {
            response.success = { resp, xml ->
                xml.result.response.group.each { GPathResult group ->
                    group.monitor.each { GPathResult res ->
                        def monitor = new Site24x7Monitor(monitorId: res.'@monitorid',
                                displayName: res.'@displayname',
                                monitorType: res.'@monitortype',
                                status: res.'@status',
                                rspValue: res.'@rspvalue')

                        monitors.add(monitor)
                    }
                }
            }
            response.failure = { resp ->
                throw new Exception("Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}")
            }
        }

        monitors
    }
}
