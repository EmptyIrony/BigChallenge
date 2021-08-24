package me.cunzai.plugin.challenge


/*
 * @ Created with IntelliJ IDEA
 * @ Author EmptyIrony
 * @ Date 2021/8/23
 * @ Time 17:30
 */
interface Challenge {

    fun getChallengeName(): String

    fun getDescription(): String

    fun onStart()

    fun onEnd()



}