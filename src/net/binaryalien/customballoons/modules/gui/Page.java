package net.binaryalien.customballoons.modules.gui;

import java.util.List;

import org.bukkit.inventory.Inventory;

import net.binaryalien.customballoons.modules.balloons.BalloonBlueprint;

public class Page
{
	private int index;
	private List<BalloonBlueprint> balloons;
	private Inventory inv;

	public Page(int index, List<BalloonBlueprint> balloons, Inventory inv)
	{
		this.index = index;
		this.balloons = balloons;
		this.inv = inv;
	}

	public int getIndex()
	{
		return index;
	}

	public BalloonBlueprint getBalloon(int index)
	{
		return balloons.get(index);
	}

	public int getBalloons()
	{
		return balloons.size();
	}

	public Inventory getInventory()
	{
		return inv;
	}
}
