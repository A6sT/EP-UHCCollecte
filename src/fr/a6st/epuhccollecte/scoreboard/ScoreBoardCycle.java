package fr.a6st.epuhccollecte.scoreboard;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.game.GameCycle;
import fr.a6st.epuhccollecte.features.Team;

public class ScoreBoardCycle extends BukkitRunnable{

	
	//=============== Partie priv§e ===================
	
	
	private Main main; //La class principale;

	

	
	//=============== Partie publique ===================
	
	
	public ScoreBoardCycle(Main main) { //Pour r§cup§rer le main de notre class principale;
		this.main=main;
	}

	
	
	@Override
	public void run(){
		for(Entry<Player, ScoreboardSign> sign : main.boards.entrySet()) {	
			//Permet d'actualiser la ligne des timers (ligne qui change le + souvent)
			int timer = GameCycle.timer; //On recup§re la valeur du timer de gameCycle
			String tempsRestant = main.convertTimer(timer); //Convertit timer en une dur§e affichable dans le scoreboard
			
			switch(main.getState()) {
			//Scoreboard lors de la phase d'attente
			case IDLE:
				sign.getValue().setLine(14, "§e§lEn ligne§e: §a " + Bukkit.getOnlinePlayers().size() + "§7/50"); //Joueurs connectés
				break;
				
			//Scoreboard lors de la phase d'invincibilitée
			case GRACE:
				sign.getValue().setLine(13, "§aInvincibilité: §b" + tempsRestant);
				sign.getValue().setLine(14, "§aJoueurs restant: §e" + main.getPlayers().size()); //Joueurs en vie
				break;
			
			//Scoreboard lors de la phase de farm
			case FARMING:
				sign.getValue().setLine(13, "§cPvP: §b" + tempsRestant);
				sign.getValue().setLine(14, "§aJoueurs restant: §e" + main.getPlayers().size()); //Joueurs en vie
				break;
				
			//Scoreboard lors de la phase de pvp
			case PVP:
				sign.getValue().setLine(13, "§eBordure: §b" + tempsRestant);
				sign.getValue().setLine(14, "§aJoueurs restant: §e" + main.getPlayers().size()); //Joueurs en vie
				break;
			
			//Scoreboard lors de la réduction de la bordure
			case BORDER:
				sign.getValue().setLine(13, "§eBordure: §b±" + (int)main.getWorldBorder().getSize()/2); //Affiche la vrai taille de la bordure
				sign.getValue().setLine(14, "§aJoueurs restant: §e" + main.getPlayers().size()); //Joueurs en vie
				break;
				
			default:
				//Ne doit jamais être utilisé
				break;
			}
			
			//Scoreboard lors d'une phase de collecte
			if(main.collecte.isAlive()) {
				sign.getValue().setLine(11, "§aTemps restant: §b"+ main.convertTimer(main.getCollecte().getTime()));
			}
		}
	}

	public static void createScoreBoard(Player player, Map<Player, ScoreboardSign> boards) {
		ScoreboardSign sb = new ScoreboardSign(player, "§6§lEP-UHC");
        sb.create();
        
        //Lignes par defaut (ne changent pas de la game)
        sb.setLine(0, "§6§lUHC§6: §bCollecte");
        sb.setLine(1, "§b "); //Ligne d'a§ration pr§definit
        
        /* Lignes pas par defaut
        //On alloue le plus de lignes possible pour les team (si ce sont les memes lignes, elles ne sont pas affich§) (autrement dit, si elles restent vides)
        sb.setLine(2, ""); //Premiere utilisation: le joueur rejoint une team : nom de la team
        sb.setLine(3, ""); //Premiere utilisation: le joueur rejoint une team : Leader de la team
        sb.setLine(4, ""); //Premiere utilisation: le joueur rejoint une team : 2eme joueur de la team
        sb.setLine(5, ""); //Premiere utilisation: le joueur rejoint une team : 3eme joueur de la team
        sb.setLine(6, ""); //Premiere utilisation: le joueur rejoint une team : 4eme joueur de la team (/!\ ne pas oublier de check lors d'un changement de team)
        sb.setLine(7, ""); //Premiere utilisation: le joueur rejoint une team : 5eme joueur de la team
        sb.setLine(8, ""); //Premiere utilisation: le joueur rejoint une team : 5eme joueur de la team
        
        sb.setLine(9, "§c "); //Premiere utilisation: le joueur rejoint une team : ligne d'aeration
        
        //Autres lignes (changent beaucoup en fonction de la periode du jeu)
        //Lignes par defaut = en State IDLE
        sb.setLine(10, ""); //Premiere utilisation: EVENT : Score de l'§quipe
        sb.setLine(11, ""); //Premiere utilisation: EVENT : Temps avant fin d'event
        
        sb.setLine(12, "§d§l ");//Premiere utilisation: EVENT : Aeration
        
        sb.setLine(13, ""); //Premiere utilisation: COMMUNE : temps restant avant prochaine phase
        */
        sb.setLine(14, "§e§lEn ligne§e: §a " + Bukkit.getOnlinePlayers().size() + "§7/30"); //Par defaut: joueurs connect§s
        
        //On ajoute le scoreboard au joueur
        boards.put(player, sb);
		
	}
	
	public static void actualiseTeam(Main main) { //Actualise la partie team du scoreboard
		for(Team team : main.getTeams()) {
			//Actualise les joueurs de la team
			for(Player playerboard : team.getPlayers()) {
				if(main.boards.containsKey(playerboard)) {
					
					//Supprime les joueurs
					for(int i = 3; i<9; i++) {
						main.boards.get(playerboard).setLine(i, "§t "); //Annule l'affichage de la ligne (car la ligne 1 contient d§ja cette valeur)
					}
					
					//Affiche les joueurs de la team du playerboard
					for(int i = 0; i<team.getPlayers().size(); i++) {
						String offline = "";
						Player gamer = team.getPlayers().get(i);
						if(main.getDeconnection().containsKey(gamer.getDisplayName()) || main.getMorts().contains(gamer)){
							offline = "§m";
						}
						main.boards.get(playerboard).setLine(3+i, " §7- "+ offline + gamer.getDisplayName());
					}
				}	
				//Actualise le nom de la team et ajoute l'aeration après les membres de la team
				for(Player joueur : team.getPlayers()) {
					if(main.boards.containsKey(joueur)) {
						main.boards.get(joueur).setLine(2, "§6Equipe: " + team.getColorCode()+team.getName() + " §7("+ team.getTeamKill() + ")"); //Nom de la team
						main.boards.get(joueur).setLine(9, "§t ");
						
						if(main.collecte.isAlive()) { //Actualise le score de la team lors de l'event de collecte
							main.boards.get(joueur).setLine(10, "§aScore: §e"+ team.getScoreCollecte());
							main.boards.get(joueur).setLine(12, "§d§l");
						} else {
							main.boards.get(joueur).removeLine(10);
							main.boards.get(joueur).removeLine(11);
							main.boards.get(joueur).removeLine(12);
						}
					}
				}
			}	
		}
	}
}

