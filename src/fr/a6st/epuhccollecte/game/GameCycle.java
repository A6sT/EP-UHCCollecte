package fr.a6st.epuhccollecte.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.State;
import fr.a6st.epuhccollecte.features.Team;
import fr.a6st.epuhccollecte.features.collecte.Collecte;
import fr.a6st.epuhccollecte.scoreboard.ScoreBoardCycle;

public class GameCycle extends BukkitRunnable{

	
	//=============== Partie privée ===================
	
	
	private Main main; //La class principale;

	

	
	//=============== Partie publique ===================
	
	
	public GameCycle(Main main) { //Pour récupérer le main de notre class principale;
		this.main=main;
		startFarmingTimer=true;
		startGracePeriod = true;
		startPvPTimer = true;
		startBorderReduction = true;
	}
	
	public static int timer; //Timer
	public boolean startGracePeriod = true;
	public boolean startFarmingTimer = true;
	public boolean startPvPTimer = true;
	public boolean startBorderReduction = true;
	
	
	
	@Override
	public void run() { //Au lancement de la partie;
		switch(main.getState()) {
		  //=============== Phase D'invincibilitée ===================
		  case GRACE:
			if(startGracePeriod) { //Au debut de la grace period
				startGracePeriod = false;
				timer=60;
				Bukkit.getWorld("world").setFullTime(1000); //On met le jour
				for(Player player : main.getPlayers()) {
					player.getInventory().clear();
					player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20*60, 255)); //Ajoute resistance 255 a chaque joueur pendant 60 secondes (60*20 ticks)
				}
			}
			//Rappel dans le chat avant la prochaine periode
			if(timer == 30 || timer == 15|| timer <=5 && timer > 0) {
				for(Player player : main.getPlayers()) {
					player.playSound(player.getLocation(), Sound.CLICK, 10, 1);
				}
				if(timer == 30) { //Envoies des messages dans le chat (pour eviter le spam)
					Bukkit.broadcastMessage(main.getConfig().getString("messages.GraceReminder").replace("&", "§").replace("{timer}", Integer.toString(timer)));
				}
			}
			//Changement de State: Plus d'invicibilité
			if(timer ==0) {
				Bukkit.broadcastMessage(main.getConfig().getString("messages.GraceEnd").replace("&", "§"));//Message pour la fin de la grace period
				main.setState(State.FARMING); //Passage en farming
			}
			timer --;
		    break;
		    
		  //=============== Phase de Farm =================== 
		  case FARMING:
			if(startFarmingTimer) { //si on doit lancer le pvp;
				startFarmingTimer=false;
				main.setCollecte(new Collecte(main));
				timer = main.getConfig().getInt("timers.farming"); //Recupère le timer de la config
			}
				
			//Rappel dans le chat avant la prochaine periode 
			if(timer == 3600 || timer == 1800 || timer == 900 || timer == 300) {// En minutes
				Bukkit.broadcastMessage(main.getConfig().getString("messages.FarmReminder").replace("&", "§").replace("{timer}", Integer.toString(timer/60)));
			} 
			if(timer == 60 || (timer <= 10 && timer > 0)) { // En secondes
				for(Player player : main.getPlayers()) {
					player.playSound(player.getLocation(), Sound.CLICK, 10, 1);
				}
			}
				
			//Changement de State: Le pvp s'active
			if(timer==0) { 
				for(Player player : main.getPlayers()) {
					player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 10, 1);
				}
					
