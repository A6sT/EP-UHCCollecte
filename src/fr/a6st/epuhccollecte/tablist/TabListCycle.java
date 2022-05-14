package fr.a6st.epuhccollecte.tablist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.features.Team;

public class TabListCycle extends BukkitRunnable {

	
	//=============== Partie privée ===================
	
	
	private Main main; //La class principale;
	
	//=============== Partie publique ===================
	
	public TabListCycle(Main main) { //Pour récupérer le main de notre class principale;
		this.main = main;
	}
	

	@Override
	public void run() {
		for(Player joueur : Bukkit.getOnlinePlayers()) {
			Team team = main.getPlayerTeam(joueur);
			String offline = "";
			String teamColor = "";
			if(main.getMorts().contains(joueur)) { //Si le joueur n'est plus en jeu
				offline += "§m";
			}
			/*ScoreboardManager manager = Bukkit.getScoreboardManager();
			Scoreboard board = manager.getNewScoreboard();
			Objective objective = board.registerNewObjective("showhealth", "health");
			objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
			objective.setDisplayName("/ 20");*/
			
			if(team != null) {
				teamColor = team.getColorCode();
			}
			joueur.setPlayerListName(teamColor + offline + joueur.getName());
			
		}	
	}
}
