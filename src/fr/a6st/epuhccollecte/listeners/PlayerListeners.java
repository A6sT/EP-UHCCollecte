package fr.a6st.epuhccollecte.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import fr.a6st.epuhccollecte.Main;
import fr.a6st.epuhccollecte.State;
import fr.a6st.epuhccollecte.scoreboard.ScoreBoardCycle;
import fr.a6st.epuhccollecte.game.Autostart;
import fr.a6st.epuhccollecte.features.Team;

public class PlayerListeners implements Listener {

	
	//=============== Partie privée ===================
	
	
	private Main main; //La class principale;

	

	
	//=============== Partie publique ===================
	
	public PlayerListeners(Main main) { //Pour récupérer le main de notre class principale;
		this.main = main;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) { //A chaque join d'un joueur;
		Player player = event.getPlayer(); //On créer une variable player récupérant le joueur;
		
		//Creation du scoreboard pour le joueur
		ScoreBoardCycle.createScoreBoard(player, main.boards);
		player.setScoreboard(main.tabBoard);
		
		//Condition pour le state IDLE
		if(main.isState(State.IDLE))
		{
			Location spawn = new Location(Bukkit.getWorld("world"), 0.450, 231.5 , 0.450, 180, 0); //On créer une variable spawn pour pouvoir y tp le joueur;
			player.teleport(spawn); //On téléporte le joueur au spawn;
			player.getInventory().clear(); //On clear l'inventaire du joueur;
			player.setFoodLevel(20); //On feed le joueur;
			player.setSaturation(800); //Give Saturation infini
			player.setHealth(20); //On heal le joueur;
			player.setMaxHealth(20); //On reset la vie du joueur par defaut;
			
			//Ajoute a la liste des participants
			if(!main.getPlayers().contains(player)) main.getPlayers().add(player); //Sinon si la liste ne contient pas le joueur on ajoute le joueur à la liste;
			player.setGameMode(GameMode.ADVENTURE); //On passe le joueur en gamemode aventure;
			
			//On prévient que le joueur à rejoint
			event.setJoinMessage(main.getConfig().getString("messages.join").replace("&", "§").replace("{player}", player.getDisplayName()).replace("{nbPlayers}", String.valueOf(main.getPlayers().size())));
			
			//Interdire le pvp au spawn
			Bukkit.getWorld("world").setPVP(false); //On annule le pvp au spawn;
			
			//Mise en place des items pour la selection des teams
			player.getInventory().clear();
			
			//Creation de la banner (interactif, ouvre un gui pour les teams)
			ItemStack bannerTeam = new ItemStack(Material.BANNER, 1);
		    BannerMeta meta = (BannerMeta)bannerTeam.getItemMeta();
		    meta.setDisplayName("§6Selectionnez votre team");
		    meta.setBaseColor(DyeColor.WHITE);  //Passe la couleur de la banner en blanc (par defaut, sera modifié a la selection d'une team)
		    bannerTeam.setItemMeta(meta);			
			player.getInventory().addItem(bannerTeam);
			player.updateInventory();
		}
		
		//Condition pour les states non idle
		if(!main.isState(State.IDLE)) { //Si le jeu à déjà démarré
			if(main.getDeconnection().containsKey(player.getDisplayName())) { //Si le joueur c'etait déconnecté
				boolean isAlive = true;
				Player removePlayer = player;
				for(Player joueur : main.getMorts()) {
					if(joueur.equals(player)) {
						isAlive = false;
						removePlayer = joueur;
					}
				}
				main.getMorts().remove(removePlayer);
				main.getMorts().add(player);
				
				removePlayer = player;
				
				//On remplace l'ancien joueur par le nouveau
				Team team = main.getTeams().get(0);
				Team equipe = main.getDeconnection().get(player.getDisplayName());
				
				for(Player gamer : equipe.getPlayers()) {
					if(gamer.equals(player)) {
						removePlayer = gamer;
						team = equipe;
					}
				}
				
				team.removePlayer(removePlayer);
				team.addPlayer(player);
				
				//Si le joueur est en vie
				if(isAlive) { 
					//On remet le joueur en condition de jeu
					player.setGameMode(GameMode.SURVIVAL); //On repasse le joueur en survie
					player.sendMessage("§7Vous vous êtes reconnecter. Evitez de vous deconnecter pour le bon déroulement de la partie.");
					event.setJoinMessage(main.getConfig().getString("messages.reconnect").replace("&", "§").replace("{player}", player.getDisplayName()));
				} 
				
				//Le joueur est mort
				else { 
					player.setGameMode(GameMode.SPECTATOR); //On passe le joueur en spectateur;
					player.sendMessage(main.getConfig().getString("messages.death").replace("&", "§")); //On le prévient que le jeu est déjà lancé;
					event.setJoinMessage(null); //On annule le join message;
				}
				//On supprime le joueur de la hashmap
				main.getDeconnection().remove(player.getDisplayName());
				
			//Le joueur ne c'est jamais connecté	
			} else { 
				player.setGameMode(GameMode.SPECTATOR); //On passe le joueur en spectateur;
				player.sendMessage(main.getConfig().getString("messages.started").replace("&", "§")); //On le prévient que le jeu est déjà lancé;
				event.setJoinMessage(null); //On annule le join message;
			}
			
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		//Evenements lors de la selection des teams
		if(main.isState(State.IDLE))
		{
			//Gestion de l'inventaire de selection de team
			ItemStack item = event.getItem();
			if(item != null && item.getType() == Material.BANNER) //Clic sur la banner
			{
				//Menu de selection des teams
				Inventory inv = Bukkit.createInventory(null, 18, "§eSelection des teams"); //Creer l'inventaire pour la selection de team
				//Setup de l'inventaire
				ItemStack bannerTeam = new ItemStack(Material.BANNER, 1); //Creation de la banniere a placer dans l'inventaire
				int pos = 0; //Pour la pos de la bannière dans l'inventaire
				for(Team team : main.getTeams())
				{
					List<String> lores = new ArrayList<String>();
					BannerMeta meta = (BannerMeta)bannerTeam.getItemMeta(); //Recuperation de la meta de la bannière
					meta.setBaseColor(team.getColor()); //Donne la couleur de la team à la meta de la bannière
					meta.setDisplayName(team.getColorCode() + team.getName()); //Change le nom de la bannière pour le nom de la team ("pas officiel")
					for(Player gamer : team.getPlayers()) //Ajoute un lore a la bannière contenant tout les joueurs de la team
					{
						lores.add("§7- §e"+ gamer.getName());
					}
					meta.setLore(lores); //Ajoute au lore de la bannière le nom des joueurs de la team
					lores.clear(); //Clear le lores pour le reutiliser apres
					bannerTeam.setItemMeta(meta); //Applique la meta à la bannière
					inv.setItem(pos, bannerTeam); //ajoute la bannière à l'inventaire
					pos ++;
				}
				player.openInventory(inv);
			}
			if(item != null && item.getType() == Material.EMERALD && main.getPlayers().size() >= 1) { //Après avoir executé la commande pour se give l'emeraude, si une interaction est enregistrer, la game démarre
				System.out.println("State: Waiting");
				main.setState(State.WAITING);
				
				Autostart start = new Autostart(main); //On créer un objet autostart;
				start.runTaskTimer(main,  0, 20); //On lance l'autostart;
				main.setState(State.STARTING); //et on passe le state en STARTING;
			}
		}
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event)
	{
		//Empecher le joueur de lancer des items dans la phase de selection de team
		if(main.isState(State.IDLE))
		{
			if(event.getItemDrop().getItemStack().getType() == Material.BANNER) //Si le joueur tente de lancer une bannière
			{
				event.setCancelled(true); //On annule l'evenement
				event.getPlayer().updateInventory(); //On actualise son inventaire pour eviter les soucis de visibilité
			}
		}
	}
	
	@EventHandler
	public void onBlockPlaced(BlockPlaceEvent event)
	{
		if(main.isState(State.IDLE)) //Empeche les joueurs de poser des blocs lors de la selection des teams
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(main.isState(State.IDLE)) //Empeche les joueurs de casser des blocs lors de la selection des teams
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event)
	{
		Inventory inv = event.getInventory();
		Player player = (Player)event.getWhoClicked();
		ItemStack current = event.getCurrentItem();
		
		if(inv.getName().equalsIgnoreCase("§eSelection des teams")) //Interaction dans le menu de selection de team
		{
			if(current.getType().equals(Material.BANNER) && current != null) //Si le joueur clic sur une bannière
			{
				for(Team team : main.getTeams()) //On cherche sur quel team le joueur a cliqué pour l'ajouter ou l'enlever d'une team
				{
					BannerMeta meta = (BannerMeta) current.getItemMeta(); //Manip foireuse pour reussir a avoir la couleur de la bannière cliqué
					if(team.getColor() == meta.getBaseColor())
					{
						main.addPlayer(player, team); //Ajout a la team
						ScoreBoardCycle.actualiseTeam(main);
						continue;
					}
					
					if(team.getPlayers().contains(player)) //Suppression du joueur de l'equipe
					{
						team.getPlayers().remove(player);
						ScoreBoardCycle.actualiseTeam(main);
					}
				}
				
				event.setCancelled(true);
				player.closeInventory();
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) { //A chaque leave d'un joueur;
		Player player = event.getPlayer(); //On créer une variable player récupérant le joueur;
		//Durant la phase de selection de team
		if(main.isState(State.IDLE) || main.isState(State.WAITING) || main.isState(State.STARTING)) {
			if(main.getPlayers().contains(player)) { //Si la liste contient le joueur;
				main.getPlayers().remove(player); //On enlève le joueur de la liste;
			}
			//Supprime le joueur de toutes ses teams
			main.removePlayer(player);
		}
		//Durant les autres phases de jeu
		else {
			main.getDeconnection().put(player.getDisplayName(), main.getPlayerTeam(player));
			
			//Verifie si la team du joueur est encore en jeu si elle n'est pas éliminée
			Team equipe = main.getPlayerTeam(player);
			if(main.isTeamAlive(equipe) == false && !main.getDeadTeams().contains(equipe)) { //Si tout les membres de cette team sont éliminées / déconectés
				Bukkit.broadcastMessage("§c[§6EP-UHC§c] - §bLa team "+ equipe.getColorCode() + equipe.getName() + " §best éliminé !");
				main.getDeadTeams().add(equipe); //On supprime la team des equipes participantes
			}
			
			ScoreBoardCycle.actualiseTeam(main);
			main.checkWin(); //On vérifie si une victoire est enclanchée;
		}
		
		//Notifie le depart du joueur
		event.setQuitMessage(main.getConfig().getString("messages.quit").replace("&", "§").replace("{player}", player.getDisplayName()));
		
		//Detruit le scoreboard du joueur
		if(main.boards.containsKey(player)) {
			main.boards.get(player).destroy();
		}
		
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msg = event.getMessage();
		event.setCancelled(true); //On empeche le message d'être envoyé, pour pouvoir le remodeler à notre facon
			
		for(Team equipe : main.getTeams()) { //Les messages de team ne s'envoient que si le joueur est dans une team
			if(equipe.getPlayers().contains(player)) { //On verifie si le joueur appartient a une team 
				if (msg.startsWith("!")) { //Si son message ne commence pas par un ! (pour parler en global)
					Bukkit.broadcastMessage("§b§l! " + equipe.getColorCode() + player.getName() + " §f> " + msg.substring(1)); //Envoie le message sans le !
				} else { //Message normal (envoyé a la team initiale)
					for(Player teamate : equipe.getPlayers()) { //Pour chaque membre de l'équipe
						teamate.sendMessage(equipe.getColorCode() + equipe.getName() + "§7- " + equipe.getColorCode() + player.getName() + " §f> " + msg); //On envoie le message initial aux membres de l'équipe
					}
				}
				return;
			} 
		}
		
		//Si le joueur n'as pas de team
		Bukkit.broadcastMessage("§7(Spec) §f" + player.getName() + " §f> " + msg); //Envoie un faux broadcast, qui contient le message du joueur et le bon format
	}
}
