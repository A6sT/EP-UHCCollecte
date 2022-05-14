package fr.a6st.epuhccollecte.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BannerMeta;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.State;
import fr.a6st.epuhccollecte.scoreboard.ScoreBoardCycle;
import fr.a6st.epuhccollecte.features.Team;

public class CommandTN implements CommandExecutor {
	
	private Main main;
	
	public CommandTN(Main main) {
		this.main=main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		if(main.isState(State.IDLE)){
			if(args.length >0){
				boolean isLead = false;
				StringBuilder newName = new StringBuilder(); //On reconstruit ce que le leader a écrit
				for(String part : args) {
					newName.append(part + " ");
				}
				if(newName.length() < 16) { //Verification de la taille du nom de team
					for(Team team : main.getTeams()){
						if(team.getFirstPlayer() == player){ //Si le joueur est premier d'une team (le "chef" de la team)
							team.setName(newName.toString()); //On change le nom de la team
							player.sendMessage("§eNom de l'équipe changé pour: " + team.getColorCode() + team.getName());
							isLead = true; //Pour eviter de lancer un message d'erreur
							for(Player gamer : team.getPlayers()) //Actualisation du nom de la bannière pour chaque membre de l'equipe
							{
								BannerMeta meta = (BannerMeta)gamer.getInventory().getItem(0).getItemMeta(); //Recupere la meta de la bannière dans l'inventaire
								meta.setBaseColor(team.getColor());
								meta.setDisplayName("§6Vous êtes dans la team "+ team.getColorCode() + team.getName());
								gamer.getInventory().getItem(0).setItemMeta(meta); //Applique la nouvelle couleur a la bannière dans l'inventaire
								gamer.updateInventory(); //Update l'inventaire du joueur (pour eviter les soucis de visibilité)
							}
						}
						ScoreBoardCycle.actualiseTeam(main);
					}
				}
				else {
					player.sendMessage("§cLe nom de team entré est trop grand! (15 caractères au max)");
				}
				if(!isLead) {
					player.sendMessage("§cVous n'êtes pas le leader de votre team");
				}
			}
			else {
				player.sendMessage("§cFormat: /tn <Nouveau nom de team>");
			}
		}
		else{
			player.sendMessage("§cVous ne pouvez plus changer le nom de votre team");
		}
		return false;
	}
}
