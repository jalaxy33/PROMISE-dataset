package net.sourceforge.pbeans.util;

import java.util.Random;

public class Hash {
	private static final Random RANDOM1 = new Random (System.currentTimeMillis() ^ Runtime.getRuntime().freeMemory());
	private static final Random RANDOM2 = new Random (System.nanoTime() ^ System.identityHashCode(Hash.class));
	
	public static Long generateLong() {
		return Math.abs(RANDOM1.nextLong() ^ RANDOM2.nextLong());
	}

	public static int fixedHash (String text) {
		// Note: Never change the implementation of this method.
		// It should remain unchanged across JVM versions, etc.
		byte[] hashBytes = new byte[4];
		int len = text.length();
		for (int i = 0; i < len; i++) {
			int hi = i % 4;
			hashBytes[hi] ^= (byte) text.charAt(i);
		}
		return hashBytes[0] + hashBytes[1] << 8 + hashBytes[2] << 16 + hashBytes[3] << 24;
	}
}
