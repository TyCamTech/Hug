/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hug.autcraft.hug;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 *
 * @author Stuart
 */
public class Hug extends JavaPlugin {

    private static final HashMap<String, Long> cds = new HashMap<String, Long>();
    private int cooldown_timer;
    private int hug_distance;
    private boolean red = false;
    
    @Override
    public void onEnable(){
        // initialize the config
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        // Get defaults
        cooldown_timer = getConfig().getInt("cooldown_timer");
        hug_distance = getConfig().getInt("minimum_hug_distance");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]){

        // Be able to take this away from people if necessary.
        if( sender.hasPermission("hug.hug") == false ){
            sender.sendMessage(msg("Insufficient permissions for hugs", true));
            return true;
        }

        // First check to see if args[0] is 'reload' because then it's a request to reloag the config
        if( sender.hasPermission("hug.reload") && args.length > 0 && args[0].equalsIgnoreCase("reload") ){
            reloadConfig();
            sender.sendMessage(msg("Hug Config File Reloaded"));
            return true;
        }
        

        // Fail if console. Consoles can not hug.
        if( sender instanceof ConsoleCommandSender ){
            sender.sendMessage(msg("Sorry console, you can not hug. You have no arms.", true));
            return true;
        }

        // If this is a player calling the command
        if( sender instanceof Player ){
            Player player = (Player) sender;

            // Bypass the cooldown?
            if( player.hasPermission("hug.cooldown_bypass") ){}
            else {
                // If there is still a cooldown timer in place, no hugs!
                if( cooldown(player, cooldown_timer * 1000) == false ){
                    sender.sendMessage(msg("Sorry, only one hug every " + cooldown_timer + " seconds!", true));
                    return true;
                }
            }


            // Did they not include someone to hug? Huge yourself!
            if( args.length == 0 ){
                sender.sendMessage(msg("You give yourself a nice big hug"));
                return true;
            }

            // "name" is easier to deal with than args[0]
            String name = args[0];

            // Check for an easter egg message. If there is one, return it. Otherwise, continue on.
            String easter_egg_message = easterEgg(name, player);
            if( easter_egg_message != null ){
                sender.sendMessage(msg(easter_egg_message));
                return true;
            }

            // If there is an argument, or more... check to see if there's a player that matches arg[0]
            Player huggee = getPlayer(name);
            if( huggee == null ){
                sender.sendMessage(msg(name + " is not online", true));
                return true;
            }
            
            // Get the hugger and huggee's locations
            String playerWorld = player.getWorld().getName();
            String huggeeWorld = huggee.getWorld().getName();
            Location playerLocation = player.getLocation();
            if( playerWorld.equals(huggeeWorld) == false || playerLocation.distance( huggee.getLocation() ) > hug_distance ){
                sender.sendMessage(msg(huggee.getName() + " is too far away to hug!", true));
                return true;
            }

            // Finally, give the hug!
            huggee.sendMessage(msg(player.getName() + " gives you a big hug! {^-^}"));
            player.sendMessage(msg("You gave " + huggee.getName() + " a big hug."));
            return true;
        }
        
        return false;
    }

    /*
    Just a handy place to pass all strings so that we can colour them as necessary
    */
    public String msg(String msg, boolean red){
        // If red is true, return the message in red.
        if( red ){
            return ChatColor.RED + "Hug: " + msg;
        }

        return ChatColor.GREEN + "Hug: " + msg;
    }

    /*
    Overload, set red to false as default
    */
    public String msg(String msg){
        return msg(msg, false);
    }

    /*
    Returns an instance of player, if that player exists and is online.
    Otherwise it returns null.
    */
    public Player getPlayer(String name){
        Player player = Bukkit.getServer().getPlayer(name);
        return player;
    }

    /*
    Returns true if the cooldown period has expired.
    Returns false if still in cooldown.
    */
    public boolean cooldown(Player player, int seconds){

        if( !cds.containsKey(player.getName()) || System.currentTimeMillis() - seconds >= cds.get(player.getName()) ){
            cds.put(player.getName(), System.currentTimeMillis());
            return true;
        }

        return false;
    }

    /*
    Check for an easter egg. If there is one, return it's text.
    */
    public String easterEgg(String input, Player sender){
        // Get the easter eggs from the config file.
        ConfigurationSection configSection = getConfig().getConfigurationSection("easter_eggs");

        // If the entered text is not an easter egg, simply return false.
        if( configSection.contains(input.toLowerCase()) == false ){
            return null;
        }

        // Return the easter egg text
        // TODO: Add alternative texts in a list and have it grab one randomly so they're different each time
        return configSection.getString(input.toLowerCase());
    }
}
