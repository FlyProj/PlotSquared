/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *               Copyright (C) 2014 - 2022 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "leave",
        permission = "plots.leave",
        usage = "/plot leave",
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.PLAYER)
public class Leave extends Command {

    private final EventDispatcher eventDispatcher;

    @Inject
    public Leave(final @NonNull EventDispatcher eventDispatcher) {
        super(MainCommand.getInstance(), true);
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public CompletableFuture<Boolean> execute(
            PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), TranslatableCaption.of("errors.not_in_plot"));
        checkTrue(plot.hasOwner(), TranslatableCaption.of("info.plot_unowned"));
        if (plot.isOwner(player.getUUID())) {
            player.sendMessage(TranslatableCaption.of("member.plot_cant_leave_owner"));
        } else {
            UUID uuid = player.getUUID();
            if (plot.isAdded(uuid)) {
                if (plot.removeTrusted(uuid)) {
                    this.eventDispatcher.callTrusted(player, plot, uuid, false);
                }
                if (plot.removeMember(uuid)) {
                    this.eventDispatcher.callMember(player, plot, uuid, false);
                }
                player.sendMessage(
                        TranslatableCaption.of("member.plot_left"),
                        Template.of("player", player.getName())
                );
            } else {
                player.sendMessage(
                        TranslatableCaption.of("members.not_added_trusted")
                );
            }
        }
        return CompletableFuture.completedFuture(true);
    }

}
