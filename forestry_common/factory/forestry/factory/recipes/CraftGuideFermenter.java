/*******************************************************************************
 * Copyright 2011-2014 by SirSengir
 * 
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 ******************************************************************************/
package forestry.factory.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import uristqwerty.CraftGuide.api.ItemSlot;
import uristqwerty.CraftGuide.api.LiquidSlot;
import uristqwerty.CraftGuide.api.RecipeGenerator;
import uristqwerty.CraftGuide.api.RecipeProvider;
import uristqwerty.CraftGuide.api.RecipeTemplate;
import uristqwerty.CraftGuide.api.Slot;
import uristqwerty.CraftGuide.api.SlotType;

import forestry.api.fuels.FuelManager;
import forestry.core.config.Defaults;
import forestry.core.config.ForestryBlock;
import forestry.factory.gadgets.MachineFermenter;

public class CraftGuideFermenter implements RecipeProvider {

	private final Slot[] slots = new Slot[5];

	public CraftGuideFermenter() {
		slots[0] = new ItemSlot(3, 12, 16, 16, true);
		slots[1] = new ItemSlot(3, 30, 16, 16, true);
		slots[2] = new LiquidSlot(21, 21);
		slots[3] = new LiquidSlot(59, 21).setSlotType(SlotType.OUTPUT_SLOT);
		slots[4] = new ItemSlot(40, 21, 16, 16).setSlotType(SlotType.MACHINE_SLOT);
	}

	@Override
	public void generateRecipes(RecipeGenerator generator) {

		if (ForestryBlock.factoryTESR == null)
			return;

		ItemStack machine = new ItemStack(ForestryBlock.factoryTESR, 1, Defaults.DEFINITION_FERMENTER_META);
		RecipeTemplate template = generator.createRecipeTemplate(slots, machine);
		List<Object> fuels = new ArrayList<Object>(FuelManager.fermenterFuel.keySet());

		for (MachineFermenter.Recipe recipe : MachineFermenter.RecipeManager.recipes) {
			Object[] array = new Object[5];

			array[0] = recipe.resource;
			array[1] = fuels;
			array[2] = recipe.liquid;
			FluidStack output = recipe.output.copy();
			output.amount *= recipe.fermentationValue;
			output.amount *= recipe.modifier;
			array[3] = output;
			array[4] = machine;
			generator.addRecipe(template, array);
		}
	}
}
