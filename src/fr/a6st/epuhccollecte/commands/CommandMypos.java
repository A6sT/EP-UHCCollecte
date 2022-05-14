package fr.a6st.epuhccollecte.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.features.Team;

public class CommandMypos implements CommandExecutor {

private Main main;
	
	public CommandMypos(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		Team team = main.getPlayerTeam(player);
		for(Player gamer : team.getPlayers()) {
			gamer.sendMessage(team.getColorCode() + team.getName() + "§7- " + team.getColorCode() + player.getName() + " §f> x:" + player.getLocation().getBlockX() + " z:" + player.getLocation().getBlockZ());
		}
		return false;
	}

}
