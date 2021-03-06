/*
 *   Copyright (C) 2020 GeorgH93
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Bukkit;

import at.pcgamingfreaks.ConsoleColor;
import at.pcgamingfreaks.Reflection;

import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of static functions that are may be useful for plugins.
 */
public class Utils extends at.pcgamingfreaks.Utils
{
	private static final Class<?> NBT_TAG_COMPOUND_CLASS = NmsReflector.INSTANCE.getNmsClass("NBTTagCompound");
	private static final Method AS_NMS_COPY_METHOD = OBCReflection.getOBCMethod("inventory.CraftItemStack", "asNMSCopy", ItemStack.class);
	private static final Method SAVE_NMS_ITEM_STACK_METHOD = NmsReflector.INSTANCE.getNmsMethod("ItemStack", "save", NBT_TAG_COMPOUND_CLASS);
	private static final Method METHOD_JAVA_PLUGIN_GET_FILE = Reflection.getMethod(JavaPlugin.class, "getFile");
	//region Reflection constants for the send packet method
	private static final Class<?> ENTITY_PLAYER = NmsReflector.INSTANCE.getNmsClass("EntityPlayer");
	private static final Class<?> PACKET = NmsReflector.INSTANCE.getNmsClass("Packet");
	private static final Method SEND_PACKET = NmsReflector.INSTANCE.getNmsMethod("PlayerConnection", "sendPacket", PACKET);
	private static final Field PLAYER_CONNECTION = NmsReflector.INSTANCE.getNmsField(ENTITY_PLAYER, "playerConnection");
	//endregion
	private static final Field PLAYER_PING = NmsReflector.INSTANCE.getNmsField(ENTITY_PLAYER, "ping");
	public static final ChatColor[] CHAT_COLORS;

	static
	{
		CHAT_COLORS = new ChatColor[16];
		int i = 0;
		for(ChatColor c : ChatColor.values())
		{
			if(c.isColor() && c != ChatColor.RESET) CHAT_COLORS[i++] = c;
		}
	}

	/**
	 * Converts an item stack into a json string used for chat messages.
	 *
	 * @param itemStack The item stack that should be converted into a json string
	 * @param logger The logger that should display the error message in case of an problem
	 * @return The item stack as a json string. empty string if the conversation failed
	 */
	public static String convertItemStackToJson(@NotNull ItemStack itemStack, @NotNull Logger logger)
	{
		Validate.notNull(logger, "The logger can't be null.");
		Validate.notNull(itemStack, "The item stack can't be null.");
		try
		{
			if(SAVE_NMS_ITEM_STACK_METHOD == null || AS_NMS_COPY_METHOD == null || NBT_TAG_COMPOUND_CLASS == null)
			{
				logger.log(Level.SEVERE, "Failed to serialize item stack to NMS item! Bukkit Version: " + Bukkit.getServer().getVersion() +
						"\nOne or more of the reflection variables is null! Looks like your bukkit version is not compatible. Please check for updates.");
			}
			else
			{
				return SAVE_NMS_ITEM_STACK_METHOD.invoke(AS_NMS_COPY_METHOD.invoke(null, itemStack), NBT_TAG_COMPOUND_CLASS.newInstance()).toString();
			}
		}
		catch (Throwable t)
		{
			logger.log(Level.SEVERE, "Failed to serialize item stack to NMS item! Bukkit Version: " + Bukkit.getServer().getVersion() + "\n", t);
		}
		return "";
	}

	/**
	 * Checks if per world plugins is installed. Used to check
	 *
	 * @return true if PerWorldPlugins is installed
	 */
	public static boolean isPerWorldPluginsInstalled()
	{
		return Bukkit.getServer().getPluginManager().getPlugin("PerWorldPlugins") != null;
	}

	/**
	 * Shows a warning message if per world plugins is installed and blocks the executing thread for 5 seconds.
	 *
	 * @param logger The logger to output the warning
	 */
	public static void warnIfPerWorldPluginsIsInstalled(@NotNull Logger logger)
	{
		warnIfPerWorldPluginsIsInstalled(logger, 5);
	}

