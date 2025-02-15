

package fr.cringebot.cringe.command;


import fr.cringebot.BotDiscord;
import fr.cringebot.cringe.Polls.PollMain;
import fr.cringebot.cringe.builder.Command;
import fr.cringebot.cringe.builder.Command.ExecutorType;
import fr.cringebot.cringe.builder.CommandMap;
import fr.cringebot.cringe.escouades.Squads;
import fr.cringebot.cringe.objects.SelectOptionImpl;
import fr.cringebot.cringe.objects.StringExtenders;
import fr.cringebot.cringe.objects.UserExtenders;
import fr.cringebot.cringe.reactionsrole.MessageReact;
import fr.cringebot.cringe.waifus.Waifu;
import fr.cringebot.cringe.waifus.WaifuCommand;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.internal.interactions.component.SelectMenuImpl;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static fr.cringebot.cringe.cki.mainCommand.ckimain;

/**
 * fichier de commandes de base
 */
public class CommandListener {

	private final BotDiscord botDiscord;
	/**
	 * intialisation de l'objet
	 *
	 * @param botDiscord
	 * @param commandMap
	 */
	public CommandListener(BotDiscord botDiscord, CommandMap commandMap) {
		this.botDiscord = botDiscord;
	}

	/**
	 * arreter le bot depuis la console NORMALEMENT
	 *
	 * @param jda le bot a arreter
	 */
	@Command(name = "-", type = ExecutorType.CONSOLE)
	private void stop(JDA jda) {
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		botDiscord.setRunning(false);
	}

	/**
	 * donne les informations sur une personne
	 *
	 * @param channel channel du message
	 * @param msg     message de l'envoyeur
	 */
	@Command(name = "info", description = "information sur un joueur", type = ExecutorType.USER)
	private void info(MessageChannel channel, Message msg) {
		Member mem = msg.getMember();
		if (msg.getMentions().getMembers().size() != 0) {
			mem = msg.getMentions().getMembers().get(0);
		}
		EmbedBuilder builder = new EmbedBuilder()
				.setAuthor(mem.getUser().getName(), null, mem.getUser().getAvatarUrl() + "?size=256")
				.setTitle("Informations")
				.setDescription("> surnom :" + mem.getEffectiveName() + "\n" +
						"> état :" + mem.getOnlineStatus().name() + "\n" +
						"> rejoint le serveur le " + mem.getTimeJoined().getDayOfMonth() + "/" + mem.getTimeJoined().getMonthValue() + "/" + mem.getTimeJoined().getYear() + "\n" +
						"> hypesquad " + UserExtenders.getHypesquad(mem) + "\n" +
						"> creer son compte le " + mem.getTimeCreated().getDayOfMonth() + "/" + mem.getTimeCreated().getMonthValue() + "/" + mem.getTimeCreated().getYear() + "\n" +
						"> messages total : " + UserExtenders.getAllmsg(mem))
				.setColor(mem.getColor());
		channel.sendMessageEmbeds(builder.build()).queue();
	}

