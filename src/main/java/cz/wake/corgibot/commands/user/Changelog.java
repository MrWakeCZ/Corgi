package cz.wake.corgibot.commands.user;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import cz.wake.corgibot.CorgiBot;
import cz.wake.corgibot.annotations.SinceCorgi;
import cz.wake.corgibot.commands.Command;
import cz.wake.corgibot.commands.CommandCategory;
import cz.wake.corgibot.objects.ChangeLog;
import cz.wake.corgibot.objects.GuildWrapper;
import cz.wake.corgibot.utils.EmoteList;
import cz.wake.corgibot.utils.MessageUtils;
import cz.wake.corgibot.utils.lang.I18n;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@SinceCorgi(version = "1.3.0")
public class Changelog implements Command {

    @Override
    public void onCommand(MessageChannel channel, Message message, String[] args, Member member, EventWaiter w, GuildWrapper gw) {
        StringBuilder changelog = new StringBuilder();

        ChangeLog changes = CorgiBot.getInstance().getSql().getLastChanges();

        if (changes == null) {
            MessageUtils.sendErrorMessage(I18n.getLoc(gw, "internal.error.api-failed"), channel);
            return;
        }

        changelog.append(EmoteList.INFO + " | **Update [" + convertMilisToDate(String.valueOf(changes.getDate())) + "]**");
        changelog.append("\n\n");
        if (changes.getNews() != null) {
            changelog.append(EmoteList.GREEN_OK + "** | " + I18n.getLoc(gw, "commands.changelog.news") + ":**\n");
            changelog.append(changes.getNews().replaceAll("-", "•"));
            changelog.append("\n\n");
        }

        if (changes.getFixes() != null) {
            changelog.append(EmoteList.BUG + " ** | " + I18n.getLoc(gw, "commands.changelog.fixes") + ":**\n");
            changelog.append(changes.getFixes().replaceAll("-", "•"));
            changelog.append("\n\n");
        }

        if (changes.getWarning() != null) {
            changelog.append(EmoteList.WARNING + " ** | " + I18n.getLoc(gw, "commands.changelog.announce") + ":**\n");
            changelog.append(changes.getWarning().replaceAll("-", "•"));
            changelog.append("\n\n");
        }

        changelog.append(I18n.getLoc(gw, "commands.changelog.footer").replace("{1}", "https://discord.gg/eaEFCYX"));

        channel.sendMessage(changelog.toString()).queue();
    }

    @Override
    public String getCommand() {
        return "changelog";
    }

    @Override
    public String getDescription() {
        return "Latest changes and news, what has been changed in Corgi";
    }

    @Override
    public String getHelp() {
        return "%changelog - Generates latest changelog";
    }

    @Override
    public CommandCategory getCategory() {
        return CommandCategory.GENERAL;
    }

    public static String dateFormat = "dd.MM.yyyy";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);

    public static String convertMilisToDate(String milliSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(milliSeconds));
        return simpleDateFormat.format(calendar.getTime());
    }
}
