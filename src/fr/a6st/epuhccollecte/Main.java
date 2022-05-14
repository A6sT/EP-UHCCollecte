package fr.a6st.epuhccollecte;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

import fr.a6st.epuhccollecte.commands.CommandFHeal;
import fr.a6st.epuhccollecte.commands.CommandMypos;
import fr.a6st.epuhccollecte.commands.CommandRevive;
import fr.a6st.epuhccollecte.commands.CommandStructures;
import fr.a6st.epuhccollecte.commands.CommandTN;
import fr.a6st.epuhccollecte.commands.CommandToggleBoard;
import fr.a6st.epuhccollecte.commands.CommandUHC;
import fr.a6st.epuhccollecte.listeners.DamageListeners;
import fr.a6st.epuhccollecte.listeners.FeaturesListeners;
import fr.a6st.epuhccollecte.listeners.GlobalListeners;
import fr.a6st.epuhccollecte.listeners.PlayerListeners;
import fr.a6st.epuhccollecte.scoreboard.ScoreBoardCycle;
import fr.a6st.epuhccollecte.scoreboard.ScoreboardSign;
import fr.a6st.epuhccollecte.tablist.ActionBarCycle;
import fr.a6st.epuhccollecte.tablist.TabListCycle;
import fr.a6st.epuhccollecte.features.Team;
import fr.a6st.epuhccollecte.features.collecte.Collecte;
import fr.a6st.epuhccollecte.tablist.Title;

//import fr.A6sT.epuhc.listeners.DamageListeners;
//import fr.A6sT.epuhc.listeners.PlayerListeners;


@SuppressWarnings("deprecation")
public class Main extends JavaPlugin{
	
	
	//=============== Partie privÃ©e ===================
	
	//Partie gestion global des joueurs
	private List<Player> players = new ArrayList<>(); //Liste des joueurs;
	private List<Player> morts = new ArrayList<>(); //Liste des joueurs morts
	private Map<String, Team> deconnection = new HashMap<>(); //Liste des joueurs dÃ©connectÃ©s
	
	//Partie gestion des teams
	private List<Team> team = new ArrayList<>(); //Liste des teams en vie
	private List<Team> deadTeam = new ArrayList<>(); //Listes des teams Ã©liminÃ©es
	
	//Partie gesetion des states
	private State state; //State de la partie : WAITING, STARTING, FARMING, PVP, FINISH;

	//Mise en place de la worldBorder
	private WorldBorder wb;
	private WorldBorder nwb;
	
	//=============== Partie publique ===================
	
	//Partie gestion des scoreboards
	public Map<Player, ScoreboardSign> boards = new HashMap<>();
	public Scoreboard tabBoard;
	
	//Partie gestion des titles
	public Title titles = new Title();
	
	//=============== Partie features ===================
	
	public Collecte collecte = new Collecte(false); //Initialisation d'une fausse collecte
	private List<ItemStack> items = new ArrayList<ItemStack>();
	private List<ItemStack> collecteItemList = new ArrayList<ItemStack>();
	private List<Location> schemLocs = new ArrayList<Location>();
	
	@Override
	public void onEnable() { //Le plugin se lance;
		saveDefaultConfig(); //Sauvegarde la configuration par default;
		setState(State.IDLE);
		wb = Bukkit.getWorld("world").getWorldBorder();
		nwb = Bukkit.getWorld("world_nether").getWorldBorder();
		getCommand("uhc").setExecutor(new CommandUHC(this));
		getCommand("tn").setExecutor(new CommandTN(this));
		getCommand("revive").setExecutor(new CommandRevive(this));
		getCommand("fheal").setExecutor(new CommandFHeal());
		getCommand("pos").setExecutor(new CommandMypos(this));
		getCommand("toggleboard").setExecutor(new CommandToggleBoard(this));
		getCommand("structures").setExecutor(new CommandStructures(this));
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new GlobalListeners(this), this);
		pm.registerEvents(new PlayerListeners(this), this);
		pm.registerEvents(new DamageListeners(this), this);
		
		pm.registerEvents(new FeaturesListeners(this), this);

		
		Bukkit.getWorld("world").setDifficulty(Difficulty.NORMAL);
		Bukkit.getWorld("world").setMonsterSpawnLimit(250);
		Bukkit.getWorld("world").setAnimalSpawnLimit(75);
		
