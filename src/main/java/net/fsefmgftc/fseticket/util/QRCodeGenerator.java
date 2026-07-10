package net.fsefmgftc.fseticket.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.platform.NativeImage;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

public class QRCodeGenerator {
	private static final Map<String, ResourceLocation> cache = new ConcurrentHashMap<>();

	public static ResourceLocation getOrGenerate(String url, int size) {
		if (FMLEnvironment.dist != Dist.CLIENT) return null;
		String key = url + "_" + size;
		ResourceLocation existing = cache.get(key);
		if (existing != null) return existing;

		NativeImage blank = new NativeImage(size, size, false);
		for (int x = 0; x < size; x++) for (int y = 0; y < size; y++) blank.setPixelRGBA(x, y, 0x00000000);
		DynamicTexture dt = new DynamicTexture(blank);
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath("fseticket", "qr_" + Math.abs(key.hashCode()));
		Minecraft.getInstance().getTextureManager().register(id, dt);
		dt.upload();
		cache.put(key, id);

		CompletableFuture.runAsync(() -> {
			try {
				String api = "https://api.qrserver.com/v1/create-qr-code/?size=" + size + "x" + size + "&data=" + url;
				BufferedImage img = ImageIO.read(new URL(api));
				NativeImage ni = new NativeImage(size, size, false);
				for (int x = 0; x < size; x++) for (int y = 0; y < size; y++) {
					int rgb = img.getRGB(x, y); int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
					ni.setPixelRGBA(x, y, (r > 200 && g > 200 && b > 200) ? 0x00000000 : 0xFF000000);
				}
				Minecraft.getInstance().execute(() -> { DynamicTexture tex = new DynamicTexture(ni); Minecraft.getInstance().getTextureManager().register(id, tex); tex.upload(); });
			} catch (Exception ignored) {}
		});
		return id;
	}
}
