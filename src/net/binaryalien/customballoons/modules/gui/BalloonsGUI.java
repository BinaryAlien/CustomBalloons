package net.binaryalien.customballoons.modules.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.binaryalien.customballoons.CustomBalloons;
import net.binaryalien.customballoons.modules.LanguageModule;
import net.binaryalien.customballoons.modules.Module;
import net.binaryalien.customballoons.modules.balloons.BalloonBlueprint;

public class BalloonsGUI implements Module, Listener
{
	private static final int BALLOONS_PER_PAGE = 54 - 9; // 9 slots reserved for actions

	private Map<Player, Page> pages;

	private ItemStack previous;
	private ItemStack next;
	private ItemStack remove;

	public BalloonsGUI()
	{
		pages = new HashMap<>();

		ItemMeta temp;

		next = new ItemStack(Material.EMERALD_BLOCK);
		temp = next.getItemMeta();
		temp.setDisplayName(LanguageModule.Strings.GUI_NEXT.toString());
		next.setItemMeta(temp);

		previous = new ItemStack(Material.REDSTONE_BLOCK);
		temp = previous.getItemMeta();
		temp.setDisplayName(LanguageModule.Strings.GUI_PREVIOUS.toString());
		previous.setItemMeta(temp);

		remove = new ItemStack(Material.BARRIER);
		temp = remove.getItemMeta();
		temp.setDisplayName(LanguageModule.Strings.GUI_REMOVE.toString());
		remove.setItemMeta(temp);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if (event.isCancelled() || !(event.getWhoClicked() instanceof Player))
			return;

		Player player = (Player) event.getWhoClicked();
		Page page = pages.get(player);

		if (page == null || !event.getView().getTopInventory().equals(page.getInventory()))
			return;

		event.setCancelled(true);

		if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR))
			return;

		if (event.getRawSlot() == 2)
		{
			display(player, page.getIndex() - 1);
		}
		else if (event.getRawSlot() == 6)
		{
			display(player, page.getIndex() + 1);
		}
		else if (event.getRawSlot() == 4)
		{
			CustomBalloons.get().getBalloonsModule().removeBalloon(player);
			page.getInventory().setItem(4, new ItemStack(Material.AIR));
			player.sendMessage(LanguageModule.Strings.MESSAGE_BALLOON_REMOVED.asChatMessage());
		}
		else if (event.getRawSlot() >= 9 && event.getRawSlot() <= 9 + BALLOONS_PER_PAGE)
		{
			CustomBalloons.get().getBalloonsModule().setBalloon(player, page.getBalloon(page.getIndex() * BALLOONS_PER_PAGE + event.getRawSlot() - 9));
			page.getInventory().setItem(4, remove);
			player.sendMessage(LanguageModule.Strings.MESSAGE_BALLOON_SET.asChatMessage());
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if (!(event.getPlayer() instanceof Player))
			return;

		Player player = (Player) event.getPlayer();
		Page page = pages.get(player);

		if (page == null)
			return;

		if (event.getView().getTopInventory().equals(page.getInventory()))
			pages.remove(player);
	}

	@Override
	public void reload()
	{
		clear();
	}

	public void display(Player player, int pageIndex)
	{
		List<BalloonBlueprint> balloons = CustomBalloons.get().getBalloonsModule().getBlueprints()
			.stream()
			.filter((BalloonBlueprint balloon) -> balloon.canHold(player))
			.collect(Collectors.toList());

		if (balloons.size() == 0)
		{
			player.sendMessage(LanguageModule.Strings.MESSAGE_BALLOON_NO_BALLOONS.asChatMessage());
			return;
		}

		int pageCount = 1 + (int) Math.floor(balloons.size() / (BALLOONS_PER_PAGE + 1));

		if (pageIndex < 0 || pageIndex >= pageCount)
			throw new IndexOutOfBoundsException("specified page index is smaller than 0 or greater than " + (pageCount - 1));

		int inventorySize = Math.min(9 + (int) Math.ceil(balloons.size() / 9.0D) * 9, 54);
		Inventory inv = Bukkit.createInventory(null, inventorySize, LanguageModule.Strings.GUI_TITLE.toString());

		if (pageIndex > 0)
		{
			inv.setItem(2, previous);
		}

		if (pageCount > 1 && pageIndex < pageCount - 1)
		{
			inv.setItem(6, next);
		}

		if (CustomBalloons.get().getBalloonsModule().hasBalloon(player))
		{
			inv.setItem(4, remove);
		}

		int slotOffset = 0;
		int max = Math.min((pageIndex + 1) * BALLOONS_PER_PAGE, balloons.size());

		for (int i = pageIndex * BALLOONS_PER_PAGE; i < max; ++i)
		{
			BalloonBlueprint balloon = balloons.get(i);
			inv.setItem(9 + slotOffset++, balloon.item);
		}

		player.openInventory(inv);
		pages.put(player, new Page(pageIndex, balloons, inv));
	}

	public void clear()
	{
		for (Page page : pages.values())
		{
			List<HumanEntity> viewers = new ArrayList<>(page.getInventory().getViewers());

			for (HumanEntity viewer : viewers)
			{
				viewer.closeInventory();
			}
		}

		pages.clear();
	}
}
