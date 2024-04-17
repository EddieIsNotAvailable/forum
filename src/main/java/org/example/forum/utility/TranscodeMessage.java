package org.example.forum.utility;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;

public class TranscodeMessage {

//    //4000 bytes of bits
//    private static final int BASE_OFFSET_BIT_SIZE = 32000;
//
//    //1 byte of bits
//    private static final int BASE_PERIOD_BIT_SIZE = 8;
//
//    public static int getOffsetBitSize(String text, int additionalBits) {
//        return BASE_OFFSET_BIT_SIZE + getStringValue(text) + additionalBits;
//    }
//
//    public static int getPeriodBitSize(String text, int additionalBits) {
//        return BASE_PERIOD_BIT_SIZE + getStringBitValue(text) + additionalBits;
//    }

    //Counts unicode sum of String characters
    private static int getStringValue(String text) {
        int value = 0;
        for (int i = 0; i < text.length(); i++)
            value += text.charAt(i);

        return value;
    }
//
//    //Counts set bits in string
//    private static int getStringBitValue(String text) {
//        int value = 0;
//        BitSet messageBits = BitSet.valueOf(text.getBytes());
//        for (int i = 0; i < messageBits.size(); i++)
//            if (messageBits.get(i)) value++;
//
//        return value;
//    }

//    public static byte[] encodeMessage(byte[] carrier, String message, int offset, int period) {
//        BitSet carrierBits = BitSet.valueOf(carrier);
//        BitSet messageBits = BitSet.valueOf(message.getBytes());
//
//        int neededBits = (messageBits.size() * period) - period + 1; //(-period and +1) since full period size not required for last bit
//        if (carrierBits.size() < neededBits + offset) {
//            throw new IllegalArgumentException("Carrier too small");
//        }
//
//        boolean bit; int index;
//        for (int i = 0; i < messageBits.size(); i++) {
//            bit = messageBits.get(i);
//            index = offset + (i * period);
//            carrierBits.set(index, bit);
//        }
//        return carrierBits.toByteArray();
//    }
//
//    //Poor design, messages prefixed with the encoded message may be valid
//    //Also poor design in that could leak message by how quickly it rejects
//    public static boolean decodeMessage(byte[] carrier, String message, int offset, int period) {
//        BitSet carrierBits = BitSet.valueOf(carrier);
//        BitSet messageBits = BitSet.valueOf(message.getBytes());
//
//        boolean mbit, cbit;
//        int index = offset;
//        for (int i = 0; i < messageBits.size(); i++) {
//            mbit = messageBits.get(i);
//            cbit = carrierBits.get(index);
//            index += period;
//            if(mbit != cbit) return false;
//        }
//        return true;
//    }

    public static int getOffsetBitSize(String text) {
//        return text.getBytes().length * 8;
        return getStringValue(text);
    }

    public static int getPeriodBitSize(String text) {
        return text.getBytes().length * 8;
    }

    public static byte[] encodeMessage(byte[] carrier, String message, int offset, int[] periods) {

        BitSet carrierBits = BitSet.valueOf(carrier);
        BitSet messageBits = BitSet.valueOf(message.getBytes(StandardCharsets.UTF_8));
        int carrierSize = carrier.length * 8;

        int i;
        if (periods.length == 1) {
            int period = periods[0];
            int neededBits = (messageBits.size() * period) - period + 1 + offset; //(-period and +1) since full period size not required for last bit

            //Must use carrier length, not bitset length, since bitset will exclude ending zero bytes
            if (carrierSize < neededBits) throw new IllegalArgumentException("Carrier too small");

            boolean bit; int index = offset;
            for (i = 0; i < messageBits.size(); i++) {
                bit = messageBits.get(i);
                carrierBits.set(index, bit);
                System.out.println("Setting bit at index " + index + " to " + bit);
                index += period;
            }
        } else {
            int period_sum = Arrays.stream(periods).sum();
            int num = messageBits.size() / periods.length;
            int rem = messageBits.size() % periods.length;
            int neededBits = num * period_sum;
            if(rem > 0) {
                for(i=0; i < rem; i++) neededBits += periods[i];
                neededBits -= periods[rem - 1];
            }

            neededBits += 1 + offset;
            if (carrierSize < neededBits) throw new IllegalArgumentException("Carrier too small");

            boolean bit; int index = offset;
            for (i=0; i<messageBits.size(); i++) {
                bit = messageBits.get(i);
                carrierBits.set(index, bit);
                index += periods[i % periods.length];
            }
        }

        byte[] nbytes = carrierBits.toByteArray();

        for (i=0; i < nbytes.length; i++) {
            carrier[i] = nbytes[i];
        }

        BitSet somebits = BitSet.valueOf(carrier);
        for(i=0; i<somebits.size(); i++) {
            if(somebits.get(i)) System.out.println("File [" + i + "] = 1");
        }
        int idx=offset;
        for(i=0; i< messageBits.size(); i++) {
            System.out.println("File idx [" + idx + "] = " + (somebits.get(idx) ? 1 : 0));
            idx += periods[i % periods.length];
        }

        return carrier;
    }

    //Poor design:
    //  accepts passwords of the first part of the actual password
    //  messages prefixed with the encoded message may be valid
    //Also poor design in that could leak message by how quickly it rejects
//    public static boolean decodeMessage(byte[] carrier, String message, int offset, int[] periods) {
//        BitSet carrierBits = BitSet.valueOf(carrier);
//        BitSet messageBits = BitSet.valueOf(message.getBytes());
//
//        boolean mbit, cbit;
//        int i, index = offset;
//        if(periods.length == 1) {
//            for (i = 0; i < messageBits.size(); i++) {
//                mbit = messageBits.get(i);
//                cbit = carrierBits.get(index);
//                index += periods[0];
//                if (mbit != cbit) return false;
//            }
//        } else {
//            for (i = 0; i < messageBits.size(); i++) {
//                mbit = messageBits.get(i);
//                cbit = carrierBits.get(index);
//                index += periods[i % periods.length];
//                if(mbit != cbit) return false;
//            }
//        }
//        return true;
//    }

    public static int[] getPeriods(String text) {
        //Split subject into words, remove empty strings
        String[] words = text.split(" ");
        words = Arrays.stream(words).filter(word -> !word.isEmpty()).toArray(String[]::new);
        if(words.length == 0) throw new IllegalArgumentException("Invalid subject");
        return Arrays.stream(words).mapToInt(TranscodeMessage::getPeriodBitSize).toArray();
    }
}
