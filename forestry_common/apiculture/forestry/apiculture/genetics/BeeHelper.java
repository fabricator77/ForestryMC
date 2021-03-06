/*******************************************************************************
 * Copyright 2011-2014 by SirSengir
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 ******************************************************************************/
package forestry.apiculture.genetics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;

import forestry.api.apiculture.EnumBeeChromosome;
import forestry.api.apiculture.EnumBeeType;
import forestry.api.apiculture.IAlleleBeeSpecies;
import forestry.api.apiculture.IAlvearyComponent;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeMutation;
import forestry.api.apiculture.IBeeRoot;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.apiculture.IBeekeepingMode;
import forestry.api.core.IStructureLogic;
import forestry.api.genetics.AlleleManager;
import forestry.api.genetics.IAllele;
import forestry.api.genetics.IChromosomeType;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.IMutation;
import forestry.apiculture.BeekeepingLogic;
import forestry.apiculture.gadgets.StructureLogicAlveary;
import forestry.core.config.ForestryItem;
import forestry.core.genetics.SpeciesRoot;
import forestry.plugins.PluginApiculture;

public class BeeHelper extends SpeciesRoot implements IBeeRoot {

	public static int beeSpeciesCount = -1;
	public static final String UID = "rootBees";

	@Override
	public String getUID() {
		return UID;
	}

	@Override
	public Class<? extends IIndividual> getMemberClass() {
		return IBee.class;
	}

	@Override
	public int getSpeciesCount() {
		if (beeSpeciesCount < 0) {
			beeSpeciesCount = 0;
			Iterator<Entry<String, IAllele> > it = AlleleManager.alleleRegistry.getRegisteredAlleles().entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, IAllele> entry = it.next();
				if (entry.getValue() instanceof IAlleleBeeSpecies)
					if (((IAlleleBeeSpecies) entry.getValue()).isCounted())
						beeSpeciesCount++;
			}
		}

