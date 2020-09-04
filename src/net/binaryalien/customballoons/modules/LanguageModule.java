package net.binaryalien.customballoons.modules;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import net.binaryalien.customballoons.CustomBalloons;

public class LanguageModule implements Module
{
	private File langFile;

	public LanguageModule()
	{
		langFile = new File(CustomBalloons.get().getDataFolder(), "lang.yml");
		reload();
	}

	@Override
	public void reload()
	{
		YamlConfiguration lang	= new YamlConfiguration();
		YamlConfiguration in	= YamlConfiguration.loadConfiguration(langFile);

		for (Strings string : Strings.values())
		{
			if (in.isSet(string.node))
				string.content = ChatColor.translateAlternateColorCodes('&', in.getString(string.node));
			else
				CustomBalloons.get().getLogger().info(langFile.getName() + " > '" + string.node + "' default string set");

			lang.set(string.node, string.content);
		}

		try
		{
			lang.save(langFile);
		}
		catch (IOException e)
		{
			CustomBalloons.get().getLogger().severe("Failed to save the language file");
			e.printStackTrace();
		}
	}

	public enum Strings
	{
		MESSAGE_PREFIX("message.prefix", "&e[&bCustomBalloons&e] &r"),
		GUI_TITLE("gui.title", "&eSelect a balloon"),
		GUI_PREVIOUS("gui.previous", "&cPrevious"),
		GUI_NEXT("gui.next", "&aNext"),
		GUI_REMOVE("gui.remove", "&cRemove your balloon"),
		MESSAGE_COMMAND_NO_PERMISSION("message.permission-denied-command", "&cYou do not have the permission to execute this command"),
		MESSAGE_COMMAND_RELOAD("message.command.reload", "&aConfiguration reloaded"),
		MESSAGE_COMMAND_CLEANUP("message.command.cleanup", "&eInitiating cleanup..."),
		MESSAGE_COMMAND_CLEANUP_RESULT("message.command.cleanup-result", "&b%d &eentities removed"),
		MESSAGE_BALLOON_SET("message.balloon.set", "&bHere is your balloon!"),
		MESSAGE_BALLOON_REMOVED("message.balloon.removed", "&eYour balloon was removed!"),
		MESSAGE_BALLOON_NO_BALLOONS("message.balloon.no-balloons", "&cYou do not have access to any ballons/no balloons were configured yet");

		private String node;
		private String content;

		private Strings(String node, String content)
		{
			this.node = node;
			this.content = ChatColor.translateAlternateColorCodes('&', content);
		}

		public String getNode()
		{
			return node;
		}

		@Override
		public String toString()
		{
			return content;
		}

		public String asChatMessage()
		{
			return MESSAGE_PREFIX.content + content;
		}
	}
}
