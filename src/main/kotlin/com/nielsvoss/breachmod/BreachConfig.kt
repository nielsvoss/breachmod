package com.nielsvoss.breachmod

import com.electronwill.nightconfig.core.file.FileConfig
import net.fabricmc.loader.api.FabricLoader
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

object BreachConfig {
    private const val CONFIG_FILE_NAME = "breach.json"
    private val config: FileConfig by lazy {
        val c = FileConfig.of(FabricLoader.getInstance().configDir.resolve(CONFIG_FILE_NAME))
        c.load()
        c
    }

    data class Setting<T>(val settingKey : String, val defaultValue : T) {
        fun get(): T = config.get(settingKey) ?: defaultValue
        fun set(newValue : T) {
            config.set<T>(settingKey, newValue)
            config.save()
        }
    }

    fun getSettings(): List<Setting<*>> {
        return BreachConfig::class.memberProperties
            .filter { it.returnType.isSubtypeOf(Setting::class.starProjectedType) }
            .map { it.get(BreachConfig) as Setting<*> }
    }

    fun saveDefaults() {
        for (setting in getSettings()) {
            config.add(setting.settingKey, setting.defaultValue)
        }
        config.save()
    }

    val instantKillArrows = Setting("instantKillArrows", true)
    val prepLengthInSeconds = Setting("prepLengthInSeconds", 30)
    val roundLengthInSeconds = Setting("roundLengthInSeconds", 180)
}