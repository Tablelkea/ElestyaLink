package org.elestya.plugins.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.elestya.plugins.database.Database;
import org.elestya.plugins.services.DiscordService;

import java.util.Objects;

public class DiscordLinkCommand implements CommandExecutor {

    private Database database;
    private DiscordService discordService;

    public DiscordLinkCommand(Database database, DiscordService discordService) {
        this.database = Objects.requireNonNull(database);
        this.discordService = Objects.requireNonNull(discordService);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        discordService.link(p);
        return true;
    }
}
