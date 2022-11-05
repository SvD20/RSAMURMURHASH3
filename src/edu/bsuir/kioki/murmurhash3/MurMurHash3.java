package edu.bsuir.kioki.murmurhash3;

public class MurMurHash3 {

    @Deprecated
    public static final long NULL_HASHCODE = 2862933555777941757L;

    public static final int DEFAULT_SEED = 104729;

    // Constants for 128-bit variant
    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;
    private static final int R1 = 31;
    private static final int R2 = 27;
    private static final int M = 5;
    private static final int N1 = 0x52dce729;

    /** No instance methods. */
    private MurMurHash3() {
    }

    @Deprecated
    public static long hash64(final byte[] data) {
        return hash64(data, 0, data.length, DEFAULT_SEED);
    }


    @Deprecated
    public static long hash64(final byte[] data, final int offset, final int length, final int seed) {
        // ************
        // Note: This fails to apply masking using 0xffffffffL to the seed.
        // ************
        long hash = seed;
        final int nblocks = length >> 3;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int index = offset + (i << 3);
            long k = getLittleEndianLong(data, index);

            // mix functions
            k *= C1;
            k = Long.rotateLeft(k, R1);
            k *= C2;
            hash ^= k;
            hash = Long.rotateLeft(hash, R2) * M + N1;
        }

        // tail
        long k1 = 0;
        final int index = offset + (nblocks << 3);
        switch (offset + length - index) {
            case 7:
                k1 ^= ((long) data[index + 6] & 0xff) << 48;
            case 6:
                k1 ^= ((long) data[index + 5] & 0xff) << 40;
            case 5:
                k1 ^= ((long) data[index + 4] & 0xff) << 32;
            case 4:
                k1 ^= ((long) data[index + 3] & 0xff) << 24;
            case 3:
                k1 ^= ((long) data[index + 2] & 0xff) << 16;
            case 2:
                k1 ^= ((long) data[index + 1] & 0xff) << 8;
            case 1:
                k1 ^= ((long) data[index] & 0xff);
                k1 *= C1;
                k1 = Long.rotateLeft(k1, R1);
                k1 *= C2;
                hash ^= k1;
        }

        // finalization
        hash ^= length;
        hash = fmix64(hash);

        return hash;
    }

    private static long getLittleEndianLong(final byte[] data, final int index) {
        return (((long) data[index    ] & 0xff)      ) |
                (((long) data[index + 1] & 0xff) <<  8) |
                (((long) data[index + 2] & 0xff) << 16) |
                (((long) data[index + 3] & 0xff) << 24) |
                (((long) data[index + 4] & 0xff) << 32) |
                (((long) data[index + 5] & 0xff) << 40) |
                (((long) data[index + 6] & 0xff) << 48) |
                (((long) data[index + 7] & 0xff) << 56);
    }

    private static long fmix64(long hash) {
        hash ^= (hash >>> 33);
        hash *= 0xff51afd7ed558ccdL;
        hash ^= (hash >>> 33);
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= (hash >>> 33);
        return hash;
    }

}
