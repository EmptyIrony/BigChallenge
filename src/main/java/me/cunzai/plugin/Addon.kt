package me.cunzai.plugin

import net.md_5.bungee.api.ChatColor


/*
 * @ Created with IntelliJ IDEA
 * @ Author EmptyIrony
 * @ Date 2021/8/24
 * @ Time 11:56
 */
class Addon {

    fun String.colored(): String{
        return ChatColor.translateAlternateColorCodes('&', this)
    }

}