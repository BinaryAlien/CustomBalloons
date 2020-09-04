package net.binaryalien.customballoons.modules.balloons;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.binaryalien.customballoons.CustomBalloons;
import net.binaryalien.customballoons.CustomBalloonsException;
import net.binaryalien.customballoons.Permissions;

public class BalloonBlueprint
{
	public final String id;
	public final String name;
	public final ItemStack item;

	public BalloonBlueprint(ConfigurationSection section) throws CustomBalloonsException
	{
		id = section.getCurrentPath().toLowerCase();

		if (section.isSet("name"))
		{
			name = section.getString("name");

			if (name.isEmpty())
				throw new CustomBalloonsException('\'' + id + "' name attribute is empty");
		}
		else
		{
			throw new CustomBalloonsException('\'' + id + "' does not have a 'name' attribute");
		}

		if (section.isSet("texture-hash"))
		{
			String textureHash = section.getString("texture-hash");

			if (textureHash.isEmpty())
				throw new CustomBalloonsException('\'' + id + "' texture attribute is empty");

			item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);

			String json = "{textures:{SKIN:{url:\"http://textures.minecraft.net/texture/" + textureHash + "\"}}}";
			byte[] data = Base64.getEncoder().encode(json.getBytes());

			GameProfile profile = new GameProfile(UUID.randomUUID(), null);
			profile.getProperties().put("textures", new Property("textures", new String(data)));

			try
			{
				SkullMeta meta = (SkullMeta) item.getItemMeta();

				Field field = meta.getClass().getDeclaredField("profile");
				field.setAccessible(true);
				field.set(meta, profile);

				item.setItemMeta(meta);
			}
			catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e)
			{
				CustomBalloons.get().getLogger().warning("Could not set custom texture for head '" + id + '\'');
				e.printStackTrace();
			}
		}
		else if (section.isSet("item"))
		{
			String itemStr = section.getString("item");

			if (itemStr.isEmpty())
				throw new CustomBalloonsException('\'' + id + "' item attribute is empty");

			String typeStr;
			short damage = 0;

			if (itemStr.contains(":"))
			{
				String[] spl = itemStr.split(":", 1);

				try
				{
					damage = Short.parseShort(spl[1]);

					if (damage < 0)
						throw new NumberFormatException();
				}
				catch (NumberFormatException e)
				{
					throw new CustomBalloonsException('\'' + id + "' item attribute's damage value '" + spl[1] + "' is invalid");
				}

				typeStr = spl[0].toUpperCase();
			}
			else
			{
				typeStr = itemStr.toUpperCase();
			}

			if (typeStr.isEmpty())
				throw new CustomBalloonsException('\'' + id + "' item attribute material is empty");

			Material type = Material.getMaterial(typeStr);

			if (type == null)
				throw new CustomBalloonsException('\'' + id + "' item attribute material '" + typeStr + "' was not found");

			item = new ItemStack(type, 1, damage);
		}
		else
		{
			throw new CustomBalloonsException('\'' + id + "' does not have neither a 'block' nor 'texture' attribute");
		}

		ItemMeta meta = item.getItemMeta();

		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

//		if (section.isSet("lore"))
//		{
//			List<String> lore = section.getStringList("lore").stream()
//				.map((String line) -> ChatColor.translateAlternateColorCodes('&', line))
//				.collect(Collectors.toList());
//			meta.setLore(lore);
//		}

		item.setItemMeta(meta);
	}

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof BalloonBlueprint))
			return false;

		return id.equals(((BalloonBlueprint) other).id);
	}

	public boolean canHold(Player player)
	{
		return player.hasPermission(Permissions.PERMISSION_BALLOONS_ALL.toString()) || player.hasPermission(Permissions.getBalloonPermission(this));
	}
}
