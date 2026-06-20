package com.political.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.political.politics.DataManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/** Personal player utilities: homes, checkpoints, server spawn, parties, titles. */
public final class PlayerCommands {

    private static final Predicate<CommandSourceStack> OP = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);
    private static final int CHECKPOINT_COST = 5000;
    private static final int MAX_CHECKPOINTS = 5;

    private PlayerCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("sethome").executes(PlayerCommands::setHome));
        d.register(Commands.literal("home").executes(PlayerCommands::home));

        d.register(Commands.literal("setspawn").requires(OP).executes(PlayerCommands::setSpawn));
        d.register(Commands.literal("spawn").executes(PlayerCommands::spawn));

        d.register(Commands.literal("checkpoint")
                .then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.word())
                        .executes(PlayerCommands::cpCreate)))
                .then(Commands.literal("tp").then(Commands.argument("name", StringArgumentType.word())
                        .executes(PlayerCommands::cpTp)))
                .then(Commands.literal("delete").then(Commands.argument("name", StringArgumentType.word())
                        .executes(PlayerCommands::cpDelete)))
                .then(Commands.literal("list").executes(PlayerCommands::cpList)));

        d.register(Commands.literal("party")
                .then(Commands.literal("create").executes(PlayerCommands::partyCreate))
                .then(Commands.literal("invite").then(Commands.argument("player", EntityArgument.player())
                        .executes(PlayerCommands::partyInvite)))
                .then(Commands.literal("accept").executes(PlayerCommands::partyAccept))
                .then(Commands.literal("leave").executes(PlayerCommands::partyLeave))
                .then(Commands.literal("list").executes(PlayerCommands::partyList)));

        d.register(Commands.literal("settitle").requires(OP)
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("title", StringArgumentType.word())
                                .then(Commands.argument("color", StringArgumentType.word())
                                        .executes(PlayerCommands::setTitle)))));
    }

    // --- Homes ---

    private static int setHome(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        DataManager.data().homes.put(p.getStringUUID(), Locations.serialize(p));
        return ok(c, "Home set.");
    }

    private static int home(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String loc = DataManager.data().homes.get(p.getStringUUID());
        if (loc == null) return fail(c, "You have no home. Use /sethome first.");
        return Locations.teleport(p, loc) ? ok(c, "Teleported home.") : fail(c, "Could not teleport home.");
    }

    // --- Spawn ---

    private static int setSpawn(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        DataManager.data().serverSpawn = Locations.serialize(p);
        return ok(c, "Server spawn set.");
    }

    private static int spawn(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String loc = DataManager.data().serverSpawn;
        if (loc == null || loc.isEmpty()) return fail(c, "Server spawn has not been set.");
        return Locations.teleport(p, loc) ? ok(c, "Teleported to spawn.") : fail(c, "Could not teleport to spawn.");
    }

    // --- Checkpoints ---

    private static Map<String, String> cps(String uuid) {
        return DataManager.data().checkpoints.computeIfAbsent(uuid, k -> new HashMap<>());
    }

    private static int cpCreate(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(c, "name");
        Map<String, String> map = cps(p.getStringUUID());
        if (map.size() >= MAX_CHECKPOINTS && !map.containsKey(name))
            return fail(c, "You have reached the maximum of " + MAX_CHECKPOINTS + " checkpoints.");
        if (!DataManager.removeCoins(p.getStringUUID(), CHECKPOINT_COST))
            return fail(c, "Creating a checkpoint costs " + CHECKPOINT_COST + " coins.");
        map.put(name, Locations.serialize(p));
        return ok(c, "Checkpoint '" + name + "' created.");
    }

    private static int cpTp(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(c, "name");
        String loc = cps(p.getStringUUID()).get(name);
        if (loc == null) return fail(c, "No checkpoint named '" + name + "'.");
        return Locations.teleport(p, loc) ? ok(c, "Teleported to '" + name + "'.") : fail(c, "Could not teleport.");
    }

    private static int cpDelete(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        String name = StringArgumentType.getString(c, "name");
        if (cps(p.getStringUUID()).remove(name) == null) return fail(c, "No checkpoint named '" + name + "'.");
        DataManager.addCoins(p.getStringUUID(), CHECKPOINT_COST / 2);
        return ok(c, "Checkpoint '" + name + "' deleted (refunded " + (CHECKPOINT_COST / 2) + " coins).");
    }

    private static int cpList(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        Map<String, String> map = cps(p.getStringUUID());
        if (map.isEmpty()) return ok(c, "You have no checkpoints.");
        return ok(c, "Checkpoints: " + String.join(", ", map.keySet()));
    }

    // --- Party ---

    private static int partyCreate(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        return PartyManager.create(p) ? ok(c, "Party created. Invite others with /party invite <player>.")
                : fail(c, "You are already in a party.");
    }

    private static int partyInvite(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        if (!PartyManager.invite(p, target)) return fail(c, "Could not invite (you must be a party leader and they must be partyless).");
        target.sendSystemMessage(Component.literal(p.getName().getString() + " invited you to their party. Use /party accept.")
                .withStyle(ChatFormatting.GREEN));
        return ok(c, "Invited " + target.getName().getString() + ".");
    }

    private static int partyAccept(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        return PartyManager.accept(p) ? ok(c, "You joined the party.") : fail(c, "You have no pending party invite.");
    }

    private static int partyLeave(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        PartyManager.leave(p);
        return ok(c, "You left your party.");
    }

    private static int partyList(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        var leader = PartyManager.leaderOf(p.getUUID());
        if (leader == null) return ok(c, "You are not in a party.");
        StringBuilder sb = new StringBuilder("Party: ");
        boolean first = true;
        for (var m : PartyManager.membersOf(leader)) {
            if (!first) sb.append(", ");
            sb.append(DataManager.nameOf(m.toString()));
            first = false;
        }
        return ok(c, sb.toString());
    }

    // --- Titles ---

    private static int setTitle(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(c, "player");
        String title = StringArgumentType.getString(c, "title");
        String colorName = StringArgumentType.getString(c, "color");
        ChatFormatting color;
        try {
            color = ChatFormatting.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fail(c, "Unknown colour: " + colorName);
        }
        DataManager.data().titles.put(target.getStringUUID(), title + "|" + color.name());
        return ok(c, "Set " + target.getName().getString() + "'s title to [" + title + "].");
    }

    private static int ok(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg).withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int fail(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendFailure(Component.literal(msg).withStyle(ChatFormatting.RED));
        return 0;
    }
}
