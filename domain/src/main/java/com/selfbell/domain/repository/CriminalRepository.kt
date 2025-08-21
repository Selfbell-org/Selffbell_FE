package com.selfbell.domain.repository

import com.selfbell.domain.model.Criminal

interface CriminalRepository {
    suspend fun getNearbyCriminals(lat: Double, lon: Double, radius: Int): List<Criminal>
}
