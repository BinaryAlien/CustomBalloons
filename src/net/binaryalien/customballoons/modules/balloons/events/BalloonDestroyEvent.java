package net.binaryalien.customballoons.modules.balloons.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.binaryalien.customballoons.modules.balloons.Balloon;

public class BalloonDestroyEvent extends Event
{
	private static final HandlerList HANDLERS = new HandlerList();

	private Balloon balloon;

	public BalloonDestroyEvent(Balloon balloon)
	{
		this.balloon = balloon;
	}

	public static HandlerList getHandlerList()
	{
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLERS;
	}

	public Balloon getBalloon()
	{
		return balloon;
	}
}
