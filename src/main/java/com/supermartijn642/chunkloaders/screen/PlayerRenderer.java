package com.supermartijn642.chunkloaders.screen;

import com.mojang.authlib.GameProfile;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.UUID;

/**
 * Created 5/20/2021 by SuperMartijn642
 */
public class PlayerRenderer {

    public static void renderPlayerHead(UUID player, GuiGraphicsHelper graphics, int x, int y, int width, int height){
        graphics.submitTexture(getPlayerSkin(player), x, y, width, height, p -> p.uv(1 / 8f, 1 / 8f, 1 / 8f, 1 / 8f));
    }

    public static String getPlayerUsername(UUID player){
        return getGameProfile(player).map(GameProfile::name).orElse(null);
    }

    public static Identifier getPlayerSkin(UUID player){
        return getGameProfile(player)
            .map(profile -> ClientUtils.getMinecraft().getSkinManager().get(profile))
            .flatMap(future -> future.getNow(Optional.empty()))
            .map(skin -> skin.body().texturePath())
            .orElseGet(() -> DefaultPlayerSkin.get(player).body().texturePath());
    }

    private static Optional<GameProfile> getGameProfile(UUID player){
        return ClientUtils.getMinecraft().services().profileResolver().fetchById(player);
    }
}
