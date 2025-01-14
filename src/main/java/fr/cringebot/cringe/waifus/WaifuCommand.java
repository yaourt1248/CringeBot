package fr.cringebot.cringe.waifus;

import fr.cringebot.BotDiscord;
import fr.cringebot.cringe.escouades.Squads;
import fr.cringebot.cringe.objects.SelectOptionImpl;
import fr.cringebot.cringe.objects.StringExtenders;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static fr.cringebot.BotDiscord.isMaintenance;

public class WaifuCommand {
	private static Message msg;
	public static Lock waifuLock = new ReentrantLock();
	private static final int SECOND = 1000;
	private static final int MINUTE = 60 * SECOND;
	private static final int HOUR = 60 * MINUTE;

	public static void CommandMain(Message msg) throws ExecutionException, InterruptedException {

		if (msg.getContentRaw().split(" ").length == 1) {
			newWaifu(msg);
			return;
		}
		if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("add"))
			addwaifu(msg);
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("list")){
			EmbedBuilder eb = new EmbedBuilder().setTitle("Listes des waifus").setDescription("chargement...");
			if (msg.getContentRaw().split(" ").length > 2)
				eb.setAuthor(msg.getContentRaw().substring(">Waifu list ".length()));
			Message ls = msg.getChannel().sendMessageEmbeds(eb.build()).complete();
			ls.addReaction("◀️").and(ls.addReaction("▶️")).queue();
			listwaifu(ls);
		}
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("info"))
			infowaifu(msg);
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("setdescription"))
			setDescription(msg);
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("delete"))
			delwaifu(msg);
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("setname"))
			setName(msg);
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("reset"))
			reset(msg);
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("setimage"))
			setImage(msg);
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("setorigin"))
			setOrigin(msg);
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("trade"))
			trade(msg);
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("release"))
			release(msg);
		else if (msg.getContentRaw().split(" ")[1].equalsIgnoreCase("stats"))
			stats(msg);
		else
			newWaifu(msg);

	}

	private static void stats(Message msg) {
		ArrayList<Waifu> waifuList = Waifu.getAllWaifu();
		EmbedBuilder eb = new EmbedBuilder();
		if (">waifu stats ".length() < msg.getContentRaw().length()) {
			waifuList.removeIf(waifu -> !waifu.getOrigin().equalsIgnoreCase(msg.getContentRaw().substring(">waifu stats ".length())));
			eb.setAuthor(msg.getContentRaw().substring(">waifu stats ".length()));
		} else
			eb.setAuthor("général");
		ArrayList<String> origins = new ArrayList<>();
		HashMap<String, Integer> content = new HashMap<>();
		int disponible = 0;
		for (Waifu w : waifuList) {
			if (w.getOwner() == null)
				disponible++;
			else
			{
				if (!content.containsKey(w.getOwner()))
					content.put(w.getOwner(), 1);
				else
					content.put(w.getOwner(), content.get(w.getOwner()) + 1);
			}
			if (!origins.contains(w.getOrigin()))
				origins.add(w.getOrigin());
		}

		eb.setTitle("Waifu stat");
		eb.setDescription("Nombres de waifus encore disponible : "+ disponible + "/"+waifuList.size()+"\n\norigines disponible :\n");
		for (String origin : origins)
			eb.appendDescription(origin + " / ");
		eb.appendDescription("\n");
		for (String id : content.keySet()){
			eb.appendDescription(msg.getGuild().getMemberById(id).getAsMention() +" : "+content.get(id) +"\n");
		}
		msg.getChannel().sendMessageEmbeds(eb.build()).queue();
	}

	private static void trade(Message msg) {
		if (!msg.getChannel().getId().equals(BotDiscord.FarmingSalonId)) {
			msg.getChannel().sendMessage("Mové salon comme dirait l'autre").queue();
			return;
		}
		if (msg.getContentRaw().split(" ").length != 3){
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.red);
			eb.setTitle("Demande d'échange");
			eb.setDescription("mauvais format : \n>waifu trade TA_WAIFU_ID SA_WAIFU_ID");
		}
		String id = msg.getContentRaw().split(" ")[2];
		String id2 = msg.getContentRaw().split(" ")[3];
		Waifu w1 = null;
		Waifu w2 = null;
		try {
			w1 = Waifu.getWaifuById(Integer.parseInt(id));
			w2 = Waifu.getWaifuById(Integer.parseInt(id2));
		} catch (NumberFormatException e){
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.red);
			eb.setTitle("Demande d'échange");
			eb.setDescription("mauvais format : \n>waifu trade TA_WAIFU_ID SA_WAIFU_ID");
			return;
		}
		EmbedBuilder eb = null;
		if (w1.getOwner() == null || !w1.getOwner().equals(msg.getMember().getId()))
		{
			eb = new EmbedBuilder();
			eb.setColor(Color.red);
			eb.setTitle("Demande d'échange");
			eb.setDescription("tu n'es pas le propriétaire de cette waifu");
		}
		else if (w2.getOwner() == null)
		{
			eb = new EmbedBuilder();
			eb.setColor(Color.red);
			eb.setTitle("Demande d'échange");
			eb.setDescription("cette waifu est à personne, cherche par toi meme");
		}
		if (eb != null)
			msg.getChannel().sendMessageEmbeds(eb.build()).queue();
		else {
			eb = new EmbedBuilder();
			eb.setAuthor(id + " " + id2);
			eb.setTitle("Demande d'échange");
			eb.setColor(Color.WHITE);
			eb.setDescription(msg.getMember().getAsMention() + " cherche à échanger avec " + msg.getGuild().getMemberById(w2.getOwner()).getAsMention() + "\nIl demande " + w2.getName() + " contre " + w1.getName());
			eb.setFooter(w1.getOwner() +" "+w2.getOwner());
			ArrayList<ActionRow> bttn = new ArrayList<>();
			bttn.add(ActionRow.of(Button.primary("waifutrade_oui", "oui")));
			bttn.add(ActionRow.of(Button.danger("waifutrade_non", "non")));
			msg.getChannel().sendMessage(msg.getGuild().getMemberById(w2.getOwner()).getAsMention()).setEmbeds(eb.build()).setActionRows(bttn).queue();
		}
	}

	private static void setImage(Message msg) {
		if (!msg.getChannel().getId().equals("975087822618910800")) {
			msg.getChannel().sendMessage("non").queue();
			return;
		}
		String id = msg.getContentRaw().split(" ")[2];
		Waifu w = Waifu.getWaifuById(Integer.parseInt(id));
		if (w == null) {
			msg.getChannel().sendMessage("id non défini").queue();
			return;
		}
		if (msg.getAttachments().isEmpty()){
			msg.getChannel().sendMessage("t'es une merde").queue();
			return;
		}
		w.setFile(msg.getAttachments().get(0));
		msg.addReaction("\uD83D\uDC4C").queue();
	}

	private static void reset(Message msg) {
		if (!msg.getMember().getId().equals("315431392789921793"))
		{
			msg.getChannel().sendMessage("https://tenor.com/view/fanta-pas-toi-qui-d%C3%A9cide-serious-selfie-gif-13900956").queue();
			return;
		}
		ArrayList<Waifu> waifus = Waifu.getAllWaifu();
		for (Waifu w : waifus)
			w.setOwner(null);
	}

	private static void newWaifu(Message msg) throws InterruptedException {
		if (!msg.getChannel().getId().equals(BotDiscord.FarmingSalonId)) {
			msg.getChannel().sendMessage("Mové salon comme dirait l'autre").queue();
			return;
		}
		if (isMaintenance) {
			msg.getChannel().sendMessage("le bot est actuellement en maintenance").queue();
			return;
		}
		else if (msg.getMember().getRoles().contains(msg.getGuild().getRoleById(BotDiscord.SecondaryRoleId))){
			msg.getChannel().sendMessage("Tu es un compte secondaire et moi, j'aime pas les comptes secondaires").queue();
			return;
		}
		else if (Waifu.timeleft(msg.getMember().getId()) < 0){
			long t = -Waifu.timeleft(msg.getMember().getId());
			long th = t/HOUR;
			t %= HOUR;
			long tmin = t/MINUTE;
			t %= MINUTE;
			long ts = t/SECOND;
			msg.getChannel().sendMessage("il te reste " + th + "h, " + tmin + "min et " + ts + " secondes avant de chercher une nouvelle Waifu").queue();
			return;

		}
		Waifu.setTime(msg.getMember().getId());
		Waifu w;
		if (Waifu.getAvailableWaifu().isEmpty())
		{
			msg.getChannel().sendMessage("y'a plus rien").queue();
			return;
		}
		if (Waifu.getAvailableWaifu().size() > 1)
			w = Waifu.getAvailableWaifu().get(new Random().nextInt(Waifu.getAvailableWaifu().size() - 1));
		else
			w = Waifu.getAvailableWaifu().get(0);
		File f = new File(w.getProfile());
		w.setOwner(msg.getMember().getId());
		EmbedBuilder eb = new EmbedBuilder();
		eb.setImage("attachment://"+f.getName());
		eb.setTitle("Nouvelle Waifu !");
		eb.setDescription("ta nouvelle Waifu est " + w.getName() + " de " + w.getOrigin());
		eb.setFooter("id : " + w.getId());
		eb.setColor(Squads.getSquadByMember(w.getOwner()).getSquadRole(msg.getGuild()).getColor());
		waifuLock.lock();
		Thread.sleep(100);
		MessageAction toSend = msg.getChannel().sendMessageEmbeds(eb.build());
		try(DataInputStream str = new DataInputStream(new FileInputStream(f))){
			byte[] bytes = new byte[(int) f.length()];
			str.readFully(bytes);
			toSend.addFile(bytes, f.getName()).complete();
		} catch (IOException e) {
			//Wrap et remonter
			throw new RuntimeException(e);
		}
		waifuLock.unlock();
		Squads.addPoints(msg.getMember(), 50L);
	}

