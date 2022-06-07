package tech.dojo.pay.sdksample.token

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TokenApi {

    @Headers("authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjb25uZWN0LWUtZGV2QGFwcHNwb3QuZ3NlcnZpY2VhY2NvdW50LmNvbSIsImF1ZCI6Imh0dHBzOi8vZS50ZXN0LmNvbm5lY3QucGF5bWVudHNlbnNlLmNsb3VkIiwiZXhwIjoyMzgzMDQ4NDAxLCJpYXQiOjE2MjYxODQ0MDEsInN1YiI6ImNvbm5lY3QtZS1kZXZAYXBwc3BvdC5nc2VydmljZWFjY291bnQuY29tIiwiYXBpS2V5IjoiMTM3ODQyOGMtYTMxNC00NTA5LWFjYTEtNmRhY2EzNGNiM2QyIiwiZW1haWwiOiJjb25uZWN0LWUtZGV2QGFwcHNwb3QuZ3NlcnZpY2VhY2NvdW50LmNvbSJ9.IKX_Kou8grA5_UTkiC4wREq8yYL4gj1W9UG6lXArlm_DQiv1eL26kMfsbzN3dfUWO-H7BJHs8zMX-EN2fXocNq16aUTrdLHtSczVSLbt8kizHcVsOMYotW3syw897vpXJBDe2xWihKMBrr6P1uBFKnx_bDeMR67wvE3-5XIh_zV9hteFneuN9QmEW-QyGEJ9RpyKwrpGKU60SPYM1WO_6L72CgkxSATLwHThsEnUQCsZoOZc058lHzjyVww0T_y7QLYsooXQo2WJy5TIunE3xjf6srZnE6yeQu_0wouUJ_m64y9lmlUNXGzAzNvmgfnDZ1IqhWdfVDiIE6ZOa__H4w")
    @POST("access-tokens")
    suspend fun getToken(@Body params: MerchantParams): TokenResponse
}