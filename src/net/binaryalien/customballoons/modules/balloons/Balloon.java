package net.binaryalien.customballoons.modules.balloons;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import net.binaryalien.customballoons.CustomBalloons;
import net.binaryalien.customballoons.modules.balloons.events.BalloonDestroyEvent;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.EntitySlime;
import net.minecraft.server.v1_12_R1.World;

public class Balloon extends EntitySlime
{
	private static final float WIND_Y = 0.1F;

	private Player owner;
	private BalloonBlueprint blueprint;

	private Slime bukkitThis;
	private ArmorStand as;

	private Vector wind;

	private BukkitTask task;

	public Balloon(World world, Player owner, BalloonBlueprint blueprint)
	{
		super(world);

		this.owner = owner;
		this.blueprint = blueprint;

		wind = new Vector();

		bukkitThis = (Slime) getBukkitEntity();
		Location loc = computePosition();
		setInvulnerable(true);
		setInvisible(true);
		setSilent(true);
		setLocation(loc.getX(), loc.getY(), loc.getZ(), 0.0F, 0.0F);
		bukkitThis.setCollidable(false);
		bukkitThis.setGravity(false);
		bukkitThis.setAI(false);
		bukkitThis.setSize(-1);

		as = (ArmorStand) owner.getWorld().spawnEntity(loc.subtract(0.0D, 1.0D, 0.0D), EntityType.ARMOR_STAND);
		as.setInvulnerable(true);
		as.setVisible(false);
		as.setCollidable(false);
		as.setMarker(true);
		as.setGravity(false);
		as.setBasePlate(false);
		setItem(blueprint.item);

		FixedMetadataValue metadata;

		metadata = new FixedMetadataValue(CustomBalloons.get(), CustomBalloons.get().getSignature().toString());
		bukkitThis.setMetadata("signature", metadata);
		as.setMetadata("signature", metadata);

		metadata = new FixedMetadataValue(CustomBalloons.get(), owner.getUniqueId().toString());
		bukkitThis.setMetadata("owner-uuid", metadata);
		as.setMetadata("owner-uuid", metadata);

		task = Bukkit.getScheduler().runTaskTimer(CustomBalloons.get(), () -> update(), 1L, 3L);
	}

	@Override
	public boolean isInvulnerable(DamageSource source)
	{
		return true;
	}

	@Override
	public void B_()
	{
		bukkitThis.setLeashHolder(owner);
	}

	public void update()
	{
		if (!as.isValid() || !bukkitThis.isValid() || !owner.getWorld().equals(bukkitThis.getWorld()) || !bukkitThis.getWorld().equals(as.getWorld()))
		{
			destroy();
			CustomBalloons.get().getBalloonsModule().setBalloon(owner, blueprint);
			return;
		}

		Location loc = computePosition();

		if (loc != null)
		{
			setLocation(loc.getX(), loc.getY(), loc.getZ(), 0.0F, 0.0F);
			as.teleport(loc.subtract(0.0D, 1.0D, 0.0D));
		}
	}

	private Location computePosition()
	{
		int mod = bukkitThis.getTicksLived() % 100;

		if (mod == 0)
			wind.setY(-wind.getY());

		if (mod > 50)
			mod = 100 - mod;

		float percent = mod / 50.0F;
		float windY = WIND_Y * percent;

		if (wind.getY() < 0.0D)
			wind.setY(-windY);
		else
			wind.setY(windY);

		Location loc = owner.getLocation().clone();
		double angle = Math.toRadians(loc.getYaw() - 180.0F);
		loc = loc.add(Math.cos(angle), 2.5D, Math.sin(angle)).add(wind);

		return loc;
	}

	public void destroy()
	{
		task.cancel();
		as.remove();
		bukkitThis.remove();
		owner.playSound(as.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0F, 1.0F);
		Bukkit.getPluginManager().callEvent(new BalloonDestroyEvent(this));
	}

	public Player getOwner()
	{
		return owner;
	}

	public void setItem(ItemStack item)
	{
		as.getEquipment().setHelmet(item);
	}
}
