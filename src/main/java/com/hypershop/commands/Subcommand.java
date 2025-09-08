package com.hypershop.commands;

import org.bukkit.command.CommandSender;

public interface Subcommand {
    String name();
    String permission();
    String usage();
    boolean execute(CommandSender sender, String[] args);
}
