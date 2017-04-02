package net.citizensnpcs.trait.waypoint.triggers;

import java.util.List;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.PlayerAnimation;
import net.citizensnpcs.util.Util;
import org.spongepowered.api.command.CommandSource;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class AnimationTriggerPrompt extends StringPrompt implements WaypointTriggerPrompt {
    private final List<PlayerAnimation> animations = Lists.newArrayList();

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("back")) {
            return (Prompt) context.getSessionData("previous");
        }
        if (input.equalsIgnoreCase("finish")) {
            context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, new AnimationTrigger(animations));
            return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
        }
        PlayerAnimation animation = Util.matchEnum(PlayerAnimation.values(), input);
        if (animation == null) {
            Messaging.sendErrorTr((CommandSource) context.getForWhom(), Messages.INVALID_ANIMATION, input,
                    getValidAnimations());
        }
        animations.add(animation);
        Messaging.sendTr((CommandSource) context.getForWhom(), Messages.ANIMATION_ADDED, input);
        return this;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Messaging.sendTr((CommandSource) context.getForWhom(), Messages.ANIMATION_TRIGGER_PROMPT, getValidAnimations());
        return "";
    }

    private String getValidAnimations() {
        return Joiner.on(", ").join(PlayerAnimation.values());
    }
}
