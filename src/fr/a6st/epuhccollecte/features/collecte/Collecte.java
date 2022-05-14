package fr.a6st.epuhccollecte.features.collecte;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.features.Team;

public class Collecte {
	private ItemStack item; //Item a collecter
	private int time; //Temps avant la fin de la collecte
	private boolean live; //L'event est il en cours
	private Team winnerTeam = null; //Vainqueur de la précedente collecte. NULL par defaut
	
	public Collecte(Main main) {
		//Prend un item au hasard de la liste, et verifie qu'il n'est pas déja sortit pour une collecte
		boolean itemNotFound = true;
		while(itemNotFound) {
			ItemStack lfItem = main.getItemList().get(new Random().nextInt(main.getItemList().size()));
			if(!main.getCollecteItemList().contains(lfItem)) {
				item = lfItem;
				main.getCollecteItemList().add(lfItem);
				itemNotFound = false;
			}
		} 
		time = main.getConfig().getInt("features.timers.collectetime");
		live = true;
		
		for(Player player : main.getPlayers()) {
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 10, 2);
		}
		Bukkit.broadcastMessage(main.getConfig().getString("features.messages.collecte.start").replace("&", "§"));
		Bukkit.broadcastMessage(main.getConfig().getString("features.messages.collecte.item").replace("&", "§").replace("{time}", main.convertTimer(time)).replace("{item}", item.getType().name().toLowerCase()));
		Bukkit.broadcastMessage("§8Rappel: Les coordonnées des structures sont disponibles à l'aide de la commande §7/structures");
	}
	public Collecte(boolean live) {
		this.live = live;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public int getTime() {
		return time;
	}

	public boolean isAlive() {
		return live;
	}

	public void setTime(int i) {
		time = i;
	}

	public void setLive(boolean b) {
		live = b;
		
	}
	
	public void setWinnerTeam(Team winnerTeam) {
		this.winnerTeam = winnerTeam;
	}
	
	public Team getWinnerTeam() {
		return winnerTeam;
	}
}
