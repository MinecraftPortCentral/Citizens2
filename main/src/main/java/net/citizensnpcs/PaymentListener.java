package net.citizensnpcs;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.util.Tristate;

import java.math.BigDecimal;

import com.google.common.base.Preconditions;

public class PaymentListener  {
    private final EconomyService provider;

    public PaymentListener(EconomyService provider) {
        Preconditions.checkNotNull(provider, "provider cannot be null");
        this.provider = provider;
    }

    @IsCancelled(Tristate.FALSE)
    @Listener
    public void onPlayerCreateNPC(PlayerCreateNPCEvent event) {
        UniqueAccount account = provider.getOrCreateAccount(event.getCreator().getUniqueId()).orElse(null);
        if (event.getCreator().hasPermission("citizens.npc.ignore-cost"))
            return;
        double cost = Setting.NPC_COST.asDouble();
        TransactionResult response = account.withdraw(this.provider.getDefaultCurrency(), new BigDecimal(cost), Citizens.pluginCause);
        if (response.getResult() != ResultType.SUCCESS) {
            event.setCancelled(true);
            event.setCancelReason(response.getResult().name());
            return;
        }
        String formattedCost = Double.toString(cost);
        Messaging.sendTr(event.getCreator(), Messages.MONEY_WITHDRAWN, formattedCost);
    }
}
