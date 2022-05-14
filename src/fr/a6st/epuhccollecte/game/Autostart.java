package fr.a6st.epuhccollecte.game;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.State;
import fr.a6st.epuhccollecte.features.Team;
import fr.a6st.epuhccollecte.features.collecte.Collecte;
import fr.a6st.epuhccollecte.scoreboard.ScoreBoardCycle;

public class Autostart extends BukkitRunnable{

	
	//=============== Partie privée ===================
	
	
	private Main main; //La class principale;

	

	
	//=============== Partie publique ===================
	
	
	public Autostart(Main main) { //Pour récupérer le main de notre class principale;
		this.main=main;
		this.start=true;
	}
	
	
	
	
	//=============== Partie bizarre ===================
	
	
	private int timeToStart; //Timer avant le début de l'uhc;
	public boolean start = true;

	
	
	
	//=============== Partie publique ===================
	
	
	@Override
	public void run() { //Au lancement du décompte de la partie;
		if(start) { //Si on doit lancer la game;
			start=false;
			timeToStart=main.getConfig().getInt("timers.start"); //On get la config;
			
		}
		
		//Si un joueur quitte durant le démarrage de la partie
		if(Bukkit.getOnlinePlayers().size() < main.getPlayers().size()) {
			Bukkit.broadcastMessage("§cUn joueur à quitté la partie, annulation du démarrage");
			main.setState(State.IDLE);
			for(Player player : Bukkit.getOnlinePlayers()) {
				player.setLevel(0);
				player.playSound(player.getLocation(), Sound.VILLAGER_NO, 10, 1);
			}
			cancel();
		}
		
		for(Player pls : Bukkit.getOnlinePlayers()) { //Boucle sur tout les joueurs;
			pls.setLevel(timeToStart); //On met le timer en lvl pour le joueur;
			if(timeToStart==10 || (timeToStart<=5 && timeToStart>0)) { //On rajoute des sons pour le timer;
				pls.playSound(pls.getLocation(), Sound.ORB_PICKUP, 10, 1);
				
				Team playerTeam = main.getPlayerTeam(pls);
				if(playerTeam != null) {
					main.titles.sendTitle(pls, playerTeam.getColorCode()+ "Démarrage de la partie", "§7dans §b"+timeToStart, 20);
				}
				
			}
			if(timeToStart ==0) { //On rajoute des sons pour le timer et on passe en gm survie;
				pls.playSound(pls.getLocation(), Sound.LEVEL_UP, 10, 1);
				pls.setGameMode(GameMode.SURVIVAL);
			}
		}
		if(timeToStart ==0) { //Lancement du jeu;
			//Suppression des equipes vides
			main.removeUnusedTeam();
			Bukkit.broadcastMessage(main.getConfig().getString("messages.start").replace("&", "§").replace("{timer}", String.valueOf(main.getConfig().getInt("timers.pvp")))); //Message de lancement du jeu
			
			//Attribution des chefs d'équipe
			for(Team team : main.getTeams()) {
				team.setLeader();
				team.getLeader().setMaxHealth(40);
				for(Player gamer : team.getPlayers()) {
					gamer.sendMessage(main.getConfig().getString("features.messages.leader").replace("&", "§").replace("{leader}", team.getLeader().getName()));
				}
			}
			
			//Teleportation des teams en jeu
			double border = main.getConfig().getDouble("border.size");//Recuperation de la taille de la border
			WorldBorder wb = main.getWorldBorder(); //récupère la worldBorder
			wb.setCenter(0.5, 0.5);
			wb.setDamageAmount(1);
			wb.setDamageBuffer(10);
			wb.setWarningDistance(20);
			wb.setSize(border*2);
			
			WorldBorder nwb = main.getNetherWorldBorder(); //récupère la worldBorder
			nwb.setCenter(0.5, 0.5);
			nwb.setDamageAmount(1);
			nwb.setDamageBuffer(10);
			nwb.setWarningDistance(20);
			nwb.setSize(border*2);
			
			for(Team team : main.getTeams()) {
				if(team.getPlayers().size() > 0) {
					Location spawnPoint = safeTeleport(Bukkit.getWorld("world"), border);
					for(Player gamer : team.getPlayers()) { //On teleporte tout les joueurs à cette position
						gamer.teleport(spawnPoint);
						gamer.setSaturation(60);
					}
				}
			}
			//Suppression de la zone du spawn
			try {
				main.loadSchematic("/vide.schematic", 0, 299, 0);
			} catch (Exception e) {
				System.out.println("Impossible de supprimer la schematic");
				e.printStackTrace();
			}
			
			main.setState(State.GRACE); //State passé à farming;
			
			ScoreBoardCycle.actualiseTeam(main);
			
			cancel(); //Pour stopper le timer;
			GameCycle cycle = new GameCycle(main); //Pour le timer du pvp;
			cycle.runTaskTimer(main, 0, 20); //les paramètres;
		}
		timeToStart--; //Update du timer;
	}




	private Location safeTeleport(World world, double border) {
		//Generation d'une location aléatoire comprise sur la map (safe)
		Location safeLocation = null;
		while(safeLocation == null) {
			Random r = new Random();
			double xPos = 2*border*r.nextDouble()-border; //Renvoie la position x située qqpart sur la map, de facon très aléatoire puisque double sera compris entre 0 et 1
			double zPos = 2*border*r.nextDouble()-border;
			Location randomLoc = new Location(world, xPos, 250, zPos); //location random sur la map
			
			//Check dans un rayon de 35 bloc si il n'y a pas d'eau ou de lave (=safeLocation)
			for(int i =0; i<35; i++) {
				for(int j=0; j<35;j++) {
					Location highestBlockLoc = world.getHighestBlockAt(randomLoc.clone().add(i,0,j)).getLocation(); //Recupère le bloc le plus haut la pos du tp random dans un radius potentiellement safe
					//Note: .clone() permet de ne pas modifier la valeur initial d'une location
					highestBlockLoc.add(0.5,0,0.5); //On centre la selection du bloc
					Material underHighestBlock = highestBlockLoc.clone().add(0,-1,0).getBlock().getType(); //Check le bloc en dessous
					if(!(underHighestBlock.equals(Material.STATIONARY_LAVA) || underHighestBlock.equals(Material.STATIONARY_WATER))) {
						safeLocation = highestBlockLoc; 
					}
				}
			}
		}
		safeLocation.add(0,16,0);//On ajoute 16 pour eviter les joueurs bloqués dans le sol
		return safeLocation;
	}
}