	/**
	 * Donne le classement des escoudes
	 * en fonction de leurs point respectif
	 *
	 * @param msg	message de l'envoyeur
	 */
	@Command(name = "top", description = "regarder le classement des escouades")
	private void top(Message msg){
		List<Squads> squads = Squads.getAllSquads();
		StringBuilder sb = new StringBuilder();

		// a b c sont des string pour pour faciliter l'assemblage du message en fonction du classement
		String a = squads.get(0).getName() + " " + squads.get(0).getTotal() + " meilleur : " + msg.getGuild().getMemberById(squads.get(0).getBestid()).getAsMention();
		a = a + " avec " + squads.get(0).getStatMember(msg.getGuild().getMemberById(squads.get(0).getBestid())).getPoints() + " pts\n";
		String b = squads.get(1).getName() + " " + squads.get(1).getTotal() + " meilleur : " + msg.getGuild().getMemberById(squads.get(1).getBestid()).getAsMention();
		b = b + " avec " + squads.get(1).getStatMember(msg.getGuild().getMemberById(squads.get(1).getBestid())).getPoints() + " pts\n";
		String c = squads.get(2).getName() + " " + squads.get(2).getTotal() + " meilleur : " + msg.getGuild().getMemberById(squads.get(2).getBestid()).getAsMention();
		c = c + " avec " + squads.get(2).getStatMember(msg.getGuild().getMemberById(squads.get(2).getBestid())).getPoints() + " pts\n";

		EmbedBuilder eb = new EmbedBuilder().setTitle("Classement :");
		if ((squads.get(0).getTotal() >= squads.get(1).getTotal()) && (squads.get(1).getTotal() >= squads.get(2).getTotal())) // guild 0 > 1 > 2
		{
			sb.append(a).append(b).append(c);
			eb.setColor(squads.get(0).getSquadRole(msg.getGuild()).getColor());
		}
		else if ((squads.get(0).getTotal() >= squads.get(2).getTotal()) && (squads.get(2).getTotal() >= squads.get(1).getTotal())) {// guild 0 > 2 > 1
			sb.append(a).append(c).append(b);
			eb.setColor(squads.get(0).getSquadRole(msg.getGuild()).getColor());
		}
		else if ((squads.get(1).getTotal() >= squads.get(0).getTotal()) && (squads.get(0).getTotal() >= squads.get(2).getTotal())) {// guild 1 > 0 > 2
			sb.append(b).append(a).append(c);
			eb.setColor(squads.get(1).getSquadRole(msg.getGuild()).getColor());
		}
		else if ((squads.get(1).getTotal() >= squads.get(2).getTotal()) && (squads.get(2).getTotal() >= squads.get(0).getTotal())) { // guild 1 > 2 > 0
			sb.append(b).append(c).append(a);
			eb.setColor(squads.get(1).getSquadRole(msg.getGuild()).getColor());
		}
		else if ((squads.get(2).getTotal() >= squads.get(0).getTotal()) && (squads.get(0).getTotal() >= squads.get(1).getTotal())) { // guild 2 > 0 > 1
			sb.append(c).append(a).append(b);
			eb.setColor(squads.get(2).getSquadRole(msg.getGuild()).getColor());
		}
		else  // guild 2 > 1 > 0
		{
			sb.append(c).append(b).append(a);
			eb.setColor(squads.get(2).getSquadRole(msg.getGuild()).getColor());
		}
		eb.setDescription(sb);
		msg.getChannel().sendMessageEmbeds(eb.build()).queue();
	}

	@Command(name = "rank", description = "rang")
	private void rank(Message msg){
		Member mb;
		EmbedBuilder eb = new EmbedBuilder();
		mb = msg.getMember();
		if (!msg.getMentions().getMembers().isEmpty()) {
			mb = msg.getMentions().getMembers().get(0);
		}
		Squads squad = Squads.getSquadByMember(mb);
		eb.setColor(squad.getSquadRole(msg.getGuild()).getColor());
		eb.setTitle(msg.getMember().getEffectiveName());
		eb.setDescription("Nombres de points : " + squad.getStatMember(mb).getPoints().toString() + "\ntotal de l'escouade : " + squad.getTotal().toString());
		eb.setFooter("rang : coming soon");
		msg.getChannel().sendMessageEmbeds(eb.build()).queue();
	}

	@Command(name = "poll", description = "faites des sondages rapidements", type = ExecutorType.USER)
	private void poll(Message msg) {
		PollMain.PollMain(msg);
	}

