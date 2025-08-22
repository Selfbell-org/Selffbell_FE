package com.selfbell.data.api.response

import com.google.gson.annotations.SerializedName
 
data class UserExistsResponse(
    @SerializedName("isExists") val isExists: Boolean
) 