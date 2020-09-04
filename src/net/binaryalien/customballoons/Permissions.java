package net.binaryalien.customballoons;

import net.binaryalien.customballoons.modules.balloons.BalloonBlueprint;

public enum Permissions
{
	PERMISSION_COMMAND_RELOAD("command.reload"),
	PERMISSION_COMMAND_CLEANUP("command.cleanup"),
	PERMISSION_BALLOONS_ALL("balloons.*");

	private static final String NODE_PREFIX = "customballoons.";

	private String node;

	private Permissions(String node)
	{
		this.node = node;
	}

	public static String getBalloonPermission(BalloonBlueprint blueprint)
	{
		return NODE_PREFIX + "balloons." + blueprint.id;
	}

	@Override
	public String toString()
	{
		return NODE_PREFIX + node;
	}
}
