package fr.a6st.epuhccollecte.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.State;

public class DamageListeners implements Listener {

	
	//=============== Partie privée ===================
	
	
	private Main main; //La class principale;

	

	
	//=============== Partie publique ===================
	public Map<Player, Player> killers = new HashMap<>();
	
	public DamageListeners(Main main) { //Pour récupérer le main de notre class principale;
		this.main=main;
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) { //A chaque dégats;
		if((main.isState(State.IDLE) || main.isState(State.WAITING) || main.isState(State.STARTING) || main.isState(State.GRACE)) && (event.getEntity() instanceof Player)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPvp(EntityDamageByEntityEvent event) { //A chaque dégats par un joueur;
		if((main.isState(State.IDLE) || main.isState(State.WAITING) || main.isState(State.STARTING) || main.isState(State.GRACE)) && (event.getEntity() instanceof Player)) {
			event.setCancelled(true);
		} else {
			Entity victim = event.getEntity(); //Variable victim qui prend la valeur de l'entitée qui à recu les dégats;
			if(victim instanceof Player) { //si la victime est un joueur;
				Player player = (Player)victim; //on change le type d'entité à joueur;
				Player killer = player; //on crée une variable killer de type player;
				if(player.getHealth() <= event.getDamage()) { //si la vie du joueur est inférieur au dégats recus alors on annule le comportement par défault pour que le joueur n'ai pas l'écran de mort;
					if(event.getDamager() instanceof Player) killer = (Player)event.getDamager(); //Si l'assaillant est un joueur alors on passe la variable killer à l'assaillant;
					if(event.getDamager() instanceof Arrow) { //Si l'assaillant a utilisé une flèche, alors le damager est une flèche;
						Arrow arrow = (Arrow) event.getDamager(); //On crée une variable flèche qui prend en valeur la flèche envoyée;
						if(arrow.getShooter() instanceof Player) { //On récupère le shooter de cette flèche et on vérifie que c'est un player;
							killer = (Player) arrow.getShooter(); //On passe le killer au shooter de la flèche;
						}
					}
				}
				killers.put(player, killer);
			}
		}
		
	}
	
	@EventHandler
	public void onDeath(EntityDeathEvent event) { //A la mort d'un joueur
		if(event.getEntity() instanceof Player) {
			Player mort = (Player) event.getEntity();
			Player killer = mort;
			if(killers.containsKey(mort)) {
				killer = killers.get(mort);
			}
			
			if(!killer.equals(mort)) { //Ajout du kill au compteur
	 			main.getPlayerTeam(killer).addKill();
			}
			
			Bukkit.broadcastMessage(main.getConfig().getString("messages.kill").replace("&", "§").replace("{killer}", killer.getDisplayName()).replace("{victim}", mort.getName()));//On affiche un message de mort;
			event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE, 1)); //Ajout d'une pomme dorée a la mort d'un joueur
			main.eliminate(mort); 
		}
	}
}