				//Activation du pvp
				Bukkit.broadcastMessage(main.getConfig().getString("messages.pvp").replace("&", "§")); //message pvp on;
				Bukkit.getWorld("world").setPVP(true); //On start le pvp après le gamecycle;
				main.setState(State.PVP);
			}
			timer --; //update timer;
			break;
			
		  //=============== Phase PVP ===================
		  case PVP:
			if(startPvPTimer) {
				startPvPTimer=false;
				timer = main.getConfig().getInt("timers.pvp"); //Recupère le timer de la config
			}
			
			if(timer == 1200) {
				if(main.getCollecte().isAlive() == false) {
					main.setCollecte(new Collecte(main));
				}
			}
			
			//Rappel dans le chat avant la prochaine periode 
			if(timer == 3600 || timer == 1800 || timer == 900 || timer == 300) {// En minutes
				Bukkit.broadcastMessage(main.getConfig().getString("messages.borderReminder").replace("&", "§").replace("{timer}", Integer.toString(timer/60)));
			} 
			if(timer == 60 || (timer <= 10 && timer > 0)) { // En secondes
				for(Player player : main.getPlayers()) {
					player.playSound(player.getLocation(), Sound.CLICK, 10, 1);
				}
			}
				
			if(timer==0) { 
				Bukkit.broadcastMessage(main.getConfig().getString("messages.borderReduction").replace("&", "§")); //message pvp on;
				Bukkit.getWorld("world").setPVP(true); //On start le pvp après le gamecycle;
				main.setState(State.BORDER);
			}
			timer --; //update timer;
			break;
		  
		  //=============== Phase Réduction de bordure ===================
		  case BORDER:
			if(startBorderReduction) {
				startBorderReduction = false;
				main.getWorldBorder().setSize(100.0, main.getConfig().getInt("timers.borderReductionTime"));
				main.getNetherWorldBorder().setSize(10, (int)main.getConfig().getInt("timers.borderReductionTime"));
			}
			break;
		default:
			//Ne doit jamais arriver
			break;
		}
		
		//=============== Phase Collecte ===================
		if(main.collecte.isAlive()) {
			Collecte collecte = main.getCollecte();
			if(collecte.getTime() <= 0) {
				main.collecte.setLive(false);
				ScoreBoardCycle.actualiseTeam(main);
				//Annonce fin de collecte
				for(Player player : main.getPlayers()) {
					player.playSound(player.getLocation(), Sound.LEVEL_UP, 10, 0);
				}
				
				//Affichage des scores
				Bukkit.broadcastMessage(main.getConfig().getString("features.messages.collecte.end").replace("&", "§"));

				List<Team> scores = new ArrayList<>();
				List<Team> winner = new ArrayList<>();
				for(Team team : main.getTeams()) {
					if(main.isTeamAlive(team)) {
						scores.add(team);
					}
				}
				int leaderBoard = 0;
				while(scores.size() > 0) {
					leaderBoard ++;
					int highScore = 0;
					List<Team> bestTeams = new ArrayList<>();
					for(Team team : scores) {
						int teamScore = team.getScoreCollecte();
						if(teamScore >= highScore) {
							bestTeams.add(team);
							highScore = teamScore;
						}
					}
					String listTeams = "";
					for(Team team : bestTeams) {
						scores.remove(team);
						listTeams += team.getColorCode()+team.getName()+ " §7et ";
						
						//Récuperation de(s) l'équipe gagnante(s)
						if(leaderBoard == 1) {
							winner.add(team);
						}
					}
					listTeams = listTeams.substring(0, listTeams.length()-3);
					String annoncePlacement = "§e#"+leaderBoard+": "+ listTeams + "- §e"+bestTeams.get(0).getScoreCollecte()+ " points";
					Bukkit.broadcastMessage(annoncePlacement);
				}

				if(winner.size() >1) {
					Bukkit.broadcastMessage("§aLes équipes gagnantes ont obtenu un avantage...");
					Bukkit.broadcastMessage("§cLes boussoles pointes maintenant vers les équipes gagnante");
				} else {
					Bukkit.broadcastMessage("§aL'équipe gagnante a obtenu un avantage...");
					Bukkit.broadcastMessage("§cLes boussoles pointes maintenant vers l'équipe gagnante");
				}
				
				//Si 2 équipes finissent premier, seul une des 2 équipe sera pointée
				//L'équipe chassé est désignée au hasard parmis toute les teams gagnantes
				collecte.setWinnerTeam(winner.get(new Random().nextInt(winner.size())));
				
				for(Player player : main.getPlayers()) {
					if(!main.getMorts().contains(player)) {
						player.setCompassTarget(collecte.getWinnerTeam().getLeader().getLocation());
					}
				}
				
				for(Team team : winner) {
					for(Player player : team.getPlayers()) {
						player.setMaxHealth(player.getMaxHealth()+4.0);
						player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*60*10, 3)); //Ajoute absorption 3 (=8 coeurs) a chaque joueur pendant 10 minutes (60*20*10 ticks)
						player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 5)); //Regen 5 pendant 5 secondes
					}
				}
				
				//Clear des points de chaque équipe
				for(Team team : main.getTeams()) {
					team.setScoreCollecte(0);
				}
			}
			//Timer - 1
			main.collecte.setTime(main.getCollecte().getTime()-1);
		}
	}
}
