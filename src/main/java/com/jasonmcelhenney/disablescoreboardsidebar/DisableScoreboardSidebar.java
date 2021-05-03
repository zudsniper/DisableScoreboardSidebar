package com.jasonmcelhenney.disablescoreboardsidebar;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml fileA
@Mod("disablescoreboardsidebar") // must be lowercase...
public class DisableScoreboardSidebar {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String COMMAND_PREFIX = "$";

    // Private instance vars
    private static ScoreObjective objectiveInSidebar = null;

    public DisableScoreboardSidebar() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        //add clientChatListener to MinecraftForge event bus
        MinecraftForge.EVENT_BUS.addListener(this::chatMessageHandler);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
        //event.getMinecraftSupplier().get().world.getScoreboard().setObjectiveInDisplaySlot(0, null);
    }

    private void chatMessageHandler(final ClientChatEvent chatEvent) {
        String msg = chatEvent.getMessage();
        if(msg.startsWith(COMMAND_PREFIX)) {
            chatEvent.setCanceled(true); //don't send in chat
            String commandStr = msg.substring(COMMAND_PREFIX.length());
            ClientPlayerEntity player = Minecraft.getInstance().player;
            ClientWorld world = Minecraft.getInstance().world;
            TextComponent component = new StringTextComponent(String.format("> %s :", msg));
            component.setStyle(component.getStyle()
                    .setFormatting(TextFormatting.ITALIC)
                    .func_244282_c(true)
            );
            player.sendMessage(component, player.getUniqueID()); //repeat the executed command

            if("disableSidebar".equals(commandStr)) {
                disableSidebar();
                player.sendMessage(new StringTextComponent("> success"), player.getUniqueID());
            } else if("enableSidebar".equals(commandStr)) {
                enableSidebar();
                player.sendMessage(new StringTextComponent("> success"), player.getUniqueID());
            } else {
                player.sendMessage(new StringTextComponent("> unrecognized command"), player.getUniqueID());
            }

        }
    }

    private boolean disableSidebar() {
        ClientWorld world = Minecraft.getInstance().world;
        if (world != null) {
            //save currently displayed sidebar scoreboard objective. May be null
            objectiveInSidebar = world.getScoreboard().getObjectiveInDisplaySlot(1); //1 is sidebar!!
            world.getScoreboard().setObjectiveInDisplaySlot(1, null); //clear scoreboard
        }
        return true; //no return value for world, gotta assume it worked
    }

    private boolean enableSidebar() {
        ClientWorld world = Minecraft.getInstance().world;
        if((objectiveInSidebar != null) && (world != null)) {
          world.getScoreboard().setObjectiveInDisplaySlot(1, objectiveInSidebar);
          return true;
        }
        return false;
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("disableScoreboardSidebar", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
