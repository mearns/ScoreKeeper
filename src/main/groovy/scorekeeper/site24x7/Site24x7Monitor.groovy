package scorekeeper.site24x7

import groovy.transform.Canonical

@Canonical
class Site24x7Monitor {
    String monitorId
    String displayName
    String monitorType
    String status
    String rspValue
}
