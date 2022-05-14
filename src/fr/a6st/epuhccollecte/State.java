package fr.a6st.epuhccollecte;

public enum State {
	IDLE, //En attente de la commande /uhc;
	WAITING, //En attente du d�but de la partie;
	STARTING, //D�compte, tps;
	GRACE, //Periode d'invincibilit�e de 1 minute;
	FARMING, //Temps avant PVP;
	PVP, //Jusqu'au dernier survivant;
	BORDER, //Depart de la bordure vers +- 50
	FINISH; //Win;
}
