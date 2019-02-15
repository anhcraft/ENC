package org.anhcraft.enc.commands;

import org.anhcraft.enc.ENC;
import org.anhcraft.spaciouslib.builders.command.CommandBuilder;
import org.anhcraft.spaciouslib.builders.command.CommandCallback;
import org.bukkit.command.CommandSender;

public class UserCommand implements Runnable {
    @Override
    public void run() {
        new CommandBuilder("enc", new CommandCallback() {
            @Override
            public void run(CommandBuilder commandBuilder, CommandSender commandSender, int i, String[] strings, int i1, String s) {
                commandBuilder.sendHelpMessages(commandSender, true, false);
            }
        })
        .build(ENC.getInstance());
    }
}