		//Creation des teams
		DyeColor[] dyeList = DyeColor.values(); //Creation d'une liste contenant toutes les couleurs de colorants
		String colorCodeList = "f65bead8795162c0"; //Code couleur basÃ© sur la generation des teams
		for(int i = 0;i < dyeList.length; i++){
			String name  = "Team " + (i+1); //Donne un nom par defaut a la team, ici, la couleur
			DyeColor color = dyeList[i];
			char colorCode = colorCodeList.charAt(i);
			String tag = Integer.toString(i);
			//Ajout a la liste global de team
			team.add(new Team(name, tag, color, colorCode));
		}
		
		//Creation des recipes
		ShapedRecipe beaconRecipe = new ShapedRecipe(new ItemStack(Material.BEACON));
		beaconRecipe.shape("AAA", "ABA", "CCC");
		beaconRecipe.setIngredient('A', Material.GLASS);
		beaconRecipe.setIngredient('B', Material.DIAMOND_BLOCK);
		beaconRecipe.setIngredient('C', Material.OBSIDIAN);
		Bukkit.addRecipe(beaconRecipe);
		
		//Actualisation du scoreboard
		
		ScoreBoardCycle sbCycle = new ScoreBoardCycle(this);
		sbCycle.runTaskTimer(this, 0, 20);
		
		//Configuration de la tablist
		tabBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		/*Objective tablistObjective = tabBoard.registerNewObjective("tabhealth", "health");
		tablistObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		Objective belowNameObjective = tabBoard.registerNewObjective("belowhealth", "health");
		belowNameObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		belowNameObjective.setDisplayName("❤");*/
		
		//Actualisation de la tablist
		TabListCycle tabList = new TabListCycle(this);
		tabList.runTaskTimer(this, 0, 100);
		
		//Actualisation de l'action bar
		ActionBarCycle actionBar = new ActionBarCycle(this);
		actionBar.runTaskTimer(this, 0, 10);
		
		//Ajout des schematics au demarrage du serveur
		try {
			//=============== Partie classique ===================
			//loadSchematic("/cage.schematic", 0, 299, 0);
			
			//=============== Partie features ===================
			//Generation des 4 structures en +- 250; +-250
			System.out.println("Starting pasting schematic...");
			for(int x= -250; x <= 250; x+= 500) {
				for(int z = -250; z <= 250; z+= 500) {
					//Récupère le bloc le plus haut aux coordonnées x;z
					Location centerBlockLoc = Bukkit.getWorld("world").getHighestBlockAt(x ,z).getLocation();
					int y = (int) centerBlockLoc.getY();
					boolean locationFound = false;
					//Si le bloc le plus haut est sur un arbre, réduit la hauteur de 1 jusqu'a paste sur le sol
					while(locationFound == false) {
						if(centerBlockLoc.getBlock().getType().equals(Material.LEAVES) || 
						   centerBlockLoc.getBlock().getType().equals(Material.LOG) || 
						   centerBlockLoc.getBlock().getType().equals(Material.AIR)) {
							y--;
							centerBlockLoc = new Location(Bukkit.getWorld("world"), x,y,z);
						} else {							
							locationFound = true;
						}
					}
					
					//Paste la schematic si elle est en dehors de l'eau et qu'elle n'existe pas déja
					System.out.println("Pasting schematic at: x"+x+";z"+z);
					loadSchematic("/collecte.schematic", x, y, z);
					schemLocs.add(centerBlockLoc);
				}
			}
			System.out.println("Done !");
		} catch (Exception e) {
			System.out.println("Impossible de load la schematic");
			e.printStackTrace();
		}
		
