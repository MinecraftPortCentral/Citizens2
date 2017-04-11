package net.citizensnpcs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.CommandManager.CommandInfo;
import net.citizensnpcs.api.command.Injector;
import net.citizensnpcs.api.event.CitizensDisableEvent;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.event.CitizensPreReloadEvent;
import net.citizensnpcs.api.event.CitizensReloadEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.npc.SimpleNPCDataStore;
import net.citizensnpcs.api.scripting.EventRegistrar;
import net.citizensnpcs.api.scripting.ObjectProvider;
import net.citizensnpcs.api.scripting.ScriptCompiler;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.api.util.NBTStorage;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.Translator;
import net.citizensnpcs.api.util.YamlStorage;
import net.citizensnpcs.commands.AdminCommands;
import net.citizensnpcs.commands.EditorCommands;
import net.citizensnpcs.commands.NPCCommands;
import net.citizensnpcs.commands.TemplateCommands;
import net.citizensnpcs.commands.TraitCommands;
import net.citizensnpcs.commands.WaypointCommands;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.CitizensNPCRegistry;
import net.citizensnpcs.npc.CitizensTraitFactory;
import net.citizensnpcs.npc.NPCSelector;
import net.citizensnpcs.npc.ai.speech.Chat;
import net.citizensnpcs.npc.ai.speech.CitizensSpeechFactory;
import net.citizensnpcs.npc.profile.ProfileFetcher;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.PlayerUpdateTask;
import net.citizensnpcs.util.StringHelper;
import net.citizensnpcs.util.Util;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "citizens", name = "Citizens", version = "1.0.0", description = "This plugin is designed to add NPC's to the world.")
public class Citizens implements CitizensPlugin {

    public static Citizens instance;
    @Inject public PluginContainer pluginContainer;
    @Inject private Logger logger;
    @Inject @ConfigDir(sharedRoot = false)
    private Path configPath;
    private final File configFile = configPath.toFile();
    public static Cause pluginCause;

    private final CommandManager commands = new CommandManager();
    private boolean compatible;
    private Settings config;
    private CitizensNPCRegistry npcRegistry;
    private NPCDataStore saves;
    private NPCSelector selector;
    private CitizensSpeechFactory speechFactory;
    private final Map<String, NPCRegistry> storedRegistries = Maps.newHashMap();
    private CitizensTraitFactory traitFactory;

    @Override
    public NPCRegistry createAnonymousNPCRegistry(NPCDataStore store) {
        return new CitizensNPCRegistry(store);
    }

    @Override
    public NPCRegistry createNamedNPCRegistry(String name, NPCDataStore store) {
        NPCRegistry created = new CitizensNPCRegistry(store);
        storedRegistries.put(name, created);
        return created;
    }

    private NPCDataStore createStorage(File folder) {
        Storage saves = null;
        String type = Setting.STORAGE_TYPE.asString();
        if (type.equalsIgnoreCase("nbt")) {
            saves = new NBTStorage(new File(folder + File.separator + Setting.STORAGE_FILE.asString()),
                    "Citizens NPC Storage");
        }
        if (saves == null) {
            saves = new YamlStorage(new File(folder, Setting.STORAGE_FILE.asString()), "Citizens NPC Storage");
        }
        if (!saves.load())
            return null;
        return SimpleNPCDataStore.create(saves);
    }

    private void despawnNPCs() {
        Iterator<NPC> itr = npcRegistry.iterator();
        while (itr.hasNext()) {
            NPC npc = itr.next();
            try {
                npc.despawn(DespawnReason.RELOAD);
            } catch (Throwable e) {
                e.printStackTrace();
                // ensure that all entities are despawned
            }
            itr.remove();
        }
    }

    /*private void enableSubPlugins() {
        File root = new File(this.configPath.toFile(), Setting.SUBPLUGIN_FOLDER.asString());
        if (!root.exists() || !root.isDirectory())
            return;
        File[] files = root.listFiles();
        for (File file : files) {
            Plugin plugin;
            try {
                plugin = Bukkit.getPluginManager().loadPlugin(file);
            } catch (Exception e) {
                continue;
            }
            if (plugin == null)
                continue;
            // code beneath modified from CraftServer
            try {
                Messaging.logTr(Messages.LOADING_SUB_PLUGIN, plugin.getDescription().getFullName());
                plugin.onLoad();
            } catch (Throwable ex) {
                Messaging.severeTr(Messages.ERROR_INITALISING_SUB_PLUGIN, ex.getMessage(),
                        plugin.getDescription().getFullName());
                ex.printStackTrace();
            }
        }
        NMS.loadPlugins();
    }*/

