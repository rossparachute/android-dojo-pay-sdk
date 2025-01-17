package tech.dojo.pay.sdk.card.data.entities

data class BaseUrlRaw(
    val format: String?,
    val baseUrl: String?,
    val baseClientEventUrl: String?
)
data class BaseUrlResult(
    val baseUrl: String,
    val lastModifiedDate: String?
)
