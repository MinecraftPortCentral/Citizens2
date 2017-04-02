package net.citizensnpcs.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public abstract class Editor {
    public abstract void begin();

    public abstract void end();

    private static void enter(Player player, Editor editor) {
        editor.begin();
        Sponge.getEventManager().registerListeners(Sponge.getPluginManager().getPlugin("Citizens"), editor);
        EDITING.put(player.getName(), editor);
    }

    public static void enterOrLeave(Player player, Editor editor) {
        if (editor == null)
            return;
        Editor edit = EDITING.get(player.getName());
        if (edit == null) {
            enter(player, editor);
        } else if (edit.getClass() == editor.getClass()) {
            leave(player);
        } else {
            Messaging.sendErrorTr(player, Messages.ALREADY_IN_EDITOR);
        }
    }

    public static boolean hasEditor(Player player) {
        return EDITING.containsKey(player.getName());
    }

    public static void leave(Player player) {
        if (!hasEditor(player))
            return;
        Editor editor = EDITING.remove(player.getName());
        editor.end();
    }

    public static void leaveAll() {
        for (Entry<String, Editor> entry : EDITING.entrySet()) {
            entry.getValue().end();
        }
        EDITING.clear();
    }

    private static final Map<String, Editor> EDITING = new HashMap<String, Editor>();
}