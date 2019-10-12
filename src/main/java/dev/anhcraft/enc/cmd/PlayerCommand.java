package dev.anhcraft.enc.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;

@CommandAlias("enc")
public class PlayerCommand extends BaseCommand {
    @Default
    @CatchUnknown
    public void root(Player player) {
        player.sendMessage("Commands for players will come soon xD");
        // TODO Add player commands
    }
}
