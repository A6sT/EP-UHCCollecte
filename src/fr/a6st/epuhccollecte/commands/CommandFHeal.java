package fr.a6st.epuhccollecte.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandFHeal implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.setHealth(player.getMaxHealth());
		}
		Bukkit.broadcastMessage("§c[§6EP-UHC§c] - §bUn Final Heal a été effectué");
		return false;
	}

}
