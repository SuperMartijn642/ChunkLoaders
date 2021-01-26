package com.supermartijn642.chunkloaders;

import com.supermartijn642.configlib.ModConfigBuilder;

import java.util.function.Supplier;

/**
 * Created 1/26/2021 by SuperMartijn642
 */
public class ChunkLoadersConfig {

    public static final Supplier<Integer> singleChunkLoaderRadius;
    public static final Supplier<Integer> basicChunkLoaderRadius;
    public static final Supplier<Integer> advancedChunkLoaderRadius;
    public static final Supplier<Integer> ultimateChunkLoaderRadius;

    static{
        ModConfigBuilder builder = new ModConfigBuilder("chunkloaders");
        builder.push("General");
        singleChunkLoaderRadius = builder.comment("In what radius should the Single Chunk Loader be able to load chunks?").define("singleChunkLoaderRadius", 1, 1, 8);
        basicChunkLoaderRadius = builder.comment("In what radius should the Basic Chunk Loader be able to load chunks?").define("basicChunkLoaderRadius", 2, 1, 8);
        advancedChunkLoaderRadius = builder.comment("In what radius should the Advanced Chunk Loader be able to load chunks?").define("advancedChunkLoaderRadius", 3, 1, 8);
        ultimateChunkLoaderRadius = builder.comment("In what radius should the Ultimate Chunk Loader be able to load chunks?").define("ultimateChunkLoaderRadius", 4, 1, 8);
        builder.pop();
        builder.build();
    }

}
