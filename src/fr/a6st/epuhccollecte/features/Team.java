package fr.a6st.epuhccollecte.features;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

public class Team {
	
	//=============== Partie classique ===================
	private String name; //A ne pas confondre avec le tag, name affiche un nom de team ("non officiel")
	private String tag; //Permet de reconnaitre la team par une chaine de caractère (peut etre un num§ro ou un nom)
	private DyeColor color; //Associe une couleur a la team (pour les scoreboard)
	private String colorCode; //Color code de la team (pour l'affichage en couleur)
	private int teamKillCount; //Compteur de kill d'une team
	private List<Player> players = new ArrayList<>();
	
	
	//=============== Partie Features ===================
	
	private Random rnd = new Random(); //Initialisation du random pour eviter une surcharge mémoire
	private int collectScore; // Score de collecte de la team sur une période de collecte
	private Player leader; //Leader random de la team definit en debut de partit
	
	
	//=============== Methodes classiques ===================
	
	public Team(String name, String tag, DyeColor color, char colorCode) //Constructeur de team classique
	{
		this.name = name;
		this.tag = tag;
		this.color = color;
		this.colorCode = "§" + colorCode;
		this.teamKillCount = 0;
	}

	public String getName()
	{
		return this.name;
	}
	
	public void setName(String newName) //Changer le nom de la team
	{
		this.name = newName;
	}
	
	public String getTag()
	{
		return this.tag;
	}
	
	public DyeColor getColor()
	{
		return this.color;
	}
	
	public String getColorCode()
	{
		return this.colorCode;
	}
	
	public int getTeamKill() {
		return teamKillCount;
	}
	
	public List<Player> getPlayers()
	{
		return players;
	}
	
	public Player getFirstPlayer()
	{
		if(players.size() >0)
		{
			return players.get(0);
		}
		else
		{
			return null;
		}
	}
	
	public int getSize()
	{
		return players.size();
	}
	
	public void addPlayer(Player player)
	{
		players.add(player);
	}
	
	public void removePlayer(Player player)
	{
		players.remove(player);
	}
	
	public void addKill() {
		this.teamKillCount += 1;
	}
	
	//=============== Methodes Features ===================
	
	public Player getLeader() {
		return leader;
	}
	
	public void setLeader() {
		leader = players.get(rnd.nextInt(getSize()));
	}
	
	public boolean isLeader(Player player) {
		boolean chef = false;
		if(player.equals(leader)) {
			chef = true;
		}
		return chef;
	}
	
	public int getScoreCollecte() {
		return collectScore;
	}
	
	public void addScoreCollecte(int scoreToAdd) {
		collectScore += scoreToAdd;
	}

	public void setScoreCollecte(int score) {
		collectScore = score;
		
	}
}