	/**
	 * Shows a warning message if per world plugins is installed and blocks the executing thread for a given time.
	 *
	 * @param logger The logger to output the warning
	 * @param pauseTime The time in seconds the function should be blocking if PerWorldPlugins is installed.
	 */
	public static void warnIfPerWorldPluginsIsInstalled(@NotNull Logger logger, int pauseTime)
	{
		Validate.notNull(logger, "The logger can't be null.");
		if(isPerWorldPluginsInstalled())
		{
			logger.warning(ConsoleColor.RED    + "   !!!!!!!!!!!!!!!!!!!!!!!!!" + ConsoleColor.RESET);
			logger.warning(ConsoleColor.RED    + "   !!!!!! - WARNING - !!!!!!" + ConsoleColor.RESET);
			logger.warning(ConsoleColor.RED    + "   !!!!!!!!!!!!!!!!!!!!!!!!!" + ConsoleColor.RESET + "\n");
			logger.warning(ConsoleColor.RED    + " We have detected that you are using \"PerWorldPlugins\"!" + ConsoleColor.RESET);
			logger.warning(ConsoleColor.YELLOW + " Please allow this plugin to run in " + ConsoleColor.BLUE + " ALL " + ConsoleColor.YELLOW + " worlds." + ConsoleColor.RESET);
			logger.warning(ConsoleColor.YELLOW + " If you block it from running in all worlds there probably will be problems!" + ConsoleColor.RESET);
			logger.warning(ConsoleColor.YELLOW + " If you don't want you players to use this plugin in certain worlds please use permissions and the plugins config!" + ConsoleColor.RESET);
			logger.warning(ConsoleColor.RED    + " There will be no support for bugs caused by \"PerWorldPlugins\"!" + ConsoleColor.RESET);
			blockThread(pauseTime);
		}
	}

	/**
	 * Calculates the distance between two players
	 * Unlike Bukkit's built in function this will not cause an exception if the players aren't in the same world but return {@link Double#POSITIVE_INFINITY}
	 *
	 * @param player1 The first player
	 * @param player2 The second player
	 * @return The distance between the players. {@link Double#POSITIVE_INFINITY} if the players aren't in the same world
	 */
	public static double getDistance(@NotNull Player player1, @NotNull Player player2)
	{
		Validate.notNull(player1, "None of the players can be null!");
		Validate.notNull(player2, "None of the players can be null!");
		if(player1.equals(player2))
		{
			return 0;
		}
		if(player1.getWorld().getName().equalsIgnoreCase(player2.getWorld().getName()))
		{
			return player1.getLocation().distance(player2.getLocation());
		}
		return Double.POSITIVE_INFINITY;
	}

	/**
	 * Calculates the squared distance between two players
	 * Unlike Bukkit's built in function this will not cause an exception if the players aren't in the same world but return {@link Double#POSITIVE_INFINITY}
	 *
	 * @param player1 The first player
	 * @param player2 The second player
	 * @return The distance between the players. {@link Double#POSITIVE_INFINITY} if the players aren't in the same world
	 */
	public static double getDistanceSquared(@NotNull Player player1, @NotNull Player player2)
	{
		Validate.notNull(player1, "None of the players can be null!");
		Validate.notNull(player2, "None of the players can be null!");
		if(player1.equals(player2))
		{
			return 0;
		}
		if(player1.getWorld().getName().equalsIgnoreCase(player2.getWorld().getName()))
		{
			return player1.getLocation().distanceSquared(player2.getLocation());
		}
		return Double.POSITIVE_INFINITY;
	}

	/**
	 * Checks if two players are within a certain range from each other.
	 *
	 * @param player1 The first player.
	 * @param player2 The second player.
	 * @param maxDistance The max distance between the two players. Negative values will always return true.
	 * @return True if the players are within the given range, false if not.
	 */
	public static boolean inRange(@NotNull Player player1, @NotNull Player player2, double maxDistance)
	{
		if(maxDistance < 0) return true;
		double distance = getDistanceSquared(player1, player2);
		return (maxDistance == 0 && distance != Double.POSITIVE_INFINITY) || distance <= maxDistance * maxDistance;
	}

	/**
	 * Checks if two players are within a certain range from each other.
	 *
	 * @param player1 The first player.
	 * @param player2 The second player.
	 * @param maxDistanceSquared The max squared distance between the two players. Negative values will always return true.
	 * @return True if the players are within the given range, false if not.
	 */
	public static boolean inRangeSquared(@NotNull Player player1, @NotNull Player player2, double maxDistanceSquared)
	{
		if(maxDistanceSquared < 0) return true;
		double distance = getDistanceSquared(player1, player2);
		return (maxDistanceSquared == 0 && distance != Double.POSITIVE_INFINITY) || distance <= maxDistanceSquared;
	}

	/**
	 * Checks if two players are within a certain range from each other.
	 *
	 * @param player1 The first player.
	 * @param player2 The second player.
	 * @param maxDistance The max distance between the two players. Negative values will always return true.
	 * @param bypassPermission If one of the players has the permission this function will return true.
	 * @return True if the players are within the given range, false if not.
	 */
	public static boolean inRange(@NotNull Player player1, @NotNull Player player2, double maxDistance, @NotNull String bypassPermission)
	{
		return player1.hasPermission(bypassPermission) || player2.hasPermission(bypassPermission) || inRange(player1, player2, maxDistance);
	}

