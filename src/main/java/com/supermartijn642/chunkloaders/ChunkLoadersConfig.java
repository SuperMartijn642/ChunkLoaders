package com.supermartijn642.chunkloaders;

import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class ChunkLoadersConfig {

    public static final Supplier<Integer> maxLoadedChunksPerPlayer;
    public static final Supplier<Long> inactivityTimeout;
    public static final Supplier<Boolean> allowLegacyLoadedChunks;

    public static final Supplier<Integer> singleChunkLoaderRadius;
    public static final Supplier<Integer> basicChunkLoaderRadius;
    public static final Supplier<Integer> advancedChunkLoaderRadius;
    public static final Supplier<Integer> ultimateChunkLoaderRadius;
    public static final Supplier<Boolean> doRandomTicks;

    static{
        IConfigBuilder builder = ConfigBuilders.newTomlConfig("chunkloaders", null, false);
        builder.push("Limitations");
        maxLoadedChunksPerPlayer = builder.comment("How many chunks should players be able to load per world? Use -1 for infinite.").define("maxLoadedChunksPerPlayer", -1, -1, 1000);
        inactivityTimeout = builder.comment("After how many minutes of offline time should players' chunk loaders be disabled? Use -1 to disable the inactivity timeout.").define("inactivityTimeout", 7 * 24 * 60, -1, 365 * 24 * 60L);
        allowLegacyLoadedChunks = builder.comment("Chunk loaders from before version 1.2.0 are not bound to a player. Should these chunk loaders' loaded chunks stay loaded?").define("allowLegacyLoadedChunks", true);
        builder.pop();
        builder.push("General");
        singleChunkLoaderRadius = builder.comment("In what radius should the Single Chunk Loader be able to load chunks?").define("singleChunkLoaderRadius", 1, 1, 6);
        basicChunkLoaderRadius = builder.comment("In what radius should the Basic Chunk Loader be able to load chunks?").define("basicChunkLoaderRadius", 2, 1, 6);
        advancedChunkLoaderRadius = builder.comment("In what radius should the Advanced Chunk Loader be able to load chunks?").define("advancedChunkLoaderRadius", 3, 1, 6);
        ultimateChunkLoaderRadius = builder.comment("In what radius should the Ultimate Chunk Loader be able to load chunks?").define("ultimateChunkLoaderRadius", 4, 1, 6);
        doRandomTicks = builder.comment("Should chunk loaders do random ticks in loaded chunks?").define("doRandomTicks", true);
        builder.pop();
        builder.build();
    }
}
