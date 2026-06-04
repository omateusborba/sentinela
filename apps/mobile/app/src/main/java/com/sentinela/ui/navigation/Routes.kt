package com.sentinela.ui.navigation

object Routes {
    const val MAP = "map"
    const val FIRE_LIST = "fires"
    const val FIRE_DETAIL = "fires/{fireId}"
    const val POINTS = "points"
    const val POINT_FORM = "points/form/{pointId}"
    const val ALERT = "alert"

    fun fireDetail(fireId: String) = "fires/$fireId"
    fun pointForm(pointId: Long = 0L) = "points/form/$pointId"
}
