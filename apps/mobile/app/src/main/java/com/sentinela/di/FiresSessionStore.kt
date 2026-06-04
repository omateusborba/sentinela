package com.sentinela.di

import com.sentinela.domain.model.FireHotspot

/** In-memory cache so list/detail screens can read fires loaded on the map. */
class FiresSessionStore {
    @Volatile
    var fires: List<FireHotspot> = emptyList()
}
