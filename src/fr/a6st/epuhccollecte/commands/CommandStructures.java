package fr.a6st.epuhccollecte.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.a6st.epuhccollecte.Main;

public class CommandStructures implements CommandExecutor {

	private Main main;
	
	public CommandStructures(Main main) {
		this.main = main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;
		if(main.getSchemLocs().size() > 0) {
			for(Location loc : main.getSchemLocs()) {
				player.sendMessage("§7x:"+loc.getBlockX()+ "; z:"+loc.getBlockZ());
			}
		} else {
			player.sendMessage("§cUh-oh, il n'y a pas de structures...");
		}
		return false;
	}

}
