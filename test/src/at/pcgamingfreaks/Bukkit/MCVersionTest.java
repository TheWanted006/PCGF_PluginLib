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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ NMSReflection.class })
public class MCVersionTest
{
	@BeforeClass
	public static void prepareTestData() throws NoSuchFieldException, IllegalAccessException
	{
		Bukkit.setServer(new TestBukkitServer());
		TestObjects.initNMSReflection();
		TestObjects.setBukkitVersion("1_8_R1");
	}

	@Test
	public void testVersionComparison()
	{
		assertTrue("The version should match", MCVersion.isOlderOrEqualThan(MCVersion.MC_1_8));
		assertTrue("The version should match", MCVersion.isOlderOrEqualThan(MCVersion.MC_1_9));
		assertFalse("The version should not match", MCVersion.isOlderOrEqualThan(MCVersion.MC_1_7));
		assertTrue("The version should match", MCVersion.is(MCVersion.MC_1_8));
		assertFalse("The version should not match", MCVersion.is(MCVersion.MC_1_9));
		assertTrue("The version should match", MCVersion.MC_1_8.isSame(MCVersion.MC_1_8));
		assertFalse("The version should not match", MCVersion.MC_1_8.isSame(MCVersion.MC_1_9));
		assertTrue("The version should match", MCVersion.MC_1_8.newerOrEqualThan(MCVersion.MC_1_8));
		assertFalse("The version should not match", MCVersion.MC_1_8.newerOrEqualThan(MCVersion.MC_1_9));
		assertTrue("The version should match", MCVersion.MC_1_8.newerThan(MCVersion.MC_1_7));
		assertFalse("The version should not match", MCVersion.MC_1_8.newerThan(MCVersion.MC_1_9));
		assertFalse("The version should not match", MCVersion.MC_1_8.newerThan(MCVersion.UNKNOWN));
		assertFalse("The version should not match", MCVersion.MC_1_8.newerOrEqualThan(MCVersion.UNKNOWN));
		assertFalse("The version should not match", MCVersion.UNKNOWN.newerOrEqualThan(MCVersion.MC_1_8));
		assertTrue("The version should match", MCVersion.MC_1_8.olderOrEqualThan(MCVersion.MC_1_8));
		assertFalse("The version should not match", MCVersion.MC_1_8.olderOrEqualThan(MCVersion.MC_1_7));
		assertTrue("The version should match", MCVersion.MC_1_8.olderThan(MCVersion.MC_1_9));
		assertFalse("The version should not match", MCVersion.MC_1_8.olderThan(MCVersion.MC_1_8));
	}

	@Test
	public void testUnknownVersion()
	{
		assertTrue(MCVersion.UNKNOWN.isSame(MCVersion.UNKNOWN));
		assertFalse(MCVersion.isOlderOrEqualThan(MCVersion.UNKNOWN));
		assertFalse(MCVersion.isOlderThan(MCVersion.UNKNOWN));
		assertFalse(MCVersion.isNewerOrEqualThan(MCVersion.UNKNOWN));
		assertFalse(MCVersion.isNewerThan(MCVersion.UNKNOWN));
		assertFalse(MCVersion.is(MCVersion.UNKNOWN));
		assertFalse(MCVersion.UNKNOWN.isSame(MCVersion.MC_1_7));
		assertTrue(MCVersion.UNKNOWN.olderOrEqualThan(MCVersion.UNKNOWN));
		assertFalse(MCVersion.UNKNOWN.olderThan(MCVersion.UNKNOWN));
		assertTrue(MCVersion.UNKNOWN.newerOrEqualThan(MCVersion.UNKNOWN));
		assertFalse(MCVersion.UNKNOWN.newerThan(MCVersion.UNKNOWN));
		assertFalse(MCVersion.UNKNOWN.olderOrEqualThan(MCVersion.MC_1_7));
		assertFalse(MCVersion.UNKNOWN.olderThan(MCVersion.MC_1_7));
		assertFalse(MCVersion.UNKNOWN.newerOrEqualThan(MCVersion.MC_1_7));
		assertFalse(MCVersion.UNKNOWN.newerThan(MCVersion.MC_1_7));
	}

