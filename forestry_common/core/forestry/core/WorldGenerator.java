/*******************************************************************************
 * Copyright 2011-2014 by SirSengir
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 ******************************************************************************/
package forestry.core;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import cpw.mods.fml.common.IWorldGenerator;

import forestry.api.core.IPlugin;
import forestry.core.config.Config;
import forestry.core.config.ForestryBlock;
import forestry.core.proxy.Proxies;
import forestry.core.worldgen.WorldGenMinableMeta;
import forestry.plugins.NativePlugin;
import forestry.plugins.PluginManager;

public class WorldGenerator implements IWorldGenerator {

	private WorldGenMinableMeta apatiteGenerator;
	private WorldGenMinableMeta copperGenerator;
	private WorldGenMinableMeta tinGenerator;

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {

		if (apatiteGenerator == null) {
			apatiteGenerator = new WorldGenMinableMeta(ForestryBlock.resources, 0, 36);
			copperGenerator = new WorldGenMinableMeta(ForestryBlock.resources, 1, 6);
			tinGenerator = new WorldGenMinableMeta(ForestryBlock.resources, 2, 6);
		}

		// shift to world coordinates
		chunkX = chunkX << 4;
		chunkZ = chunkZ << 4;

		// / APATITE
		if (Config.generateApatiteOre)
			if (random.nextFloat() < 0.8f) {
				int randPosX = chunkX + random.nextInt(16);
				int randPosY = random.nextInt(world.getActualHeight() - 72) + 56; // Does not generate below y = 64
				int randPosZ = chunkZ + random.nextInt(16);
				if (apatiteGenerator.generate(world, random, randPosX, randPosY, randPosZ))
					Proxies.log.finest("Generated apatite vein around %s/%s/%s", randPosX, randPosY, randPosZ);
			}

		// / COPPER
		if (Config.generateCopperOre)
			for (int i = 0; i < 20; i++) {
				int randPosX = chunkX + random.nextInt(16);
				int randPosY = random.nextInt(76) + 32;
				int randPosZ = chunkZ + random.nextInt(16);
				copperGenerator.generate(world, random, randPosX, randPosY, randPosZ);
			}

		// / TIN
		if (Config.generateTinOre)
			for (int i = 0; i < 18; i++) {
				int randPosX = chunkX + random.nextInt(16);
				int randPosY = random.nextInt(76) + 16;
				int randPosZ = chunkZ + random.nextInt(16);
				tinGenerator.generate(world, random, randPosX, randPosY, randPosZ);
			}

		// / PLUGIN WORLD GENERATION
		for (IPlugin plugin : PluginManager.plugins)
			if (plugin.isAvailable() && plugin instanceof NativePlugin)
				((NativePlugin) plugin).generateSurface(world, random, chunkX, chunkZ);
	}

}