    public CommandInfo getCommandInfo(String rootCommand, String modifier) {
        return commands.getCommand(rootCommand, modifier);
    }

    public Iterable<CommandInfo> getCommands(String base) {
        return commands.getCommands(base);
    }

    @Override
    public net.citizensnpcs.api.npc.NPCSelector getDefaultNPCSelector() {
        return selector;
    }

    @Override
    public NPCRegistry getNamedNPCRegistry(String name) {
        return storedRegistries.get(name);
    }

    @Override
    public Iterable<NPCRegistry> getNPCRegistries() {
        return new Iterable<NPCRegistry>() {
            @Override
            public Iterator<NPCRegistry> iterator() {
                return new Iterator<NPCRegistry>() {
                    Iterator<NPCRegistry> stored;

                    @Override
                    public boolean hasNext() {
                        return stored == null ? true : stored.hasNext();
                    }

                    @Override
                    public NPCRegistry next() {
                        if (stored == null) {
                            stored = storedRegistries.values().iterator();
                            return npcRegistry;
                        }
                        return stored.next();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    @Override
    public NPCRegistry getNPCRegistry() {
        return npcRegistry;
    }

    public NPCSelector getNPCSelector() {
        return selector;
    }

    @Override
    public File getScriptFolder() {
        return new File(this.configPath.toFile(), "scripts");
    }

    @Override
    public SpeechFactory getSpeechFactory() {
        return speechFactory;
    }

    @Override
    public TraitFactory getTraitFactory() {
        return traitFactory;
    }

    @Override
    public boolean onCommand(CommandSource sender, org.bukkit.command.Command command, String cmdName, String[] args) {
        String modifier = args.length > 0 ? args[0] : "";
        if (!commands.hasCommand(command, modifier) && !modifier.isEmpty()) {
            return suggestClosestModifier(sender, command.getName(), modifier);
        }

        NPC npc = selector == null ? null : selector.getSelected(sender);
        // TODO: change the args supplied to a context style system for
        // flexibility (ie. adding more context in the future without
        // changing everything)

        Object[] methodArgs = { sender, npc };
        return commands.executeSafe(command, args, sender, methodArgs);
    }

    @Listener(order = Order.LAST)
    public void onPreInit(GamePreInitializationEvent event) {
        instance = this;
        pluginCause = Cause.of(NamedCause.source(this.pluginContainer));
        setupTranslator();
        CitizensAPI.setImplementation(this);
        config = new Settings(this.configPath.toFile());
        // Disable if the server is not using the compatible Minecraft version
        String mcVersion = Util.getMinecraftRevision();
        compatible = true;
        try {
            NMS.loadBridge(mcVersion);
        } catch (Exception e) {
            compatible = false;
            if (Messaging.isDebugging()) {
                e.printStackTrace();
            }
            Messaging.severeTr(Messages.CITIZENS_INCOMPATIBLE, this.pluginContainer.getVersion().get(), mcVersion);
            return;
        }
        registerScriptHelpers();

        saves = createStorage(this.configFile);
        if (saves == null) {
            Messaging.severeTr(Messages.FAILED_LOAD_SAVES);
            //getServer().getPluginManager().disablePlugin(this);
            return;
        }

        npcRegistry = new CitizensNPCRegistry(saves);
        traitFactory = new CitizensTraitFactory();
        selector = new NPCSelector(pluginContainer);
        speechFactory = new CitizensSpeechFactory();
        speechFactory.register(Chat.class, "chat");

        Sponge.getEventManager().registerListeners(this.pluginContainer, new EventListen(storedRegistries));

        if (Setting.NPC_COST.asDouble() > 0) {
            setupEconomy();
        }

        registerCommands();
        //enableSubPlugins();
        NMS.load(commands);

        // Setup NPCs after all plugins have been enabled (allows for multiworld
        // support and for NPCs to properly register external settings)
        /*if (getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                saves.loadInto(npcRegistry);
                Messaging.logTr(Messages.NUM_LOADED_NOTIFICATION, Iterables.size(npcRegistry), "?");
                startMetrics();
                scheduleSaveTask(Setting.SAVE_TASK_DELAY.asInt());
                Bukkit.getPluginManager().callEvent(new CitizensEnableEvent());
                new PlayerUpdateTask().runTaskTimer(Citizens.this, 0, 1);
            }
        }, 1) == -1) {
            Messaging.severeTr(Messages.LOAD_TASK_NOT_SCHEDULED);
            //getServer().getPluginManager().disablePlugin(this);
        }*/
        Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(Setting.SAVE_TASK_DELAY.asInt()).execute(new PlayerUpdateTask()).submit(this.pluginContainer);
    }

    public void registerCommandClass(Class<?> clazz) {
        try {
            commands.register(clazz);
        } catch (Throwable ex) {
            Messaging.logTr(Messages.CITIZENS_INVALID_COMMAND_CLASS);
            ex.printStackTrace();
        }
    }

    private void registerCommands() {
        commands.setInjector(new Injector(this));
        // Register command classes
        commands.register(AdminCommands.class);
        commands.register(EditorCommands.class);
        commands.register(NPCCommands.class);
        commands.register(TemplateCommands.class);
        commands.register(TraitCommands.class);
        commands.register(WaypointCommands.class);
    }

    private void registerScriptHelpers() {
        ScriptCompiler compiler = CitizensAPI.getScriptCompiler();
        compiler.registerGlobalContextProvider(new EventRegistrar(pluginContainer));
        compiler.registerGlobalContextProvider(new ObjectProvider("plugin", this));
    }

    public void reload() throws NPCLoadException {
        Editor.leaveAll();
        config.reload();
        despawnNPCs();
        ProfileFetcher.reset();
        Skin.clearCache();
        Sponge.getEventManager().post(new CitizensPreReloadEvent());

        saves = createStorage(getDataFolder());
        saves.loadInto(npcRegistry);

        Sponge.getEventManager().post(new CitizensReloadEvent());
    }

    @Override
    public void removeNamedNPCRegistry(String name) {
        storedRegistries.remove(name);
    }

    private void scheduleSaveTask(int delay) {
        Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(delay).execute(new Runnable() {
            @Override
            public void run() {
                storeNPCs();
                saves.saveToDisk();
            }
        }).submit(this);
    }

    private void setupEconomy() {
        try {
            ProviderRegistration<EconomyService> provider = Sponge.getServiceManager().getRegistration(EconomyService.class).orElse(null);
            if (provider != null && provider.getProvider() != null) {
                EconomyService economy = provider.getProvider();
                Sponge.getEventManager().registerListeners(this.pluginContainer, new PaymentListener(economy));
            }
        } catch (NoClassDefFoundError e) {
            Messaging.logTr(Messages.ERROR_LOADING_ECONOMY);
        }
    }

    private void setupTranslator() {
        Locale locale = Locale.getDefault();
        String setting = Setting.LOCALE.asString();
        if (!setting.isEmpty()) {
            String[] parts = setting.split("[\\._]");
            switch (parts.length) {
                case 1:
                    locale = new Locale(parts[0]);
                    break;
                case 2:
                    locale = new Locale(parts[0], parts[1]);
                    break;
                case 3:
                    locale = new Locale(parts[0], parts[1], parts[2]);
                    break;
                default:
                    break;
            }
        }
        Translator.setInstance(new File(getDataFolder(), "lang"), locale);
    }

    public void storeNPCs() {
        if (saves == null)
            return;
        for (NPC npc : npcRegistry) {
            saves.store(npc);
        }
    }

    public void storeNPCs(CommandContext args) {
        storeNPCs();
        boolean async = args.hasFlag('a');
        if (async) {
            saves.saveToDisk();
        } else {
            saves.saveToDiskImmediate();
        }
    }

    private boolean suggestClosestModifier(CommandSource sender, String command, String modifier) {
        String closest = commands.getClosestCommandModifier(command, modifier);
        if (!closest.isEmpty()) {
            sender.sendMessage(Text.of(TextColors.GRAY + Messaging.tr(Messages.UNKNOWN_COMMAND)));
            sender.sendMessage(Text.of(StringHelper.wrap(" /") + command + " " + StringHelper.wrap(closest)));
            return true;
        }
        return false;
    }

    @Override
    public PluginContainer getPlugin() {
        return this.pluginContainer;
    }
}
