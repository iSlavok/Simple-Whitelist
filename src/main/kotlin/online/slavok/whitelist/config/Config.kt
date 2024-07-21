package online.slavok.whitelist.config

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    var whitelist: Boolean,
    var databaseType: String,
    var mysqlUrl: String,
    var jsonFileName: String,
)