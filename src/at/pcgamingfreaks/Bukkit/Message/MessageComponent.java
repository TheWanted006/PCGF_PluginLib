/*
 *   Copyright (C) 2019 GeorgH93
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

package at.pcgamingfreaks.Bukkit.Message;

import at.pcgamingfreaks.Bukkit.MCVersion;
import at.pcgamingfreaks.Bukkit.NmsReflector;
import at.pcgamingfreaks.Bukkit.OBCReflection;
import at.pcgamingfreaks.Bukkit.Utils;
import at.pcgamingfreaks.Message.MessageColor;
import at.pcgamingfreaks.Message.MessageHoverEvent;
import at.pcgamingfreaks.Reflection;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;

import org.bukkit.*;
import org.bukkit.Statistic.Type;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public final class MessageComponent extends at.pcgamingfreaks.Message.MessageComponent<MessageComponent, ChatColor> implements JsonDeserializer<MessageComponent>
{
	//region Reflection Variables
	private static final transient Class<?> CRAFT_STATISTIC = OBCReflection.getOBCClass("CraftStatistic");
	@SuppressWarnings({ "ConstantConditions", "deprecation" })
	private static final transient Method GET_NMS_ACHIEVEMENT =  (MCVersion.isOlderThan(MCVersion.MC_1_12)) ? OBCReflection.getMethod(CRAFT_STATISTIC, "getNMSAchievement", Reflection.getClass("org.bukkit.Achievement")) : null;
	@SuppressWarnings("ConstantConditions")
	private static final transient Method GET_NMS_STATISTIC = OBCReflection.getMethod(CRAFT_STATISTIC, "getNMSStatistic", Statistic.class);
	private static final transient Method GET_MATERIAL_STATISTIC = OBCReflection.getMethod(CRAFT_STATISTIC, "getMaterialStatistic", Statistic.class, Material.class);
	private static final transient Method GET_ENTITY_STATISTIC = OBCReflection.getMethod(CRAFT_STATISTIC, "getEntityStatistic", Statistic.class, EntityType.class);
	private static final transient Method GET_STATISTIC_NAME = MCVersion.isNewerOrEqualThan(MCVersion.MC_1_13) ? NmsReflector.INSTANCE.getNmsMethod("IScoreboardCriteria", "getName") : null;
	private static final transient Field FIELD_STATISTIC_NAME = MCVersion.isOlderThan(MCVersion.MC_1_13) ? NmsReflector.INSTANCE.getNmsField("Statistic", "name") : null;
	//endregion

	private static final transient at.pcgamingfreaks.Message.MessageComponent NEW_LINE_HELPER = new MessageComponent("\n");
	private static final transient MessageComponent MESSAGE_COMPONENT_INSTANCE = new MessageComponent();

	static
	{
		GSON = new GsonBuilder().registerTypeAdapter(at.pcgamingfreaks.Message.MessageComponent.class, MESSAGE_COMPONENT_INSTANCE).create();
		messageComponentClass = MessageComponent.class;
		messageComponentConstructor = Reflection.getConstructor(MessageComponent.class);
	}

	@Override
	protected at.pcgamingfreaks.Message.MessageComponent getNewLineComponent()
	{
		return NEW_LINE_HELPER;
	}

	//region Constructors
	/**
	 * Creates a new empty MessageComponent instance.
	 */
	public MessageComponent() {}

	/**
	 * Creates a new empty MessageComponent instance.
	 *
	 * @param text   The text for the {@link MessageComponent}.
	 * @param styles The style for the {@link MessageComponent}.
	 */
	public MessageComponent(String text, MessageColor... styles)
	{
		super(text, styles);
	}

	/**
	 * Creates a new empty MessageComponent instance.
	 *
	 * @param text   The text for the {@link MessageComponent}.
	 * @param styles The style for the {@link MessageComponent}.
	 */
	public MessageComponent(String text, @Nullable ChatColor[] styles)
	{
		super(text, styles);
	}
	//endregion

	//region Deserializer and Deserializer Functions
	/**
	 * Generates a MessageComponent list from a given JSON string.
	 *
	 * @param jsonString The JSON string representing the components.
	 * @return A list of MessageComponent objects. An empty list if there are no components in the given {@link JsonArray}.
	 */
	public static List<MessageComponent> fromJson(String jsonString)
	{
		return MESSAGE_COMPONENT_INSTANCE.fromJsonWorker(jsonString);
	}

	/**
	 * Generates a MessageComponent list from a given {@link JsonArray} object.
	 *
	 * @param componentArray The {@link JsonArray} containing all the components, from the deserializer.
	 * @return A list of MessageComponent objects. An empty list if there are no components in the given {@link JsonArray}.
	 */
	public static List<MessageComponent> fromJsonArray(JsonArray componentArray)
	{
		return MESSAGE_COMPONENT_INSTANCE.fromJsonArrayWorker(componentArray);
	}
	//endregion

	//region Message modifier (getter/setter).
	/**
	 * Gets the color of the component.
	 *
	 * @return The color of the component as a {@link ChatColor}, null if no color is defined.
	 */
	public ChatColor getChatColor()
	{
		return ChatColor.valueOf(getColorString().toUpperCase(Locale.ROOT));
	}
	//endregion

	//region Short message modifier (setter)
	private static String getStatisticName(Object statistic) throws Exception
	{
		if(FIELD_STATISTIC_NAME != null) return (String) FIELD_STATISTIC_NAME.get(statistic);
		else return (String) GET_STATISTIC_NAME.invoke(statistic);
	}

	/**
	 * Set the behavior of the component to display information about an achievement when the client hovers over the text.
	 * Not supported in MC 1.12 and newer.
	 *
	 * @param achievement The achievement to display.
	 * @return This message component instance.
	 */
	public MessageComponent achievementTooltip(Object achievement)
	{
		if(MCVersion.isOlderThan(MCVersion.MC_1_12))
		{
			assert achievement instanceof Achievement;
			try
			{
				return onHover(MessageHoverEvent.HoverEventAction.SHOW_ACHIEVEMENT, getStatisticName(GET_NMS_ACHIEVEMENT.invoke(null, achievement)));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Achievements are not supported in your minecraft version!");
		}
		return this;
	}

	/**
	 * Set the behavior of the component to display information about a parameterless statistic when the client hovers over the text.
	 *
	 * @param statistic The statistic to display.
	 * @return This message component instance.
	 * @exception IllegalArgumentException If the statistic requires a parameter which was not supplied.
	 */
	public MessageComponent statisticTooltip(Statistic statistic) throws IllegalArgumentException
	{
		Type type = statistic.getType();
		if (type != Type.UNTYPED) throw new IllegalArgumentException("That statistic requires an additional " + type + " parameter!");
		try
		{
			return onHover(MessageHoverEvent.HoverEventAction.SHOW_ACHIEVEMENT, getStatisticName(GET_NMS_STATISTIC.invoke(null, statistic)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Set the behavior of the component to display information about a parameterless statistic when the client hovers over the text.
	 *
	 * @param statistic The statistic to display.
	 * @param material The sole material parameter to the statistic.
	 * @return This message component instance.
	 * @exception IllegalArgumentException If the statistic requires a parameter which was not supplied, or was supplied a parameter that was not required.
	 */
	public MessageComponent statisticTooltip(Statistic statistic, Material material) throws IllegalArgumentException
	{
		Type type = statistic.getType();
		if (type == Type.UNTYPED) throw new IllegalArgumentException("That statistic needs no additional parameter!");
		if ((type == Type.BLOCK && material.isBlock()) || type == Type.ENTITY) throw new IllegalArgumentException("Wrong parameter type for that statistic - needs " + type + "!");
		try
		{
			return onHover(MessageHoverEvent.HoverEventAction.SHOW_ACHIEVEMENT, getStatisticName(GET_MATERIAL_STATISTIC.invoke(null, statistic, material)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Set the behavior of the component to display information about a parameterless statistic when the client hovers over the text.
	 *
	 * @param statistic The statistic to display.
	 * @param entity The sole entity type parameter to the statistic.
	 * @return This message component instance.
	 * @exception IllegalArgumentException If the statistic requires a parameter which was not supplied, or was supplied a parameter that was not required.
	 */
	public MessageComponent statisticTooltip(Statistic statistic, EntityType entity) throws IllegalArgumentException
	{
		Type type = statistic.getType();
		if (type == Type.UNTYPED) throw new IllegalArgumentException("That statistic needs no additional parameter!");
		if (type != Type.ENTITY) throw new IllegalArgumentException("Wrong parameter type for that statistic - needs " + type + "!");
		try
		{
			return onHover(MessageHoverEvent.HoverEventAction.SHOW_ACHIEVEMENT, getStatisticName(GET_ENTITY_STATISTIC.invoke(null, statistic, entity)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Set the behavior of the component to display information about an item when the client hovers over the text.
	 *
	 * @param itemStack The stack for which to display information.
	 * @return This message component instance.
	 */
	@SuppressWarnings("UnusedReturnValue")
	public MessageComponent itemTooltip(ItemStack itemStack)
	{
		return itemTooltip(Utils.convertItemStackToJson(itemStack, Bukkit.getLogger()));
	}
	//endregion
}