package fr.a6st.epuhccollecte;

public enum State {
	IDLE, //En attente de la commande /uhc;
	WAITING, //En attente du début de la partie;
	STARTING, //Décompte, tps;
	GRACE, //Periode d'invincibilitée de 1 minute;
	FARMING, //Temps avant PVP;
	PVP, //Jusqu'au dernier survivant;
	BORDER, //Depart de la bordure vers +- 50
	FINISH; //Win;
}
