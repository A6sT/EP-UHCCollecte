package fr.a6st.epuhccollecte.tablist;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

public class Title {

	public void sendTitle(Player player, String title, String subtitle, int ticks){
        IChatBaseComponent baseTitle = ChatSerializer.a("{\"text\": \"" + title + "\"}"); //Convertit le title en json (pour pouvoir l'afficher)
        IChatBaseComponent baseSubTitle = ChatSerializer.a("{\"text\": \"" + subtitle + "\"}"); //De meme pour le subTitle
        
        //On set les titles dans des packets
        PacketPlayOutTitle titlepacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, baseTitle);
        PacketPlayOutTitle subtitlepacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, baseSubTitle);
        
        //Convertir le joueur en CraftPlayer (pour supporter les NMS), auquel on envoie les packet qui contiennent les titles
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(titlepacket);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(subtitlepacket);
        
        sendTime(player, ticks);
    }
   
    private void sendTime(Player player, int ticks){
        PacketPlayOutTitle titlepacket = new PacketPlayOutTitle(EnumTitleAction.TIMES, null, 20, ticks, 20);
        
        //Envoyer le packet au joueur
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(titlepacket);
    }
   
    public void sendActionBar(Player player, String message){
        IChatBaseComponent basetitle = ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat packet = new PacketPlayOutChat(basetitle, (byte)2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
