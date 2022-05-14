package fr.a6st.epuhccollecte.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.features.Team;

public class CommandRevive implements CommandExecutor {

private Main main;
	
	public CommandRevive(Main main) {
		this.main = main;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 1) {
			Player mort = Bukkit.getPlayer(args[0]); //On stock le joueur mort
			if(main.getMorts().contains(mort)) {
				boolean tped = false;
				for(Team team : main.getTeams()) { //On recherche la team a laquel il appartient
					if(team.getPlayers().contains((mort))){
						Location coord = new Location(Bukkit.getWorld("world"), 0.450, Bukkit.getWorld("world").getHighestBlockYAt(0, 0) , 0.450, 180, 0); //Position de tp par defaut si le joueur etait le dernier en vie de sa team
						
						//On le tp au premier joueur en vie
						for(int i = 0; i< team.getPlayers().size() && tped == false; i++) {
							if(!main.getMorts().contains(team.getPlayers().get(i))) {
								coord = team.getPlayers().get(i).getLocation();
								tped = true;
							}
						}
						
						//On remet le joueur en condition de jeu
						mort.setGameMode(GameMode.SURVIVAL);
						mort.teleport(coord);
						mort.setHealth(mort.getMaxHealth());
						
						//On modifie les listes
						main.getMorts().remove(mort);
						main.getPlayers().add(mort);
						mort.sendMessage("§aVous avez été ressucité. Faites plus attention la prochaine fois!");
						sender.sendMessage("§aLe joueur §e" + mort.getName() + "§a a été ressucité");
					}
				}
				
			} else {
				sender.sendMessage("§cLe joueur ciblé n'est pas mort ou n'existe pas");
			}
		} else {
			sender.sendMessage("Usage: /revive <Joueur>");
		}
		
		return false;
	}

}
