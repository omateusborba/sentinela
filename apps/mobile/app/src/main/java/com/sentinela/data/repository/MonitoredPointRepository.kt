package com.sentinela.data.repository

import com.sentinela.data.local.AppDatabase
import com.sentinela.data.local.toDomain
import com.sentinela.data.local.toEntity
import com.sentinela.domain.model.MonitoredPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MonitoredPointRepository(
    database: AppDatabase,
) {
    private val dao = database.monitoredPointDao()

    fun observeAll(): Flow<List<MonitoredPoint>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): MonitoredPoint? =
        dao.getById(id)?.toDomain()

    suspend fun upsert(point: MonitoredPoint): Long {
        val entity = point.toEntity()
        return if (point.id == 0L) {
            dao.insert(entity.copy(id = 0))
        } else {
            dao.update(entity)
            point.id
        }
    }

    suspend fun delete(point: MonitoredPoint) {
        dao.delete(point.toEntity())
    }

    suspend fun updateAlertStates(points: List<MonitoredPoint>) {
        points.forEach { dao.updateAlertState(it.id, it.inAlert) }
    }
}
