package ru.shemplo.dsau.utils;

public final class MurmurHash {

	private static final int DEFAULT_SEED = 0;
	
	private int hash;
	
	public MurmurHash (int seed) {
		this.hash = seed;
	}
	
	public MurmurHash () {
		this (DEFAULT_SEED);
	}

	public MurmurHash update (int value) {
		final int c1 = 0xCC9E2D51;
		final int c2 = 0x1B873593;
		final int r1 = 15;
		final int r2 = 13;
		final int m = 5;
		final int n = 0xE6546B64;

		int k = value;
		k = k * c1;
		k = (k << r1) | (k >>> (32 - r1));
		k = k * c2;

		hash = hash ^ k;
		hash = (hash << r2) | (hash >>> (32 - r2));
		hash = hash * m + n;

		return this;
	}

	public MurmurHash update (Object value) {
		update (value != null ? value.hashCode () : 0);
		return this;
	}

	public int finish (int numberOfWords) {
		hash = hash ^ (numberOfWords * 4);
		hash = hash ^ (hash >>> 16);
		hash = hash * 0x85EBCA6B;
		hash = hash ^ (hash >>> 13);
		hash = hash * 0xC2B2AE35;
		hash = hash ^ (hash >>> 16);
		
		return hash;
	}

	public static <T> int hashCode (T [] data, int seed) {
		MurmurHash engine = new MurmurHash (seed);
		
		for (T value : data) { 
			engine.update (value); 
		}

		return engine.finish (data.length);
	}
	
}