	@Command(name = "role", description = "permettre de creer un role", type = ExecutorType.USER)
	private void role(Message msg) {
		String[] args = msg.getContentRaw().split(" ");
		ArrayList<SelectOption> options = new ArrayList<>();
		if (args.length == 3) {
			msg.addReaction(args[2]).queue();
			Role r = msg.getGuild().createRole().setName("©◊ß" + args[1]).setMentionable(true).setColor(new Color(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255))).complete();
			EmbedBuilder eb = new EmbedBuilder()
					.setTitle("nouveau role")
					.setDescription(r.getName().replace("©◊ß", "") + "\n" + args[2])
					.setFooter(r.getId());
			for (MessageReact mr : MessageReact.message)
				options.add(new SelectOptionImpl(mr.getTitle(), mr.getTitle()));
			SelectMenuImpl selectionMenu = new SelectMenuImpl("role", "catégorie", 1, 1, false, options);
			msg.getChannel().sendMessageEmbeds(eb.build()).setActionRow(selectionMenu).complete();
		} else {
			msg.getChannel().sendMessage("erreur argument >role <nom> <emote>").queue();
		}
	}

	@Command(name = "harem", description = "la listes des waifus", type = ExecutorType.USER)
	private void harem(Message msg){
		if (!msg.getChannel().getId().equals(BotDiscord.FarmingSalonId)) {
			msg.getChannel().sendMessage("non").queue();
			return;
		}
		String id = msg.getMember().getId();
		if (!msg.getMentions().getMembers().isEmpty())
			id = msg.getMentions().getMembers().get(0).getId();
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Waifu de " + msg.getGuild().getMemberById(id).getEffectiveName());
		eb.setAuthor(id);
		eb.setDescription("chargement...");
		msg = msg.getChannel().sendMessageEmbeds(eb.build()).complete();
		msg.addReaction("◀️").and(msg.addReaction("▶️")).queue();
		WaifuCommand.haremEmbed(msg);
	}
	@Command(name = "waifu", description = "instance des waifus", type = ExecutorType.USER)
	private void waifu(Message msg) throws ExecutionException, InterruptedException {
		WaifuCommand.CommandMain(msg);
	}

	private void addDirectory(ZipOutputStream zos, Path relativeFilePath) {
		try {
			ZipEntry entry = new ZipEntry(relativeFilePath.toString() + "/");
			zos.putNextEntry(entry);
			zos.closeEntry();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int length;
		while ((length = inputStream.read(buffer)) >= 0) {
			outputStream.write(buffer, 0, length);
		}
	}
	private void addFile(ZipOutputStream zos, Path filePath, Path zipFilePath) {
		try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
			ZipEntry entry = new ZipEntry(zipFilePath.toString());
			zos.putNextEntry(entry);
			copy(fis, zos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Command(name = "getbdd", description = "recuperer la bdd", type = ExecutorType.USER)
	private void getbdd(Message msg){
		if (!msg.getMember().getPermissions().contains(Permission.ADMINISTRATOR))
			return;
		Path zipPath = new File("bdd.zip").toPath();
		Path inputDirectoryPath = new File("save/").toPath();
		try (FileOutputStream fos = new FileOutputStream(zipPath.toFile());
			 ZipOutputStream zos = new ZipOutputStream(fos)) {
			Files.walk(inputDirectoryPath)
					.filter(someFileToZip -> !someFileToZip.equals(inputDirectoryPath))
					.forEach(
							someFileToZip -> {
								Path pathInZip = inputDirectoryPath.relativize(someFileToZip);
								if (Files.isDirectory(someFileToZip)) {
									addDirectory(zos, pathInZip);
								} else {
									addFile(zos, someFileToZip, pathInZip);
								}
							});
		} catch (IOException e) {
			e.printStackTrace();
		}
		msg.getChannel().sendFile(zipPath.toFile()).queue();
	}

	@Command(name = "cki", description = "mais qui est-il !", type = ExecutorType.USER)
	private void cki(Message msg){
		ckimain(msg);
	}

	@Command(name = "reset", type = Command.ExecutorType.USER)
	private void reset(Message msg) throws IOException {
		if (msg.getMember().getId().equals("315431392789921793"))
		{
			ArrayList<Squads> squads = Squads.getAllSquads();
			for (Squads squad : squads)
				squad.ResetPoint();
		}
	}

	@Command(name = "test", description = "commande provisoire", type = ExecutorType.USER)
	private void test(Message msg) throws IOException {
		msg.getChannel().sendFile(new File("save.zip")).queue();
	}
}