package com.sentinela.data

data class MonitorRegion(
    val id: String,
    val name: String,
    val bbox: String,
)

object MonitorRegions {
    val ALL: List<MonitorRegion> = listOf(
        MonitorRegion("north", "Norte", "-74,-10,-46,6"),
        MonitorRegion("northeast", "Nordeste", "-46,-18,-34,0"),
        MonitorRegion("center-west", "Centro-Oeste", "-65,-25,-46,-8"),
        MonitorRegion("southeast", "Sudeste", "-53,-25,-39,-15"),
        MonitorRegion("south", "Sul", "-58,-35,-48,-22"),
    )
}
