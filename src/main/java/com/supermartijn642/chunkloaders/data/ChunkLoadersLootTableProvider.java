package com.supermartijn642.chunkloaders.data;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.*;
import net.minecraft.util.ResourceLocation;
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
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation,LootTable.Builder>>>,LootParameterSet>> getTables(){
        BlockLootTables lootTables = new BlockLootTables() {
            @Override
            protected Iterable<Block> getKnownBlocks(){
                return ForgeRegistries.BLOCKS.getEntries().stream().filter(entry -> entry.getKey().getRegistryName().getNamespace().equals("chunkloaders")).map(Map.Entry::getValue).collect(Collectors.toList());
            }

            @Override
            protected void addTables(){
                this.getKnownBlocks().forEach(this::dropSelf);
            }
        };
        return ImmutableList.of(Pair.of(() -> lootTables, LootParameterSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation,LootTable> map, ValidationTracker validationtracker){
        map.forEach((a, b) -> LootTableManager.validate(validationtracker, a, b));
    }
}
