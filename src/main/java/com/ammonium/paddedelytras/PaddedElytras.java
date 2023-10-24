package com.ammonium.paddedelytras;

import com.ammonium.paddedelytras.ench.PaddedEnchantment;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.BasicItemListing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PaddedElytras.MODID)
public class PaddedElytras
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "paddedelytras";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final DeferredRegister<Enchantment> ENCHS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MODID);

    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
//    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of(Material.STONE)));
//    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
//    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));

    public static final RegistryObject<Enchantment> PADDED = ENCHS.register("padded", PaddedEnchantment::new);

    public PaddedElytras()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register enchantments
        ENCHS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
//        LOGGER.info("HELLO FROM COMMON SETUP");
//        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
//        LOGGER.info("HELLO from server starting");
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        // Check if the entity is a player
        if(event.getEntity() instanceof Player player) {
            if (!event.getSource().equals(DamageSource.FLY_INTO_WALL)) {
                return;
            }

            // Check if the player is wearing an Elytra in the chest slot
            ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
            if(chestStack.getItem() == Items.ELYTRA) {
                // Check if the Elytra has the custom enchantment
                float originalDamage = event.getAmount();
                switch (EnchantmentHelper.getItemEnchantmentLevel(PADDED.get(), chestStack)) {
                    case 1 -> event.setAmount(originalDamage * 0.67F);
                    case 2 -> event.setAmount(originalDamage * 0.34F);
                    case 3 -> event.setCanceled(true);
                    default -> {
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() == VillagerProfession.LIBRARIAN) {
            ItemStack book1 = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(ImmutableMap.of(new PaddedEnchantment(), 1), book1);
            event.getTrades().get(1).add(new BasicItemListing(15, book1, 12, 10));
            ItemStack book2 = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(ImmutableMap.of(new PaddedEnchantment(), 2), book2);
            event.getTrades().get(1).add(new BasicItemListing(30, book2, 12, 10));
            ItemStack book3 = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(ImmutableMap.of(new PaddedEnchantment(), 3), book3);
            event.getTrades().get(1).add(new BasicItemListing(45, book3, 12, 10));
        }
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
//            LOGGER.info("HELLO FROM CLIENT SETUP");
//            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
