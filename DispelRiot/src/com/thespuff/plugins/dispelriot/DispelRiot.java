package com.thespuff.plugins.dispelriot;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class DispelRiot extends JavaPlugin {

	public static String pluginName;
	public static String pluginVersion;
	public static Server server;
	public static DispelRiot plugin;
	public static BukkitTask asyncTask;
	private static boolean lock;
	private static Random random;
	private static int tickDelay;


	public void onDisable() {
		this.getServer().getScheduler().cancelTask(asyncTask.getTaskId());//cancelAllTasks();
		log("Disabled");
	}

	public void onEnable() {
		pluginName = this.getDescription().getName();
		pluginVersion = this.getDescription().getVersion();
		server = this.getServer();
		plugin = this;
		random = new Random();
		tickDelay = 60; //TODO: Should load from config

		asyncTask = this.getServer().getScheduler().runTaskTimer(this, new Runnable() { public void run() { checkClumps(); } }, 60, tickDelay); //unclumps every 60 ticks=3 seconds

		List<String> worlds;
		tickDelay = 60
		radiusToCheck = 1.5
		dieChance = 50
		maxNumber = 4
		explode = true
		power = .1
		removeEntity = true

		
		log("Enabled. Keeps mobs from clumping.");
	}
	
	public void log(Object in) {
		System.out.println("[" + pluginName + "] " + String.valueOf(in));
	}
	
	public void checkClumps() {
		if(lock) { log("Still running checkClump!"); return; }
		lock=true;
		List<World> worlds = server.getWorlds(); //TODO: Limit which worlds we check on. Load from CONfig
		Set<Entity> forRemoval = new HashSet<Entity>();

		for(World world : worlds) {
			List<Entity> ents = world.getEntities();
			if(ents.size()<1) { continue; }

			try{

				for(Entity ent : ents){
					if(random.nextInt(50)>1) { continue; } //TODO: Load from config: "dieChance"
					if(!ent.isValid()) { continue; }
					if(!(ent instanceof LivingEntity)) { continue; }
					if((ent instanceof Player)) { continue; }
					List<Entity> nearEnts = ent.getNearbyEntities(1.5,1,1.5); //TODO: load from config: "radiusToCheck"

					int k = 0;

					for(Entity nearEnt: nearEnts){
						if(nearEnt.getType().equals(ent.getType())) { k++; }
					}

					if(k>4) { //TODO: load from config: "maxNumber"
						if(ent.isValid()) {
							forRemoval.add(ent);
						}
					}
				}
			} catch(NoSuchElementException e) { log("Whoops. Exception!"); }


			for(Entity ent: forRemoval){
				if(ent.isValid()) {
					explode(ent);
				}
			}
		}
		lock=false;
	}

	private void explode(final Entity entity) {
		
		server.getScheduler().runTaskLater(this, new Runnable() {public void run() {
			if(!entity.isValid()) { return; }
			entity.getWorld().createExplosion(entity.getLocation(), .1f); //TODO: Load from config: "explode", "power"
			if(entity.isValid()) { entity.remove(); } //TODO: load from config: "removeEntity"
			}}, random.nextInt(tickDelay-1));
	}
}

/* config.yml >
 * 
 * dispelRiot:
 *   worlds:
 *     world: true
 *     otherworld: false
 *   settings:
 *     tickDelay: 60
 *     radiusToCheck: 1.5
 *     dieChance: 50
 *     maxNumber: 4
 *   removal:
 *     explode: true
 *     power: .1
 *     removeEntity: true
 */
