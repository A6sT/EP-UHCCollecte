package fr.a6st.epuhccollecte.tablist;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.State;
import fr.a6st.epuhccollecte.features.Team;

public class ActionBarCycle extends BukkitRunnable {

	
	//=============== Partie privée ===================
	
	
	private Main main; //La class principale;
	
	//=============== Partie publique ===================
	
	public ActionBarCycle(Main main) { //Pour récupérer le main de notre class principale;
		this.main = main;
	}
	

	@Override
	public void run() {
		if(!main.isState(State.IDLE)) {
			for(Team team : main.getTeams()) {
				int deadPlayerPerTeam = 0; //Reinitialise le compteur de mort / team a chaque nouvelle team
				
				//premiere boucle: on compte le nombre de mort / team
				for(Player player : team.getPlayers()) {
					if(main.getMorts().contains(player) || main.getDeconnection().containsKey(player.getDisplayName())) {
						deadPlayerPerTeam += 1;
					}
				}
				
				//Seconde boucle: on affiche l'action bar au joueur
				for(Player player : team.getPlayers()) {
					if(!main.getMorts().contains(player) && !main.getDeconnection().containsKey(player.getDisplayName())) { //Compte le nombre de joueur mort
						if(team.getPlayers().size() - deadPlayerPerTeam > 1) { //Il reste au moin 2 joueurs dans la team
							if(getNearestTeamate(player, team) != null) { //Check si le tracker pointe vers un autre monde
								main.titles.sendActionBar(player, "§7Coéquipier le plus proche: §e" + getNearestTeamate(player, team) + "§7m");
							} else {
								main.titles.sendActionBar(player, "§cVotre coéquipier le plus proche est dans un autre monde");
							}	
						} else {
							main.titles.sendActionBar(player, "§cVous êtes le dernier survivant de votre team");
						}
					}
				}
			}
		}
	}


	private String getNearestTeamate(Player player, Team team) {
		Location playerLoc = player.getLocation();
		int lowestDistance = 1500; //On initialise la lowestDistance avec la plus grande valeur possible pour eviter des problemes
		boolean error = false;
		
		for(Player gamer : team.getPlayers()) {
			if(!gamer.equals(player)) { //On ne doit pas tracker le joueur en question
				if(!(main.getMorts().contains(gamer) && main.getDeconnection().containsKey(player.getDisplayName()))) { //le joueur a track doit encore être en jeu
					if(playerLoc.getWorld() == gamer.getLocation().getWorld()) {
						if(playerLoc.distance(gamer.getLocation()) < lowestDistance) {
							lowestDistance = (int) playerLoc.distance(gamer.getLocation());
						}
					} else {
						error = true;
					}
				}
			}
		}
		if(error && lowestDistance == 1500) { //Si le tracker n'as pas été modifié (autrement dit: si la seul valeur trouvé provient d'un joueur d'une autre dimension
			return null;
		} else { //Cas par defaut
			return Integer.toString(lowestDistance);
		}
	}
}
