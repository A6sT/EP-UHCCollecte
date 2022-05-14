package fr.a6st.epuhccollecte.listeners;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.features.Team;
import fr.a6st.epuhccollecte.features.collecte.Collecte;
import fr.a6st.epuhccollecte.scoreboard.ScoreBoardCycle;

public class FeaturesListeners implements Listener {

	
	//=============== Partie privée ===================
	
	
	private Main main; //La class principale;

	

	
	//=============== Partie publique ===================
	
	public FeaturesListeners(Main main) {
		this.main = main;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event)
	{
		Inventory inv = event.getInventory();
		Player player = (Player)event.getWhoClicked();
		ItemStack current = event.getCurrentItem();
		Collecte collecte = main.getCollecte();
		
		if(inv.getName().equalsIgnoreCase("§6§lDéposez les ressources")) //Interaction dans le menu de dépot d'item
		{
			if(collecte.isAlive()) {
				if (current.getType().equals(collecte.getItem().getType()) && current != null) //Verifie si l'item est bien celui demandé par l'event
		        {
					int itemPos = event.getSlot();
					int nbPoints = current.getAmount();
					main.getPlayerTeam(player).addScoreCollecte(nbPoints);
					
					for(Player mates : main.getPlayerTeam(player).getPlayers()) {
						mates.playSound(player.getLocation(), Sound.LEVEL_UP, 10, 2);
						mates.sendMessage("§aVotre équipe vien de gagner §e"+ nbPoints +" §apoints !");
					}
					
					player.getInventory().clear(itemPos); //Clear les items a la pos itemPos de l'inv du joueur
					ScoreBoardCycle.actualiseTeam(main);
					
		        } else { //Empecher le déplacement
		        	event.setCancelled(true);
		        }
			} else {
				player.sendMessage("§cLa période de collecte n'as pas commencé !");
				event.setCancelled(true);
			}
			
		} else if(inv.getName().equalsIgnoreCase("§eChoisissez un joueur")) {
			if(current.getType().equals(new ItemStack(Material.SKULL_ITEM, 1, (short) 3).getType()) && current != null) {
				SkullMeta meta = (SkullMeta) current.getItemMeta();
				Player revived = Bukkit.getPlayer(meta.getOwner());
				Location reviveLoc = player.getLocation();
				
				revived.setGameMode(GameMode.SURVIVAL);
				revived.teleport(reviveLoc);
				revived.setHealth(revived.getMaxHealth());
				
				//On modifie les listes
				main.getMorts().remove(revived);
				main.getPlayers().add(revived);
				revived.sendMessage("§aVous avez été ressucité!");
				player.sendMessage("§aLe joueur §e" + revived.getName() + "§a a été ressucité");
				
				Bukkit.broadcastMessage("§cUn joueur a été ressucité aux coordonnées §7x"+reviveLoc.getBlockX() + "; z"+ reviveLoc.getBlockZ());
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Team playerTeam = main.getPlayerTeam(player);
		ItemStack current = event.getItem();
		
		if(current != null && current.getType().equals(Material.BEACON)) {
			if(playerTeam.isLeader(player)) {
				//Menu de revive
				Inventory inv = Bukkit.createInventory(null, 9, "§eChoisissez un joueur");
				
				//Setup de l'inventaire
				for(Player gamer : main.getPlayerTeam(player).getPlayers()) {
					if(main.getMorts().contains(gamer)) {
						ItemStack tete = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
						SkullMeta teteMeta = (SkullMeta) tete.getItemMeta();
						ArrayList<String> lore = new ArrayList<>();
						teteMeta.setOwner(gamer.getName());
						lore.clear();
						lore.add("§7Cliquez ici pour réssusciter ce joueur");
				        teteMeta.setLore(lore);
				        teteMeta.setDisplayName("§e"+gamer.getName());
				        tete.setItemMeta(teteMeta);
				        inv.addItem(tete);
					}
				}
				
				player.openInventory(inv);
			} else {
				player.sendMessage("§cSeul le chef de l'équipe peut utiliser cet item");
			}
		} else if(current != null && current.getType().equals(Material.COMPASS)) {
			if(main.getCollecte().getWinnerTeam() != null) {
				for(Player teamate : playerTeam.getPlayers()) {
					teamate.setCompassTarget(main.getCollecte().getWinnerTeam().getLeader().getLocation());
					teamate.sendMessage("§aLa boussole a été actualisée par "+player.getName());
				}
			}
		}
	}
	
	@EventHandler
	public void onBlocPlaced(BlockPlaceEvent event) {
		Location blockPos = event.getBlockPlaced().getLocation();
		Player player = event.getPlayer();
		for(Location loc : main.getSchemLocs()) {
			if(player.getLocation().getWorld().equals(Bukkit.getWorld("world"))) {
				if(blockPos.distance(loc) < 30) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlocPlaced(BlockBreakEvent event) {
		Location blockPos = event.getBlock().getLocation();
		Player player = event.getPlayer();
		for(Location loc : main.getSchemLocs()) {
			if(player.getLocation().getWorld().equals(Bukkit.getWorld("world"))) {
				if(blockPos.distance(loc) < 30) {
					event.setCancelled(true);
				}
			}
			
		}
	}
}