	/**
	 * Checks if two players are within a certain range from each other.
	 *
	 * @param player1 The first player.
	 * @param player2 The second player.
	 * @param maxDistanceSquared The max squared distance between the two players. Negative values will always return true.
	 * @param bypassPermission If one of the players has the permission this function will return true.
	 * @return True if the players are within the given range, false if not.
	 */
	public static boolean inRangeSquared(@NotNull Player player1, @NotNull Player player2, double maxDistanceSquared, @NotNull String bypassPermission)
	{
		return player1.hasPermission(bypassPermission) || player2.hasPermission(bypassPermission) || inRangeSquared(player1, player2, maxDistanceSquared);
	}

	/**
	 * Sends a nms packet to the client
	 *
	 * @param player The player that should receive the packet
	 * @param packet The packet that should be sent to the client
	 * @throws IllegalAccessException If the access to the NMS player was denied by the Java language access control.
	 * @throws InvocationTargetException If the NMS player method used for sending caused an exception.
	 */
	public static void sendPacket(@NotNull Player player, @NotNull Object packet) throws IllegalAccessException, InvocationTargetException
	{
		Validate.notNull(player, "The player that should receive this packet can't be null!");
		Validate.notNull(packet, "The packet to send can't be null!");
		if(SEND_PACKET == null || PLAYER_CONNECTION == null) return;
		Object handle = NMSReflection.getHandle(player);
		if(handle != null && handle.getClass() == ENTITY_PLAYER) // If it's not a real player we can't send him the packet
		{
			SEND_PACKET.invoke(PLAYER_CONNECTION.get(handle), packet);
		}
	}

	/**
	 * Gets the ping for a player.
	 *
	 * @param player The player for witch the ping should be retrieved.
	 * @return The ping of the player.
	 */
	public static int getPing(@NotNull Player player)
	{
		Validate.notNull(player, "The player for which the ping is requested must not be null!");
		if(PLAYER_PING == null) return -1;
		Object handle = NMSReflection.getHandle(player);
		if(handle != null && handle.getClass() == ENTITY_PLAYER) // If it's not a real player we can't send him the packet
		{
			try
			{
				return PLAYER_PING.getInt(handle);
			}
			catch(IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		return -1;
	}

	/**
	 * Drops the content of an inventory.
	 * The inventory will be cleared after it has been dropped successful.
	 *
	 * @param inventory The inventory to be dropped
	 * @param location The location the inventory should be dropped to
	 */
	public static void dropInventory(@NotNull Inventory inventory, @NotNull Location location)
	{
		dropInventory(inventory, location, true);
	}

	/**
	 * Drops the content of an inventory.
	 *
	 * @param inventory The inventory to be dropped
	 * @param location The location the inventory should be dropped to
	 * @param clearInventory Defines if the inventory should be cleared after dropping it or not
	 */
	public static void dropInventory(@NotNull Inventory inventory, @NotNull Location location, boolean clearInventory)
	{
		for(ItemStack i : inventory.getContents())
		{
			if(i != null)
			{
				location.getWorld().dropItemNaturally(location, i);
			}
		}
		if(clearInventory) inventory.clear();
	}

	/**
	 * Gets the jar file of a Bukkit JavaPlugin.
	 *
	 * @param plugin The plugin to get the jar file of.
	 * @return The jar file of the given plugin.
	 * @throws RuntimeException If there was a problem obtaining the plugin.
	 */
	public static File getPluginJarFile(final @NotNull JavaPlugin plugin) throws RuntimeException
	{
		try
		{
			//noinspection ConstantConditions
			return (File) METHOD_JAVA_PLUGIN_GET_FILE.invoke(plugin);
		}
		catch(Exception e)
		{
			throw new RuntimeException("Failed to retrieve jar file for plugin " + plugin.getName(), e);
		}
	}

	public static @NotNull List<String> getPlayerNamesStartingWith(@NotNull String startingWith, final @NotNull CommandSender exclude)
	{
		String excludeName = exclude.getName().toLowerCase(Locale.ROOT);
		startingWith = startingWith.toLowerCase(Locale.ROOT);
		List<String> names = new ArrayList<>();
		for(Player player : Bukkit.getOnlinePlayers())
		{
			String nameLower = player.getName().toLowerCase(Locale.ROOT);
			if(!nameLower.equals(excludeName) && nameLower.startsWith(startingWith)) names.add(player.getName());
		}
		return names;
	}

	public static @Nullable Inventory getClickedInventory(final @NotNull InventoryClickEvent event)
	{
		if (event.getRawSlot() < 0) return null;

			return event.getRawSlot() < event.getView().getTopInventory().getSize() ? event.getView().getTopInventory() : event.getView().getBottomInventory();
	}
}
