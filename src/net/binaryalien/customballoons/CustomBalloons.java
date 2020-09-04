package net.binaryalien.customballoons;

import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import net.binaryalien.customballoons.commands.CommandBalloons;
import net.binaryalien.customballoons.modules.LanguageModule;
import net.binaryalien.customballoons.modules.balloons.Balloon;
import net.binaryalien.customballoons.modules.balloons.BalloonsModule;
import net.binaryalien.customballoons.modules.gui.BalloonsGUI;
import net.minecraft.server.v1_12_R1.EntityTypes;
import net.minecraft.server.v1_12_R1.MinecraftKey;

public class CustomBalloons extends JavaPlugin
{
	private static CustomBalloons instance;
	public static CustomBalloons get() { return instance; }

	private UUID signature;

	private LanguageModule language;
	private BalloonsModule balloons;
	private BalloonsGUI gui;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable()
	{
		instance = this;

		signature = UUID.randomUUID();

		super.onEnable();

		if (!CustomBalloons.get().getDataFolder().exists())
			CustomBalloons.get().getDataFolder().mkdirs();

		EntityTypes.b.a(EntityType.SLIME.getTypeId(), new MinecraftKey("CustomBalloons"), Balloon.class);

		language = new LanguageModule();
		balloons = new BalloonsModule();
		gui = new BalloonsGUI();

		getServer().getPluginManager().registerEvents(balloons, this);
		getServer().getPluginManager().registerEvents(gui, this);

		getCommand("balloons").setExecutor(new CommandBalloons());
	}

	@Override
	public void onDisable()
	{
		balloons.clearBalloons();
		gui.clear();
	}

	/**
	 * Reloads the plugin
	 */
	public void reload()
	{
		language.reload();
		balloons.reload();
		gui.reload();
	}

	public UUID getSignature()
	{
		return signature;
	}

	public LanguageModule getLanguageModule()
	{
		return language;
	}

	public BalloonsModule getBalloonsModule()
	{
		return balloons;
	}

	public BalloonsGUI getBalloonsGUI()
	{
		return gui;
	}
}
