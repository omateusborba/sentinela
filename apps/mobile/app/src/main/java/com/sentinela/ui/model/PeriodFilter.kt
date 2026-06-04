package com.sentinela.ui.model

enum class PeriodFilter(val label: String, val days: Int) {
    H24("24h", 1),
    D3("3d", 3),
    D5("5d", 5),
}
