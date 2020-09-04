package net.binaryalien.customballoons.commands;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.metadata.MetadataValue;

import net.binaryalien.customballoons.CustomBalloons;
import net.binaryalien.customballoons.Permissions;
import net.binaryalien.customballoons.modules.LanguageModule;

public class CommandBalloons implements CommandExecutor, TabCompleter
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		switch (args.length)
		{
		case 0:
			if (sender instanceof Player)
			{
				Player player = (Player) sender;
				CustomBalloons.get().getBalloonsGUI().display(player, 0);
			}

			return true;

		case 1:
			if (args[0].equalsIgnoreCase("reload"))
			{
				if (sender.hasPermission(Permissions.PERMISSION_COMMAND_RELOAD.toString()))
				{
					CustomBalloons.get().reload();
					sender.sendMessage(LanguageModule.Strings.MESSAGE_COMMAND_RELOAD.asChatMessage());
				}
				else
				{
					sender.sendMessage(LanguageModule.Strings.MESSAGE_COMMAND_NO_PERMISSION.asChatMessage());
				}

				return true;
			}
			else if (args[0].equalsIgnoreCase("cleanup"))
			{

				if (sender.hasPermission(Permissions.PERMISSION_COMMAND_CLEANUP.toString()))
				{
					int count = 0;

					sender.sendMessage(LanguageModule.Strings.MESSAGE_COMMAND_CLEANUP.asChatMessage());

					for (World world : Bukkit.getWorlds())
					{
						Iterator<Entity> it = world.getEntities().iterator();

						while (it.hasNext())
						{
							Entity entity = it.next();

							if (!(entity instanceof ArmorStand) && !(entity instanceof Slime))
								continue;

							boolean removed = false;

							// First check with plugin's signature
							for (MetadataValue value : entity.getMetadata("signature"))
							{
								if (value.getOwningPlugin().equals(CustomBalloons.get()))
								{
									if (!value.asString().equals(CustomBalloons.get().getSignature().toString()))
									{
										entity.remove();
										removed = true;
										++count;
									}

									break;
								}
							}

							if (removed)
								continue;

							// Second check with balloon's ownership
							for (MetadataValue value : entity.getMetadata("owner-uuid"))
							{
								if (value.getOwningPlugin().equals(CustomBalloons.get()))
								{
									Player owner = Bukkit.getPlayer(UUID.fromString(value.asString()));

									if (owner == null || !CustomBalloons.get().getBalloonsModule().hasBalloon(owner))
									{
										entity.remove();
										++count;
									}

									break;
								}
							}
						}
					}

					sender.sendMessage(String.format(LanguageModule.Strings.MESSAGE_COMMAND_CLEANUP_RESULT.asChatMessage(), count));
				}
				else
				{
					sender.sendMessage(LanguageModule.Strings.MESSAGE_COMMAND_NO_PERMISSION.asChatMessage());
				}

				return true;
			}
			else
			{
				return false;
			}

		default:
			return false;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args)
	{
		List<String> empty = Arrays.asList();

		switch (args.length)
		{
		case 1:
			if (args[0].length() == 0)
				return Arrays.asList("cleanup", "reload");
			else if ("reload".startsWith(args[0].toLowerCase()))
				return Arrays.asList("reload");
			else if ("cleanup".startsWith(args[0].toLowerCase()))
				return Arrays.asList("cleanup");
			else
				return empty;

		default:
			return empty;
		}
	}
}
