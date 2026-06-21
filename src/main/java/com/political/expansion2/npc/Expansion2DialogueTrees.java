package com.political.expansion2.npc;

import com.political.npc.DialogueNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Expansion2DialogueTrees {

    private static final Map<String, DialogueNode> NODES = new HashMap<>();

    static {
        for (NpcArchetype arch : NpcArchetype.values()) registerTree(arch);
        NODES.put("farewell", node("farewell", line("", "Safe travels.", ChatFormatting.GRAY), List.of()));
    }

    private Expansion2DialogueTrees() {}

    public static DialogueNode start(NpcArchetype arch, String name) {
        return relabel(NODES.get(arch.name() + ":greet"), name, arch);
    }

    public static DialogueNode nodeById(NpcArchetype arch, String name, String nodeId) {
        String key = nodeId.contains(":") ? nodeId : arch.name() + ":" + nodeId;
        DialogueNode n = NODES.get(key);
        return n == null ? start(arch, name) : relabel(n, name, arch);
    }

    public static DialogueNode farewell(String name, NpcArchetype arch) {
        return relabel(NODES.get("farewell"), name, arch);
    }

    public static DialogueNode result(String name, NpcArchetype arch, String text) {
        return node("result", say(name, text, arch), List.of(choice("Goodbye", "bye", "exp2:farewell")));
    }

    private static void registerTree(NpcArchetype arch) {
        String p = arch.name();
        NODES.put(p + ":greet", node(p + ":greet", say("", arch.tagline, arch), greetChoices(arch)));
        NODES.put(p + ":lore", node(p + ":lore", line("", loreText(arch), arch.color),
                List.of(
                        choice("What can you do?", "svc", "exp2:nav:" + p + ":services"),
                        choice("Any jobs?", "q", "exp2:nav:" + p + ":quests"),
                        choice("Back", "back", "exp2:nav:" + p + ":greet"))));
        NODES.put(p + ":services", node(p + ":services", line("", "Name your need.", arch.color), serviceChoices(arch)));
        NODES.put(p + ":quests", node(p + ":quests", line("", "Contracts pay coin or credit.", arch.color),
                List.of(
                        choice("Show contracts", "list", "exp2:quest:list"),
                        choice("Turn in work", "turnin", "exp2:quest:turnin"),
                        choice("Back", "back", "exp2:nav:" + p + ":greet"))));
        NODES.put(p + ":rumors", node(p + ":rumors", line("", "Rumors cost nothing.", arch.color),
                List.of(
                        choice("Spirits?", "s", "exp2:nav:" + p + ":spirit_lore"),
                        choice("Politics?", "pol", "exp2:nav:" + p + ":pol_lore"),
                        choice("Back", "back", "exp2:nav:" + p + ":greet"))));
        NODES.put(p + ":spirit_lore", node(p + ":spirit_lore",
                line("", "Grade-four wisps swarm the wilds. Calamities stalk cursed ruins.", ChatFormatting.DARK_PURPLE),
                List.of(choice("Back", "back", "exp2:nav:" + p + ":rumors"))));
        NODES.put(p + ":pol_lore", node(p + ":pol_lore",
                line("", "Elections turn when turnout spikes. Bank deposits build influence.", ChatFormatting.YELLOW),
                List.of(choice("Back", "back", "exp2:nav:" + p + ":rumors"))));
        NODES.put(p + ":boss_hint", node(p + ":boss_hint", line("", bossHint(arch), ChatFormatting.DARK_RED),
                List.of(
                        choice("Where?", "loc", "exp2:boss:hint"),
                        choice("Back", "back", "exp2:nav:" + p + ":greet"))));
    }

    private static List<DialogueNode.DialogueChoice> greetChoices(NpcArchetype arch) {
        List<DialogueNode.DialogueChoice> c = new ArrayList<>();
        c.add(choice("Who are you?", "lore", "exp2:nav:" + arch.name() + ":lore"));
        c.add(choice("I need services", "svc", "exp2:nav:" + arch.name() + ":services"));
        c.add(choice("Got any jobs?", "jobs", "exp2:nav:" + arch.name() + ":quests"));
        if (arch == NpcArchetype.SCOUT || arch == NpcArchetype.TOWN_CRIER || arch == NpcArchetype.INNKEEPER)
            c.add(choice("Rumors?", "rumors", "exp2:nav:" + arch.name() + ":rumors"));
        if (arch == NpcArchetype.BOUNTY_BROKER || arch == NpcArchetype.SPIRIT_HUNTER
                || arch == NpcArchetype.MERCENARY || arch == NpcArchetype.EXORCIST)
            c.add(choice("Dangerous figures?", "boss", "exp2:nav:" + arch.name() + ":boss_hint"));
        c.add(choice("Farewell", "bye", "exp2:farewell"));
        return c;
    }

    private static List<DialogueNode.DialogueChoice> serviceChoices(NpcArchetype arch) {
        List<DialogueNode.DialogueChoice> c = new ArrayList<>();
        for (String svc : Expansion2NpcServices.servicesFor(arch))
            c.add(choice(Expansion2NpcServices.label(svc), svc, "exp2:svc:" + svc));
        c.add(choice("Back", "back", "exp2:nav:" + arch.name() + ":greet"));
        return c;
    }

    private static String loreText(NpcArchetype arch) {
        return switch (arch) {
            case BLACKSMITH -> "Three generations of hammers. I temper steel against cursed fire.";
            case ENCHANTER -> "I bind whispers into metal. Some clients regret the bargain.";
            case SORCERER -> "Grade-one techniques aren't sold — they're earned in blood.";
            case BOUNTY_BROKER -> "Every monster has a price on its head.";
            case BANKER -> "The vault sleeps beneath the settlement.";
            case POLITICIAN -> "Public service is a ladder.";
            case HEALER -> "I mend flesh. Souls require a specialist.";
            case CURSED_MERCHANT -> "My goods bite back.";
            case ARMOR_SMITH -> "A good breastplate turns a killing blow into a bruise.";
            case WEAPON_SMITH -> "Balance, edge, and a curse-slot if you pay extra.";
            case ALCHEMIST -> "One wrong drop and you'll glow for a week.";
            case SCOUT -> "I've mapped cursed groves the council denies.";
            case MERCENARY -> "Loyalty lasts exactly as long as the contract.";
            case TAX_COLLECTOR -> "Civilization isn't free.";
            case ELECTION_CLERK -> "Democracy is messy. My paperwork is pristine.";
            case SPIRIT_HUNTER -> "Tracks don't lie.";
            case CURSE_SCHOLAR -> "Academia funded my research until specimens escaped.";
            case RUNE_CARVER -> "Cut the glyph wrong and the stone screams.";
            case GEM_CUTTER -> "Some gems hum. Those cost triple.";
            case HERBALIST -> "Nature heals — if you know which roots aren't venomous.";
            case INNKEEPER -> "Ale drowns sorrow.";
            case FENCE -> "If it's hot, I cool it.";
            case LOAN_SHARK -> "Default once and your kneecaps learn compound interest.";
            case AUCTIONEER -> "Today's lot: misery, slightly used.";
            case TOWN_CRIER -> "I shout truths the chair would rather whisper.";
            case DIPLOMAT -> "Treaties are spells written in ink.";
            case WARDEN -> "The stocks are empty. Help me keep them that way.";
            case EXORCIST -> "Bell, blade, and bound scripture.";
            case SHRINE_KEEPER -> "The altar drinks offerings.";
            case BEAST_TAMER -> "Monsters respond to tone.";
            case MAPMAKER -> "X marks danger.";
            case RELIC_DEALER -> "Provenance is fiction. Power is real.";
            case GRIMOIRE_SELLER -> "Don't read page forty-seven aloud.";
            case SUMMONER -> "Circles contain. Until they don't.";
            case DOOMSDAY_PROPHET -> "The sky will crack.";
        };
    }

    private static String bossHint(NpcArchetype arch) {
        NamedNpcBoss b = Expansion2BossSpawner.hintForArchetype(arch);
        return b == null ? "Dangerous folk hide in ruins."
                : "They call them " + b.displayName + ". Command: /exp2quest boss " + b.id;
    }

    private static DialogueNode relabel(DialogueNode n, String name, NpcArchetype arch) {
        String text = n.line().getString();
        int idx = text.indexOf(": ");
        if (idx >= 0) text = text.substring(idx + 2);
        return new DialogueNode(n.id(), say(name, text, arch), n.choices());
    }

    private static DialogueNode node(String id, Component line, List<DialogueNode.DialogueChoice> choices) {
        return new DialogueNode(id, line, choices);
    }

    private static DialogueNode.DialogueChoice choice(String label, String id, String action) {
        return new DialogueNode.DialogueChoice(id, Component.literal(label).withStyle(ChatFormatting.WHITE), id, action);
    }

    private static Component say(String name, String text, NpcArchetype arch) {
        return Component.literal(name + ": ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(text).withStyle(arch.color));
    }

    private static Component line(String name, String text, ChatFormatting fmt) {
        if (name.isEmpty()) return Component.literal(text).withStyle(fmt);
        return Component.literal(name + ": ").withStyle(ChatFormatting.GOLD).append(Component.literal(text).withStyle(fmt));
    }
}