		return beeSpeciesCount;
	}

	@Override
	public boolean isMember(ItemStack stack) {
		return getType(stack) != EnumBeeType.NONE;
	}

	@Override
	public boolean isMember(ItemStack stack, int type) {
		return getType(stack).ordinal() == type;
	}

	@Override
	public boolean isMember(IIndividual individual) {
		return individual instanceof IBee;
	}

	@Override
	public ItemStack getMemberStack(IIndividual bee, int type) {
		if (!isMember(bee))
			return null;

		Item beeItem = null;
		switch (EnumBeeType.VALUES[type]) {
		case QUEEN:
			beeItem = ForestryItem.beeQueenGE.item();
			break;
		case PRINCESS:
			beeItem = ForestryItem.beePrincessGE.item();
			break;
		case DRONE:
			beeItem = ForestryItem.beeDroneGE.item();
			break;
		case LARVAE:
			beeItem = ForestryItem.beeLarvaeGE.item();
			break;
		default:
			throw new RuntimeException("Cannot instantiate a bee of type " + type);

		}

		NBTTagCompound nbttagcompound = new NBTTagCompound();
		bee.writeToNBT(nbttagcompound);
		ItemStack beeStack = new ItemStack(beeItem);
		beeStack.setTagCompound(nbttagcompound);
		return beeStack;
	}

	@Override
	public EnumBeeType getType(ItemStack stack) {
		if (stack == null)
			return EnumBeeType.NONE;

		if (ForestryItem.beeDroneGE.isItemEqual(stack))
			return EnumBeeType.DRONE;
		else if (ForestryItem.beePrincessGE.isItemEqual(stack))
			return EnumBeeType.PRINCESS;
		else if (ForestryItem.beeQueenGE.isItemEqual(stack))
			return EnumBeeType.QUEEN;
		else if (ForestryItem.beeLarvaeGE.isItemEqual(stack))
			return EnumBeeType.LARVAE;

		return EnumBeeType.NONE;
	}

	@Override
	public boolean isDrone(ItemStack stack) {
		return getType(stack) == EnumBeeType.DRONE;
	}

	@Override
	public boolean isMated(ItemStack stack) {
		if (getType(stack) != EnumBeeType.QUEEN)
			return false;

		IBee bee = getMember(stack);
		return bee.getMate() != null;
	}

	@Override
	public IBee getMember(ItemStack stack) {
		if (!ForestryItem.beeQueenGE.isItemEqual(stack) && !ForestryItem.beePrincessGE.isItemEqual(stack)
				&& !ForestryItem.beeDroneGE.isItemEqual(stack) && !ForestryItem.beeLarvaeGE.isItemEqual(stack))
			return null;

		return new Bee(stack.getTagCompound());
	}

	@Override
	public IBee getMember(NBTTagCompound compound) {
		return new Bee(compound);
	}

	@Override
	public IBee getBee(World world, IBeeGenome genome) {
		return new Bee(world, genome);
	}

	@Override
	public IBee getBee(World world, IBeeGenome genome, IBee mate) {
		return new Bee(world, genome, mate);
	}

	/* GENOME CONVERSIONS */
	@Override
	public IBeeGenome templateAsGenome(IAllele[] template) {
		return new BeeGenome(templateAsChromosomes(template));
	}

	@Override
	public IBeeGenome templateAsGenome(IAllele[] templateActive, IAllele[] templateInactive) {
		return new BeeGenome(templateAsChromosomes(templateActive, templateInactive));
	}

	@Override
	public IBee templateAsIndividual(IAllele[] template) {
		return new Bee(templateAsGenome(template));
	}

	@Override
	public IBee templateAsIndividual(IAllele[] templateActive, IAllele[] templateInactive) {
		return new Bee(templateAsGenome(templateActive, templateInactive));
	}

	/* TEMPLATES */
	public static ArrayList<IBee> beeTemplates = new ArrayList<IBee>();

	@Override
	public ArrayList<IBee> getIndividualTemplates() {
		return beeTemplates;
	}

	@Override
	public void registerTemplate(String identifier, IAllele[] template) {
		beeTemplates.add(new Bee(PluginApiculture.beeInterface.templateAsGenome(template)));
		speciesTemplates.put(identifier, template);
	}

	@Override
	public IAllele[] getDefaultTemplate() {
		return BeeTemplates.getDefaultTemplate();
	}

	/* MUTATIONS */
	/**
	 * List of possible mutations on species alleles.
	 */
	private static ArrayList<IBeeMutation> beeMutations = new ArrayList<IBeeMutation>();

	@Override
	public Collection<IBeeMutation> getMutations(boolean shuffle) {
		if (shuffle)
			Collections.shuffle(beeMutations);
		return beeMutations;
	}

	@Override
	public void registerMutation(IMutation mutation) {
		if (AlleleManager.alleleRegistry.isBlacklisted(mutation.getTemplate()[0].getUID()))
			return;
		if (AlleleManager.alleleRegistry.isBlacklisted(mutation.getAllele0().getUID()))
			return;
		if (AlleleManager.alleleRegistry.isBlacklisted(mutation.getAllele1().getUID()))
			return;

		beeMutations.add((IBeeMutation) mutation);
	}

	/* BREEDING MODES */
	ArrayList<IBeekeepingMode> beekeepingModes = new ArrayList<IBeekeepingMode>();
	public static IBeekeepingMode activeBeekeepingMode;

	@Override
	public void resetBeekeepingMode() {
		activeBeekeepingMode = null;
	}

	@Override
	public ArrayList<IBeekeepingMode> getBeekeepingModes() {
		return this.beekeepingModes;
	}

	@Override
	public IBeekeepingMode getBeekeepingMode(World world) {
		if (activeBeekeepingMode != null)
			return activeBeekeepingMode;

		// No beekeeping mode yet, get it.
		IApiaristTracker tracker = getBreedingTracker(world, "__COMMON_");
		String mode = tracker.getModeName();
		if (mode == null || mode.isEmpty())
			mode = PluginApiculture.beekeepingMode;

		setBeekeepingMode(world, mode);
		FMLCommonHandler.instance().getFMLLogger().debug("Set beekeeping mode for a world to " + mode);

		return activeBeekeepingMode;
	}

	@Override
	public void registerBeekeepingMode(IBeekeepingMode mode) {
		beekeepingModes.add(mode);
	}

	@Override
	public void setBeekeepingMode(World world, String name) {
		activeBeekeepingMode = getBeekeepingMode(name);
		getBreedingTracker(world, "__COMMON_").setModeName(name);
	}

	@Override
	public IBeekeepingMode getBeekeepingMode(String name) {
		for (IBeekeepingMode mode : beekeepingModes) {
			if (mode.getName().equals(name) || mode.getName().equals(name.toLowerCase(Locale.ENGLISH)))
				return mode;
		}

		FMLCommonHandler.instance().getFMLLogger().debug("Failed to find a beekeeping mode called '%s', reverting to fallback.");
		return beekeepingModes.get(0);
	}

	@Override
	public IApiaristTracker getBreedingTracker(World world, String player) {
		String filename = "ApiaristTracker." + player;
		ApiaristTracker tracker = (ApiaristTracker) world.loadItemData(ApiaristTracker.class, filename);

		// Create a tracker if there is none yet.
		if (tracker == null) {
			tracker = new ApiaristTracker(filename, player);
			world.setItemData(filename, tracker);
		}

		return tracker;
	}

	@Override
	public IBeekeepingLogic createBeekeepingLogic(IBeeHousing housing) {
		return new BeekeepingLogic(housing);
	}

	@Override
	public IStructureLogic createAlvearyStructureLogic(IAlvearyComponent structure) {
		return new StructureLogicAlveary(structure);
	}

	@Override
	public IChromosomeType[] getKaryotype() {
		return EnumBeeChromosome.values();
	}

	@Override
	public IChromosomeType getKaryotypeKey() {
		return EnumBeeChromosome.SPECIES;
	}
}