		//Initialisation de la liste des items present dans items.txt
		try {
		      InputStream is = Collecte.class.getResourceAsStream("/items.txt");
		      InputStreamReader isr = new InputStreamReader(is);
		      BufferedReader br = new BufferedReader(isr);
		      String line;
		      while ((line = br.readLine()) != null) {
		    	  items.add(new ItemStack(Material.valueOf(line))); 
		      }
		      br.close();
		      isr.close();
		      is.close();
		} catch (IOException e) {
		      e.printStackTrace();
		} 
	}
	
	public void loadSchematic(String name, int x, int y, int z) throws Exception
	{		
        	File dir = new File(this.getDataFolder() + File.separator + name); //Schematic a paste
    		Vector pastePos = new Vector(x,y,z); // Position du paste
    		Location loc = new Location(Bukkit.getWorld("world"), 1, 1, 1); //Creer une loc pour en recuprer le monde dans la session
    		
    		WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
    		EditSession session = we.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(loc.getWorld()), -1); //Creer la session ou l'on pastera la schematic
    		
    		CuboidClipboard clipboard = MCEditSchematicFormat.getFormat(dir).load(dir);
    		clipboard.paste(session, pastePos, false);
        
	}
	
	public void setState(State state) { //MÃ©thode setState pour changer le state en dehors de la class principale;
		this.state=state;
	}
	
	public boolean isState(State state) { //Test State en dehors de la class principale;
		return this.state==state;
	}
	
	public State getState() {
		return state;
	}
	
	public List<Player> getPlayers(){ //getPlayers en dehors de la class principale;
		return players;
	}
	
	public List<Player> getMorts(){ //getMorts en dehors de la class principale
		return morts;
	}
	
	public Map<String, Team> getDeconnection(){ //getDeconnection en dehors de la class principale
		return deconnection;
	}
	
	public void addPlayer(Player player, Team team) //addPlayer en dehors de la class principale;
	{
		if(team.getPlayers().contains(player)) //Le joueur est dÃ©ja dans l'equipe
		{
			player.sendMessage("§cVous appartenez déjà à cette équipe");
			return; //Fin de la methode
		}	
		//Si la team est complete:
		/*if(team.getSize() >= tailleMax)
		  {
		      player.sendMessage("Â§cLa team est complÃ¨te");
		      return;
		  }*/
		 
		team.addPlayer(player);
		player.sendMessage("§aVous avez rejoint l'équipe " + team.getColorCode() + team.getName());
		//Changement de la couleur de la banniÃ¨re dans l'inventaire
		BannerMeta meta = (BannerMeta)player.getInventory().getItem(0).getItemMeta(); //Recupere la meta de la banniÃ¨re dans l'inventaire
		meta.setBaseColor(team.getColor());
		meta.setDisplayName("§6Vous êtes dans la team "+ team.getColorCode() + team.getName());
		player.getInventory().getItem(0).setItemMeta(meta); //Applique la nouvelle couleur a la banniÃ¨re dans l'inventaire
		player.updateInventory(); //Update l'inventaire du joueur (pour eviter les soucis de visibilitÃ©)
	}
	
	public void removePlayer(Player player) //removePlayer en dehors de la class principale;
	{
		for(Team Team : team) //Check chaque team pour voir si le joueur est dedans
		{
			if(Team.getPlayers().contains(player)) //Si la team contient le joueur
			{
				Team.removePlayer(player); //On supprime le joueur
			}
		}
	}
	
	public void removeUnusedTeam() { //Suppression des equipes sans joueurs
		List<Team> tempTeam = new ArrayList<>();
		for(Team equipe : team) {
			if(equipe.getPlayers().size() == 0) { //Si la team ne participe pas
				tempTeam.add(equipe); //on ajoute la team des equipes a supprimer a une liste
			}
		}
		
		for(Team equipe : tempTeam) { //Pour toute les teams sans joueurs
			team.remove(equipe); //On les supprimes de la liste des equipes participantes
		}
	}
	
	public List<Team> getTeams() //getTeams en dehors de la class principale;
	{
		return team;
	}
	
	public List<Team> getDeadTeams() { //getDeadTeams en dehors de la class principale
		return deadTeam;
	}
	
	public Team getPlayerTeam(Player player) { //getPlayerTeam en dehors de la class principale
		Team playerTeam = null;
		for(Team equipe : team) {
			if(equipe.getPlayers().contains(player)) {
				playerTeam = equipe;
			}
		}
		return playerTeam;
	}
	
	public WorldBorder getWorldBorder() { //getworldBorder en dehors de la class principale
		return wb;
	}
	
	public WorldBorder getNetherWorldBorder() { //getworldBorder en dehors de la class principale
		return nwb;
	}

	public void eliminate(Player player) { //Sur Ã©limination d'un joueur Ã  cause d'une dÃ©connection ou d'une mort;
		if(players.contains(player)) { //test si l'array contient toujours le joueur, pour ensuite l'enlever;
			players.remove(player); 
			for(Player gamer : Bukkit.getOnlinePlayers()) {
				gamer.playSound(gamer.getLocation(), Sound.AMBIENCE_THUNDER, 10, 1);
			}
		}
		morts.add(player);
		player.setGameMode(GameMode.SPECTATOR); //passe le joueur en gamemode spectateur;
		player.sendMessage(getConfig().getString("messages.death").replace("&", "§")); //envoie un message au joueur;
		
		Team equipe = getPlayerTeam(player);
		if(isTeamAlive(equipe) == false && !getDeadTeams().contains(equipe)) { //Si tout les membres de cette team sont Ã©liminÃ©es / dÃ©conectÃ©s et que la team n'est pas dÃ©ja Ã©liminÃ©e
			Bukkit.broadcastMessage("§c[§6EP-UHC§c] - §bLa team "+ equipe.getColorCode() + equipe.getName() + " §best éliminé !");
			deadTeam.add(equipe); //On ajoute la team a la liste des equipes mortes
			for(Player gamer : Bukkit.getOnlinePlayers()) {
				gamer.playSound(gamer.getLocation(), Sound.WITHER_SPAWN, 10, 1);
			}
		}
		ScoreBoardCycle.actualiseTeam(this);
		checkWin(); //exÃ©cute la mÃ©thode checkWin;
	}

	public boolean isTeamAlive(Team team) {
		boolean isPlaying = true;
		int occurence = 0;
		for(Player gamer : team.getPlayers()) {
			if(getMorts().contains(gamer) || getDeconnection().containsKey(gamer.getDisplayName())) {
				occurence += 1; //On compte le nombre joueur Ã©liminÃ©s ou dÃ©connectÃ©
			}
		}
		if(occurence >= team.getPlayers().size() || deadTeam.contains(team)) { //Si le nombre de joueur Ã©liminÃ© est le meme que le nombre de personne dans la team
			isPlaying = false;
		}
		return isPlaying;
	}
	
	public void checkWin() { //test si le joueur gagne la partie;
		if(team.size() == deadTeam.size()+1 && !isState(State.FINISH)) { //si il ne reste qu'une seul team et que la game n'est pas dÃ©ja fini
			Team winner = team.get(0); //RÃ©cupÃ¨re une team par defaut (sera modifiÃ© pour Ãªtre la team gagnante juste aprÃ¨s)
			
			//RÃ©cupÃ¨re la team gagnante
			for(Team teamAlive : team) {
				if(!deadTeam.contains(teamAlive)) { //Si la liste des teams morte ne contient pas une team, c'est que c'est l'Ã©quipe gagnante
					winner = teamAlive;
				}
			}
			Bukkit.broadcastMessage(getConfig().getString("messages.win").replace("&", "§").replace("{team}", winner.getColorCode() + winner.getName())); //Envoie d'un message;
			
			String joueurGagnants = "§7";
			for(Player gagnant : winner.getPlayers()) {
				joueurGagnants += gagnant.getName() + " ";
			}
			for(Player player : Bukkit.getOnlinePlayers()) {
				player.sendTitle("§bVictoire de l'équipe " + winner.getColorCode() + winner.getName() + "§b!", joueurGagnants);
			}
			setState(State.FINISH);
		}
	}

	//=============== Partie Pratique ===================
	public String convertTimer(int timer) {
		String tempsRestant = "";
		int h = timer / 3600;
		if(h != 0) {
			tempsRestant += h+"h";
			timer -= h*3600;
		}
		int m = timer / 60;
		if((m != 0) || (m == 0 && h>0)) { //Lorsqu'il reste 1h, on affichera quand meme les minutes (1h00m01 sec par exemple)
			if(m <10) {
				tempsRestant +="0";
			}
			tempsRestant += m+":";
		}
		int s = timer % 60;
		if(s < 10) {
			tempsRestant +="0";
		}
		tempsRestant += s+"s";
		return tempsRestant;
	}
	
	//=============== Partie features ===================
	public List<ItemStack> getItemList(){
		return items;
	}
	
	public Collecte getCollecte() {
		return collecte;
	}
	
	public List<ItemStack> getCollecteItemList(){
		return collecteItemList;
	}
	
	public void setCollecte(Collecte collecte) {
		this.collecte = collecte;
	}
	
	public boolean isLeaderAlive(Team team) {
		boolean mort = false;
		if(getMorts().contains(team.getLeader())) {
			mort = true;
		}
		return mort;
	}
	
	public List<Location> getSchemLocs(){
		return schemLocs;
	}
}