public static void addwaifu(Message msg) throws ExecutionException, InterruptedException {
		if (!msg.getChannel().getId().equals("975087822618910800")) {
			msg.getChannel().sendMessage("non").queue();
			return;
		}
		String[] args = msg.getContentRaw().split("\n");
		if (args[0].split(" ").length <= 2) {
			msg.getChannel().sendMessage("SOMBRE MERDE").queue();
			return;
		}
		if (!msg.getAttachments().isEmpty() && msg.getAttachments().size() == 1) {
		ArrayList<SelectOption> options = new ArrayList<>();
		for (Waifu.Type tpe : Waifu.Type.values())
			options.add(new SelectOptionImpl("Catégorie : " + tpe.name(), tpe.name()));
		options.add(new SelectOptionImpl("Annuler", "stop"));
		SelectMenuImpl selectionMenu = new SelectMenuImpl("Waifu", "selectionnez un choix", 1, 1, false, options);
			MessageAction toSend = msg.getChannel().sendMessageEmbeds(new EmbedBuilder().setTitle(args[0].substring(">Waifu add".length())).setFooter(args[1]).setDescription(msg.getContentRaw().substring(args[0].length() + args[1].length() + 1)).build());
			File f = new File(msg.getAttachments().get(0).downloadToFile().get().getName());
			try(DataInputStream str = new DataInputStream(new FileInputStream(f))){
				byte[] bytes = new byte[(int) f.length()];
				str.readFully(bytes);
				toSend.addFile(bytes, f.getName()).setActionRow(selectionMenu).complete();
			} catch (IOException e) {
				//Wrap et remonter
				throw new RuntimeException(e);
			}
		} else {
			msg.getChannel().sendMessage("t'es une merde").queue();
		}
	}
	private static void sendEmbedInfo(Waifu w, TextChannel tc) throws InterruptedException {
		EmbedBuilder eb = new EmbedBuilder();
		File f = new File(w.getProfile());
		eb.setAuthor(w.getOrigin());
		eb.setTitle("Information : " + w.getName() + "\nIdentifiant : " + w.getId());
		eb.setImage("attachment://"+f.getName());
		if (w.getOwner() != null) {
			eb.setFooter("appartient à " + tc.getGuild().getMemberById(w.getOwner()).getEffectiveName(), tc.getGuild().getMemberById(w.getOwner()).getUser().getAvatarUrl());
			eb.setColor(Squads.getSquadByMember(w.getOwner()).getSquadRole(tc.getGuild()).getColor());
		}
		else
			eb.setFooter("disponible");
		eb.setDescription(w.getDescription());
		waifuLock.lock();
		Thread.sleep(100);
		MessageAction toSend = tc.sendMessageEmbeds(eb.build());
		try(DataInputStream str = new DataInputStream(new FileInputStream(f))){
			byte[] bytes = new byte[(int) f.length()];
			str.readFully(bytes);
			toSend.addFile(bytes, f.getName()).complete();
		} catch (IOException e) {
			//Wrap et remonter
			throw new RuntimeException(e);
		}
		waifuLock.unlock();
	}

	public static void infowaifu(Message msg) throws InterruptedException {
		if (msg.getContentRaw().split(" ").length <= 2) {

			ArrayList<Waifu> waifuList = Waifu.getAllWaifu();
			int disponible = 0;
			for (Waifu w : waifuList) {
				if (w.getOwner() == null) {disponible++;}
			}
			int i;
			int P = 100 - ((disponible*100)/waifuList.size());
			String texte = P + " % des waifu ont été capturée \n\n|| ";
			for (i = 0; i<(P/2); i++) {texte += ". ";}
			texte +="||";
			for (int j = i; j<50; j++) {texte += " I";}
			EmbedBuilder eb = new EmbedBuilder().setTitle("Waifu Capturée");
			eb.setDescription(texte);
			eb.setFooter(msg.getMember().getUser().getName(),msg.getMember().getUser().getAvatarUrl());
			msg.getChannel().sendMessageEmbeds(eb.build()).queue();

			return;
		}
		ArrayList<Waifu> w = Waifu.getWaifubyName(msg.getContentRaw().substring(">Waifu info ".length()));
		if (w != null) {
			for (Waifu waif : w) {
				sendEmbedInfo(waif, msg.getTextChannel());
			}
		}
		else
		{
			Waifu wid;
			try {
				wid = Waifu.getWaifuById(Integer.parseInt(msg.getContentRaw().split(" ")[2]));
			}
			catch (NumberFormatException e){
				msg.getChannel().sendMessage("je ne connais pas de Waifu à ce nom ou cet id").queue();
				return;
			}
			if (wid != null)
				sendEmbedInfo(wid, msg.getTextChannel());
			else {
				msg.getChannel().sendMessage("je ne connais pas de Waifu à ce nom ou cet id").queue();
			}
		}
	}


	public static void haremEmbed(Message msg){
		haremEmbed(msg, 0);
	}
	public static void haremEmbed(Message msg, Integer f) {
		ArrayList<Waifu> waifus = Waifu.getAllWaifu();
		Waifu w;
		EmbedBuilder eb = new EmbedBuilder();
		String MemberID = msg.getEmbeds().get(0).getAuthor().getName();
		waifus.removeIf(wai -> wai.getOwner() == null || !wai.getOwner().equals(MemberID));
		int	i = f*10;
		if (waifus.isEmpty())
		{
			msg.editMessageEmbeds(eb.setDescription("tu as actuellement aucune waifu").build()).queue();
			return;
		}
		if (i > waifus.size() || i < 0)
			return;
		eb.setFooter(f.toString()).setTitle(msg.getEmbeds().get(0).getTitle());
		StringBuilder sb = new StringBuilder();
		while (i < (f*10)+10)
		{
			if (i < waifus.size()) {
				w = waifus.get(i);
				sb.append(w.getId()).append(" ").append(w.getName()).append(" de ").append(w.getOrigin()).append("\n");
			}
			i++;
		}
		eb.setColor(Squads.getSquadByMember(MemberID).getSquadRole(msg.getGuild()).getColor());
		eb.setDescription(sb);
		eb.setAuthor(MemberID);
		msg.editMessageEmbeds(eb.build()).queue();
	}




	public static void listwaifu(Message msg){
		listwaifu(msg, 0);
	}
	public static void listwaifu(Message tc, Integer f) {
		ArrayList<Waifu> waifus = Waifu.getAllWaifu();
		Waifu w;
		String search = null;
		if (tc.getEmbeds().get(0).getAuthor() != null)
			search = tc.getEmbeds().get(0).getAuthor().getName();
		EmbedBuilder eb = new EmbedBuilder();
		if (search != null) {
			eb.setAuthor(search);
			String finalSearch = search;
			waifus.removeIf(wai -> !StringExtenders.startWithIgnoreCase(wai.getOrigin(), finalSearch));
		}
		int	i = f*10;
		if (waifus.isEmpty())
		{
			tc.editMessageEmbeds(eb.setDescription("aucune waifu sous cette origine").build()).queue();
			return;
		}
		if (i > waifus.size() || i < 0)
			return;
		eb.setFooter(f.toString()).setTitle(tc.getEmbeds().get(0).getTitle());
		StringBuilder sb = new StringBuilder();
		String combo = waifus.get(i).getOwner();
		while (i < (f*10)+10)
		{
			if (i < waifus.size()) {
				w = waifus.get(i);
				if (w.getOwner() == null || !w.getOwner().equals(combo))
					combo = null;
				if (w.getOwner() == null)
					sb.append(w.getId()).append(" ").append(w.getName()).append(" de ").append(w.getOrigin()).append("\n");
				else
					sb.append(w.getId()).append(" ").append(w.getName()).append(" de ").append(w.getOrigin()).append(" __").append(tc.getGuild().getMemberById(w.getOwner()).getEffectiveName()).append("__\n");
			}
			i++;
		}
		if (combo != null)
			eb.setColor(Squads.getSquadByMember(combo).getSquadRole(tc.getGuild()).getColor());
		eb.setDescription(sb);
		tc.editMessageEmbeds(eb.build()).queue();
	}

	public static void setDescription(Message msg)
	{
		if (!msg.getChannel().getId().equals("975087822618910800")) {
			msg.getChannel().sendMessage("non").queue();
			return;
		}
		String id = msg.getContentRaw().split(" ")[2];
		Waifu w = Waifu.getWaifuById(Integer.parseInt(id));
		if (w == null) {
			msg.getChannel().sendMessage("id non défini").queue();
			return;
		}
		String name = msg.getContentRaw().substring(">Waifu setdescription  ".length() + id.length());
		w.setDescription(name);
		msg.addReaction("\uD83D\uDC4C").queue();
	}

	public static void setOrigin(Message msg)
	{
		if (!msg.getChannel().getId().equals("975087822618910800")) {
			msg.getChannel().sendMessage("non").queue();
			return;
		}
		String id = msg.getContentRaw().split(" ")[2];
		Waifu w = Waifu.getWaifuById(Integer.parseInt(id));
		if (w == null) {
			msg.getChannel().sendMessage("id non défini").queue();
			return;
		}
		String name = msg.getContentRaw().substring(">Waifu setOrigin  ".length() + id.length());
		w.setOrigin(name);
		msg.addReaction("\uD83D\uDC4C").queue();
	}

	public static void delwaifu(Message msg)
	{
		if (!msg.getChannel().getId().equals("975087822618910800")) {
			msg.getChannel().sendMessage("non").queue();
			return;
		}
		String id = msg.getContentRaw().split("\n")[0];
		Waifu w = Waifu.getWaifuById(Integer.parseInt(id.substring(">Waifu delete ".length())));
		if (w == null) {
			msg.getChannel().sendMessage("id non défini").queue();
			return;
		}
		w.delwaifu();
		msg.addReaction("\uD83D\uDC4C").queue();
	}

	public static void setName(Message msg)
	{
		if (!msg.getChannel().getId().equals("975087822618910800")) {
			msg.getChannel().sendMessage("non").queue();
			return;
		}
		String id = msg.getContentRaw().split(" ")[2];
		Waifu w = Waifu.getWaifuById(Integer.parseInt(id));
		if (w == null) {
			msg.getChannel().sendMessage("id non défini").queue();
			return;
		}
		String name = msg.getContentRaw().substring(">Waifu setname  ".length() + id.length());
		w.setName(name);
		msg.addReaction("\uD83D\uDC4C").queue();
	}

	private static void release(Message msg) {
		if (!msg.getChannel().getId().equals(BotDiscord.FarmingSalonId)) {
			msg.getChannel().sendMessage("Mové salon comme dirait l'autre").queue();
			return;
		}
		String id = msg.getContentRaw().split(" ")[2];
		Waifu w = Waifu.getWaifuById(Integer.parseInt(id));
		if (w.getOwner() == null)
			msg.getChannel().sendMessage(w.getName() + " n'appartient a personne").queue();
		else if (!w.getOwner().equals(msg.getMember().getId()))
			msg.getChannel().sendMessage("tu n'est pas le propriétaire de " + w.getName()).queue();
		else {
			w.setOwner(null);
			msg.getChannel().sendMessage(w.getName() + " a été relâché").queue();
			Squads.removePoints(msg.getMember().getId(), 500L);
		}
	}
}