	@Test
	public void testGetMainMinecraftVersion()
	{
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7_1.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7_2.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7_3.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7_4.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7_5.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7_6.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7_7.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7_8.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7_9.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_1_7_10.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_NMS_1_7_R1.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_NMS_1_7_R2.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_NMS_1_7_R3.getMajorMinecraftVersion());
		assertEquals(MCVersion.MC_1_7, MCVersion.MC_NMS_1_7_R4.getMajorMinecraftVersion());
	}

	@Test
	public void testIsAnyMajorVersion()
	{
		assertTrue(MCVersion.MC_1_7.isSameMajorVersion(MCVersion.MC_1_7_10));
		assertFalse(MCVersion.MC_1_8_8.isSameMajorVersion(MCVersion.MC_1_7_10));
	}

	@Test
	public void testIsAny()
	{
		assertTrue(MCVersion.isAny(MCVersion.MC_1_8_8));
		assertFalse(MCVersion.isAny(MCVersion.MC_1_7_10));
	}

	@Test
	public void testUUIDsSupportFlag()
	{
		assertFalse(MCVersion.MC_1_7.areUUIDsSupported());
		assertTrue(MCVersion.MC_1_8.areUUIDsSupported());
		assertTrue(MCVersion.isUUIDsSupportAvailable());
	}

	@Test
	public void testIsDualWielding()
	{
		assertFalse(MCVersion.isDualWieldingMC());
		for(MCVersion version : MCVersion.values())
		{
			boolean dualWielding = version.newerOrEqualThan(MCVersion.MC_1_9);
			assertEquals("MC version " + version.toString() + " should " + (dualWielding ? "" : "not ") + "be dual wielding!", dualWielding, version.isDualWielding());
		}
	}

	@Test
	public void testProtocolVersion()
	{
		assertEquals(3, MCVersion.MC_1_7.getProtocolVersion());
		assertEquals(3, MCVersion.MC_1_7_1.getProtocolVersion());
		assertEquals(4, MCVersion.MC_1_7_2.getProtocolVersion());
		assertEquals(4, MCVersion.MC_1_7_3.getProtocolVersion());
		assertEquals(4, MCVersion.MC_1_7_4.getProtocolVersion());
		assertEquals(5, MCVersion.MC_1_7_5.getProtocolVersion());
		assertEquals(5, MCVersion.MC_1_7_6.getProtocolVersion());
		assertEquals(5, MCVersion.MC_1_7_7.getProtocolVersion());
		assertEquals(5, MCVersion.MC_1_7_8.getProtocolVersion());
		assertEquals(5, MCVersion.MC_1_7_9.getProtocolVersion());
		assertEquals(5, MCVersion.MC_1_7_10.getProtocolVersion());
		assertEquals(4, MCVersion.MC_NMS_1_7_R1.getProtocolVersion());
		assertEquals(5, MCVersion.MC_NMS_1_7_R2.getProtocolVersion());
		assertEquals(5, MCVersion.MC_NMS_1_7_R3.getProtocolVersion());
		assertEquals(5, MCVersion.MC_NMS_1_7_R4.getProtocolVersion());
		assertEquals(47, MCVersion.MC_1_8_2.getProtocolVersion());
		assertEquals(340, MCVersion.MC_1_12_2.getProtocolVersion());
		assertEquals(404, MCVersion.MC_1_13_2.getProtocolVersion());
	}

	@Test
	public void testGetFromProtocolVersion()
	{
		assertEquals(MCVersion.MC_1_8_9, MCVersion.getFromProtocolVersion(47));
		assertEquals(MCVersion.MC_1_12_2, MCVersion.getFromProtocolVersion(340));
		assertEquals(MCVersion.MC_1_13_2, MCVersion.getFromProtocolVersion(404));
		assertEquals(MCVersion.UNKNOWN, MCVersion.getFromProtocolVersion(Integer.MIN_VALUE));
	}

	@Test
	public void testVersionName()
	{
		assertEquals("1.7.9", MCVersion.MC_1_7_9.getName());
		assertEquals("1.8.9", MCVersion.MC_1_8_9.getName());
		assertEquals("1.9.4", MCVersion.MC_1_9_4.getName());
		assertEquals("1.12.2", MCVersion.MC_1_12_2.getName());
		assertEquals("1.13.2", MCVersion.MC_1_13_2.getName());
		assertEquals("1.15.2", MCVersion.MC_1_15_2.getName());
		assertEquals("1.15_NMS_R1", MCVersion.MC_NMS_1_15_R1.getName());
		assertEquals("UNKNOWN", MCVersion.UNKNOWN.getName());
		assertEquals("1.15.2", MCVersion.MC_1_15_2.toString());
	}

	@Test
	public void testGetFromVersionName()
	{
		assertEquals(MCVersion.MC_1_7_9, MCVersion.getFromVersionName("1.7.9"));
		assertEquals(MCVersion.MC_1_8_9, MCVersion.getFromVersionName("1.8.9"));
		assertEquals(MCVersion.MC_1_12_2, MCVersion.getFromVersionName("1.12.2"));
		assertEquals(MCVersion.MC_1_13_2, MCVersion.getFromVersionName("1.13.2"));
		assertEquals(MCVersion.MC_1_15_2, MCVersion.getFromVersionName("1.15.2"));
		assertEquals(MCVersion.UNKNOWN, MCVersion.getFromVersionName("1.1.2"));
		assertEquals(MCVersion.UNKNOWN, MCVersion.getFromVersionName("nothing"));
		assertEquals(MCVersion.UNKNOWN, MCVersion.getFromVersionName("UNKNOWN"));
	}
}