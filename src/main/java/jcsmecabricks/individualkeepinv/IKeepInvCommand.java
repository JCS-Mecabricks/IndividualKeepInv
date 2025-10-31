package jcsmecabricks.individualkeepinv;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static jcsmecabricks.individualkeepinv.KeepInvMap.kim;
import static net.minecraft.server.command.CommandManager.literal;

public class IKeepInvCommand {

    public static void commandLogic (CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("ikeepinv")

                .then(CommandManager.literal("getdefault")
                        .requires (source -> source.hasPermissionLevel(2))
                                .executes(ctx -> {
                                    ctx.getSource().sendMessage(Text.of("The current default state is: " + kim.keepInvDefault));
                                    return 1;
                                }))

                .then(CommandManager.literal("default")
                        .requires (source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("boolean", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean bool = BoolArgumentType.getBool(ctx, "boolean");
                                    KeepInvMap.setDefaultState(bool);
                                    ctx.getSource().sendMessage(Text.of("The default state is now: " + bool));
                                    return 1;
                                })))

            .then(CommandManager.literal("get")
                    .then(CommandManager.argument("target", EntityArgumentType.player())
                            .requires(source -> source.hasPermissionLevel(0))
                            .executes(ctx -> {
                                ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "target");
                                if (ctx.getSource().hasPermissionLevel(2)) {  // hasPermissionLevel also works for permission levels above specified, so this works for permission level 4 as well
                                    ctx.getSource().sendMessage(Text.of(player.getName().getString() + "'s inventory state is currently: " + KeepInvMap.getPlayerState(player)));
                                }
                                else if (player.equals(ctx.getSource().getPlayer())) { // checks if player executing command is the same as the player passed to the command
                                    ctx.getSource().sendMessage(Text.of(player.getName().getString() + "'s inventory state is currently: " + KeepInvMap.getPlayerState(player)));
                                }
                                else {
                                    ctx.getSource().sendError(Text.of("Non-OP players cannot view other player's inventory states."));
                                }
                                return 1;
                            })))

            .then(CommandManager.literal("set")
                    .then(CommandManager.argument("target", EntityArgumentType.player())
                            .then(CommandManager.argument("boolean", BoolArgumentType.bool())
                                    .executes(ctx -> {
                                        ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "target");
                                        boolean bool = BoolArgumentType.getBool(ctx, "boolean");
                                        if (ctx.getSource().hasPermissionLevel(2)) {  // hasPermissionLevel also works for permission levels above specified, so this works for permission level 4 as well
                                            KeepInvMap.setPlayerState(player, bool);
                                            ctx.getSource().sendMessage(Text.of(player.getName().getString() + "'s inventory state has been set to: " + bool));
                                        }
                                        else if (player.equals(ctx.getSource().getPlayer())) { // checks if player executing command is the same as the player passed to the command
                                            KeepInvMap.setPlayerState(player, bool);
                                            ctx.getSource().sendMessage(Text.of(player.getName().getString() + "'s inventory state has been set to: " + bool));
                                        }
                                        else {
                                            ctx.getSource().sendError(Text.of("Non-OP players cannot alter other player's inventory states."));
                                        }
                                        return 1;
                                    })))));
    }
}
