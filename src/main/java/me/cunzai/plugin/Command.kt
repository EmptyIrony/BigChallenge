package me.cunzai.plugin

import io.izzel.taboolib.module.command.lite.CommandBuilder
import io.izzel.taboolib.module.inject.TInject
import me.cunzai.plugin.game.Game


/*
 * @ Created with IntelliJ IDEA
 * @ Author EmptyIrony
 * @ Date 2021/8/23
 * @ Time 17:08
 */

object Command {

    @TInject
    val startCommand = CommandBuilder.create("start", BigChallenge.plugin)
        .execute { sender, args ->



        }.build()

    @TInject
    val unitTest = CommandBuilder.create("unit", BigChallenge.plugin)
        .execute { commandSender, args ->
            Game.startRound()
        }.build()

}