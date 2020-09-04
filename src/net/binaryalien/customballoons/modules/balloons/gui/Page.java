package net.binaryalien.customballoons.modules.balloons.gui;

import org.bukkit.inventory.Inventory;

public class Page
{
	public final int index;
	public final Inventory inv;

	public Page(int index, Inventory inv)
	{
		this.index = index;
		this.inv = inv;
	}
}
