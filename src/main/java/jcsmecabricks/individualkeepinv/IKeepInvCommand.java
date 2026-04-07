package jcsmecabricks.individualkeepinv;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;

import static jcsmecabricks.individualkeepinv.KeepInvMap.kim;
import static net.minecraft.commands.Commands.literal;

public class IKeepInvCommand {

    public static void commandLogic (CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(literal("ikeepinv")

                .then(literal("getdefault")
                        .requires (source -> source.permissions().hasPermission(Permission.Atom.create("command/level/2")))
                        .executes(ctx -> {
                            ctx.getSource().sendSystemMessage(Component.nullToEmpty("The current default state is: " + kim.keepInvDefault));
                            return 1;
                        }))

                .then(literal("default")
                        .requires (source -> source.permissions().hasPermission(Permission.Atom.create("command/level/2")))
                        .then(Commands.argument("boolean", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean bool = BoolArgumentType.getBool(ctx, "boolean");
                                    KeepInvMap.setDefaultState(bool);
                                    ctx.getSource().sendSystemMessage(Component.nullToEmpty("The default state is now: " + bool));
                                    return 1;
                                })))

                .then(literal("get")
                        .then(Commands.argument("target", EntityArgument.player())
                                .requires(source -> source.permissions().hasPermission(Permission.Atom.create("command/level/0")))
                                .executes(ctx -> {
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
                                    if (ctx.getSource().permissions().hasPermission(Permission.Atom.create("command/level/2"))) {  // hasPermissionLevel also works for permission levels above specified, so this works for permission level 4 as well
                                        ctx.getSource().sendSystemMessage(Component.nullToEmpty(player.getName().getString() + "'s inventory state is currently: " + KeepInvMap.getPlayerState(player)));
                                    }
                                    else if (player.equals(ctx.getSource().getPlayer())) { // checks if player executing command is the same as the player passed to the command
                                        ctx.getSource().sendSystemMessage(Component.nullToEmpty(player.getName().getString() + "'s inventory state is currently: " + KeepInvMap.getPlayerState(player)));
                                    }
                                    else {
                                        ctx.getSource().sendFailure(Component.nullToEmpty("Non-OP players cannot view other player's inventory states."));
                                    }
                                    return 1;
                                })))

                .then(literal("set")
                        .then(Commands.argument("target", EntityArgument.player())
                                .then(Commands.argument("boolean", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "target");
                                            boolean bool = BoolArgumentType.getBool(ctx, "boolean");
                                            if (ctx.getSource().permissions().hasPermission(Permission.Atom.create("command/level/2"))) {  // hasPermissionLevel also works for permission levels above specified, so this works for permission level 4 as well
                                                KeepInvMap.setPlayerState(player, bool);
                                                ctx.getSource().sendSystemMessage(Component.nullToEmpty(player.getName().getString() + "'s inventory state has been set to: " + bool));
                                            }
                                            else if (player.equals(ctx.getSource().getPlayer())) { // checks if player executing command is the same as the player passed to the command
                                                KeepInvMap.setPlayerState(player, bool);
                                                ctx.getSource().sendSystemMessage(Component.nullToEmpty(player.getName().getString() + "'s inventory state has been set to: " + bool));
                                            }
                                            else {
                                                ctx.getSource().sendFailure(Component.nullToEmpty("Non-OP players cannot alter other player's inventory states."));
                                            }
                                            return 1;
                                        })))));
    }
}