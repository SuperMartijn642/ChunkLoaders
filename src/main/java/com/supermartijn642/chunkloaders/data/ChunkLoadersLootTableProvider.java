package com.supermartijn642.chunkloaders.data;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created 25/06/2022 by SuperMartijn642
 */
public class ChunkLoadersLootTableProvider extends LootTableProvider {

    public ChunkLoadersLootTableProvider(DataGenerator dataGenerator){
        super(dataGenerator);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation,LootTable.Builder>>>,LootContextParamSet>> getTables(){
        BlockLoot lootTables = new BlockLoot() {
            @Override
            protected Iterable<Block> getKnownBlocks(){
                return ForgeRegistries.BLOCKS.getEntries().stream().filter(entry -> entry.getKey().getRegistryName().getNamespace().equals("chunkloaders")).map(Map.Entry::getValue).collect(Collectors.toList());
            }

            @Override
            protected void addTables(){
                this.getKnownBlocks().forEach(this::dropSelf);
            }
        };
        return ImmutableList.of(Pair.of(() -> lootTables, LootContextParamSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation,LootTable> map, ValidationContext validationtracker){
        map.forEach((a, b) -> LootTables.validate(validationtracker, a, b));
    }
}
