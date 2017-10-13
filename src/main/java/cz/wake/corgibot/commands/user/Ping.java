package cz.wake.corgibot.commands.user;

import cz.wake.corgibot.commands.ICommand;
import cz.wake.corgibot.commands.CommandType;
import cz.wake.corgibot.commands.Rank;
import cz.wake.corgibot.utils.Constants;
import cz.wake.corgibot.utils.MessageUtils;
import me.jagrosh.jdautilities.waiter.EventWaiter;
import net.dv8tion.jda.core.entities.*;

import java.awt.*;
import java.time.temporal.ChronoUnit;

public class Ping implements ICommand {

    @Override
    public void onCommand(User sender, MessageChannel channel, Message message, String[] args, Member member, EventWaiter w) {
        channel.sendMessage(MessageUtils.getEmbed(Color.GRAY).setDescription("Vypočítávám ping...").build()).queue(m -> {
            m.editMessage(MessageUtils.getEmbed(Constants.GREEN).setDescription(":ping_pong: Pong! `" + message.getCreationTime().until(m.getCreationTime(), ChronoUnit.MILLIS) + " ms`").build()).queue();
        });
    }

    @Override
    public String getCommand() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "Zjištění rychlosti odezvy.";
    }

    @Override
    public String getHelp() {
        return  ".ping";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
    }

    @Override
    public Rank getRank() {
        return Rank.USER;
    }

}
