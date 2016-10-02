/*
 * Copyright (C) 2016 MarkusWME
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Bukkit;

import at.pcgamingfreaks.TestClasses.TestBukkitServer;
import at.pcgamingfreaks.TestClasses.TestObjects;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, JavaPlugin.class })
public class ConfigurationTest
{
	@Before
	public void prepareTestData() throws Exception
	{
		TestObjects.initMockedJavaPlugin();
		whenNew(at.pcgamingfreaks.Configuration.class).withAnyArguments().thenAnswer(new Answer<Object>()
		{
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable
			{
				return null;
			}
		});
		suppress(at.pcgamingfreaks.Configuration.class.getDeclaredMethods());
	}

	@Test
	public void testConfiguration()
	{
		assertNotNull("The configuration object should not be null", new Configuration(TestObjects.getJavaPlugin(), 1));
		assertNotNull("The configuration object should not be null", new Configuration(TestObjects.getJavaPlugin(), 1, "config.yml"));
		assertNotNull("The configuration object should not be null", new Configuration(TestObjects.getJavaPlugin(), 1, 2));
	}

	@Test
	@SuppressWarnings("SpellCheckingInspection")
	public void testIsBukkitVersionUUIDCompatible() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
	{
		Method isBukkitVersionUUIDCompatible = Configuration.class.getDeclaredMethod("isBukkitVersionUUIDCompatible");
		isBukkitVersionUUIDCompatible.setAccessible(true);
		Configuration configuration = new Configuration(TestObjects.getJavaPlugin(), 1);
		assertFalse("If the method fails false should be returned", (Boolean) isBukkitVersionUUIDCompatible.invoke(configuration));
		TestBukkitServer server = new TestBukkitServer();
		Bukkit.setServer(server);
		assertFalse("The version should not be UUID compatible", (Boolean) isBukkitVersionUUIDCompatible.invoke(configuration));
		server.serverVersion = "1.7.4";
		assertFalse("The version should not be UUID compatible", (Boolean) isBukkitVersionUUIDCompatible.invoke(configuration));
		server.serverVersion = "1.7.8";
		assertTrue("The version should be UUID compatible", (Boolean) isBukkitVersionUUIDCompatible.invoke(configuration));
		server.serverVersion = "1.8.8";
		assertTrue("The version should be UUID compatible", (Boolean) isBukkitVersionUUIDCompatible.invoke(configuration));
		server.serverVersion = "2.9.0";
		assertTrue("The version should be UUID compatible", (Boolean) isBukkitVersionUUIDCompatible.invoke(configuration));
		isBukkitVersionUUIDCompatible.setAccessible(false);
	}

	@AfterClass
	public static void cleanupTestData()
	{
		//noinspection ResultOfMethodCallIgnored
		new File("\\config.yml").delete();
	}
}