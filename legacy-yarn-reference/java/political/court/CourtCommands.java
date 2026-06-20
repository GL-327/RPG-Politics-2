package com.political.court;

import com.political.DataManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Registers the {@code /court} command used by the Judge to open or adjourn a Court Domain. */
public final class CourtCommands {

    private CourtCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) ->
                dispatcher.register(CommandManager.literal("court")
                        .requires(CourtCommands::isJudgeOrOp)
                        .then(CommandManager.literal("adjourn")
                                .executes(ctx -> {
                                    if (!CourtDomainManager.isActive()) {
                                        ctx.getSource().sendError(Text.literal("No court is in session."));
                                        return 0;
                                    }
                                    CourtDomainManager.end("ADJOURNED");
                                    return 1;
                                }))
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(ctx -> {
                                    ServerPlayerEntity judge = ctx.getSource().getPlayerOrThrow();
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
                                    String error = CourtDomainManager.start(judge, target);
                                    if (error != null) {
                                        ctx.getSource().sendError(Text.literal(error));
                                        return 0;
                                    }
                                    ctx.getSource().sendFeedback(() -> Text.literal("Court convened.")
                                            .formatted(Formatting.LIGHT_PURPLE), true);
                                    return 1;
                                }))));
    }

    private static boolean isJudgeOrOp(ServerCommandSource source) {
        if (source.hasPermissionLevel(2)) return true;
        ServerPlayerEntity player = source.getPlayer();
        return player != null && player.getUuidAsString().equals(DataManager.getJudge());
    }
}
