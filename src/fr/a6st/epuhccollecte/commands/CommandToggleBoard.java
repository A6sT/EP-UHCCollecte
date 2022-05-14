package fr.a6st.epuhccollecte.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.scoreboard.ScoreBoardCycle;

public class CommandToggleBoard implements CommandExecutor {

private Main main;
	
	public CommandToggleBoard(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		if(main.boards.containsKey(player)) { //Si un scoreboard contient le joueur: on le desactive
			main.boards.get(player).destroy();
			main.boards.remove(player);
			player.sendMessage("§aScoreboard désactivé");
		} else { //Sinon on le reactive
			ScoreBoardCycle.createScoreBoard(player, main.boards);
			ScoreBoardCycle.actualiseTeam(main);
			player.sendMessage("§aScoreboard activé");
		}
		return false;
	}

}
