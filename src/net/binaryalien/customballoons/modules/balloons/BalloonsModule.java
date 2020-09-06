package net.binaryalien.customballoons.modules.balloons;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import net.binaryalien.customballoons.CustomBalloons;
import net.binaryalien.customballoons.CustomBalloonsException;
import net.binaryalien.customballoons.modules.LanguageModule;
import net.binaryalien.customballoons.modules.Module;
import net.binaryalien.customballoons.modules.balloons.events.BalloonDestroyEvent;
import net.minecraft.server.v1_12_R1.World;

public class BalloonsModule implements Module, Listener
{
	private File balloonsFile;
	private YamlConfiguration balloonsConf;

	private List<BalloonBlueprint> blueprints;
	private Map<Player, Balloon> balloons;

	public BalloonsModule()
	{
		balloonsFile = new File(CustomBalloons.get().getDataFolder(), "balloons.yml");

		if (!balloonsFile.exists())
		{
			makeDefaultBalloons();
		}

		blueprints = new ArrayList<>();
		balloons = new HashMap<>();

		reload();
	}

	@Override
	public void reload()
	{
		blueprints.clear();
		clearBalloons();

		balloonsConf = YamlConfiguration.loadConfiguration(balloonsFile);

		for (String id : balloonsConf.getKeys(false))
		{
			if (id.equalsIgnoreCase("example"))
				continue;

			ConfigurationSection section = balloonsConf.getConfigurationSection(id);

			try
			{
				BalloonBlueprint blueprint = new BalloonBlueprint(section);

				if (blueprints.contains(blueprint))
				{
					CustomBalloons.get().getLogger().warning("Duplicate balloon '" + id + '\'');
				}
				else
				{
					blueprints.add(blueprint);
				}

			}
			catch (CustomBalloonsException e)
			{
				CustomBalloons.get().getLogger().severe("Failed to load balloon: " + e.getMessage());
			}
		}

		CustomBalloons.get().getLogger().info(blueprints.size() + " balloon" + ((blueprints.size() == 0 || blueprints.size() > 1) ? 's' : "") + " loaded");
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Balloon balloon = balloons.get(event.getPlayer());

		if (balloon != null)
		{
			CustomBalloons.get().getLogger().info(event.getPlayer().getName() + " disconnected, removing it's balloon...");
			balloon.destroy();
		}
	}

	@EventHandler
	public void onVehicleEnter(VehicleEnterEvent event)
	{
		if (!CustomBalloons.get().getConfig().getBoolean("custom-remove-conditions.on-vehicle-enter") || !(event.getEntered() instanceof Player))
			return;

		Player player = (Player) event.getEntered();
		Balloon balloon = balloons.get(player);

		if (balloon != null)
		{
			balloon.destroy(); // Will trigger BalloonDestroyEvent where we will remove the balloon from `balloons`
			player.sendMessage(LanguageModule.Strings.MESSAGE_BALLOON_REMOVED_AUTO.asChatMessage());
		}
	}

	@EventHandler
	public void onBalloonDestroy(BalloonDestroyEvent event)
	{
		balloons.remove(event.getBalloon().getOwner());
	}

	public void removeBalloon(Player owner)
	{
		Balloon balloon = balloons.get(owner);

		if (balloon != null)
			balloon.destroy(); // Will trigger BalloonDestroyEvent where we will remove the balloon from `balloons`
	}

	public void setBalloon(Player owner, BalloonBlueprint blueprint)
	{
		Balloon balloon = balloons.get(owner);

		if (balloon == null)
		{
			World world = ((CraftWorld) owner.getWorld()).getHandle();

			balloon = new Balloon(world, owner, blueprint);
			world.addEntity(balloon, SpawnReason.CUSTOM);

			balloons.put(owner, balloon);
		}
		else
		{
			balloons.get(owner).setItem(blueprint.item);
		}
	}

	public boolean hasBalloon(Player player)
	{
		return balloons.containsKey(player);
	}

	/**
	 * Copies the default balloons configuration file to the plugin's folder
	 */
	private void makeDefaultBalloons()
	{
		CustomBalloons.get().getLogger().info("Making default balloons configuration file...");

		try
		{
			balloonsFile.createNewFile();

			InputStream in = CustomBalloons.class.getResourceAsStream("/balloons.yml");
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(balloonsFile));

			byte[] buffer = new byte[1024];
			int len;

			while ((len = in.read(buffer)) > 0)
			{
				out.write(buffer, 0, len);
			}

			out.close();
			in.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Destroys all the balloons
	 */
	public void clearBalloons()
	{
		for (Balloon balloon : balloons.values())
		{
			balloon.destroy();
		}

		balloons.clear();
	}

	public List<BalloonBlueprint> getBlueprints()
	{
		return blueprints;
	}
}
