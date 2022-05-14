package fr.a6st.epuhccollecte.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.State;

public class CommandUHC implements CommandExecutor {

private Main main;
	
	public CommandUHC(Main main) {
		this.main=main;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Player player = (Player) sender;
		if(main.isState(State.IDLE)) {
			ItemStack emerald = new ItemStack(Material.EMERALD);
			ItemMeta eMeta = emerald.getItemMeta();
			eMeta.setDisplayName("§a§lCliquez pour demarrer l'UHC");
			eMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
			eMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			emerald.setItemMeta(eMeta);
			player.getInventory().setItem(8, emerald);
			player.sendMessage("§aVous avez le pouvoir de démarrer un evenement incroyable entre vos mains"); //Ouai, enfin faut se hyper quoi
			
		} else {
			player.sendMessage("§cLa game a déjà commencé!");
		}
		return false;
	}
	

}
