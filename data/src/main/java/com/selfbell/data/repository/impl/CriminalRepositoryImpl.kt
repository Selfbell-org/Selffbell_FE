package com.selfbell.data.repository.impl

import com.selfbell.data.api.CriminalApi
import com.selfbell.data.mapper.toDomainModel
import com.selfbell.domain.model.Criminal
import com.selfbell.domain.repository.CriminalRepository
import javax.inject.Inject

class CriminalRepositoryImpl @Inject constructor(
    private val api: CriminalApi
) : CriminalRepository {

    override suspend fun getNearbyCriminals(lat: Double, lon: Double, radius: Int): List<Criminal> {
        return api.getNearbyCriminals(lat, lon, radius).map { it.toDomainModel() }
    }
}
