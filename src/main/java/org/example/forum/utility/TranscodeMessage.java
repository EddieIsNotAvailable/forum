package org.example.forum.utility;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;

public class TranscodeMessage {

    //Unicode sum of String characters
    private static int getStringValue(String text) {
        int value = 0;
        for (int i = 0; i < text.length(); i++)
            value += text.charAt(i);

        return value;
    }

    public static int getOffsetBitSize(String text) {
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
//                System.out.println("Setting bit at index " + index + " to " + bit);
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
                System.out.println("Setting bit at index " + index + " to " + bit);
                index += periods[i % periods.length];
            }
        }

        byte[] nbytes = carrierBits.toByteArray();

        for (i=0; i < nbytes.length; i++) {
            carrier[i] = nbytes[i];
        }

//        BitSet somebits = BitSet.valueOf(carrier); //RM
//        for(i=0; i<somebits.size(); i++) {
//            if(somebits.get(i)) System.out.println("File [" + i + "] = 1");
//        }
//        int idx=offset; //RM
//        for(i=0; i< messageBits.size(); i++) {
//            System.out.println("File idx [" + idx + "] = " + (somebits.get(idx) ? 1 : 0));
//            idx += periods[i % periods.length];
//        }

        return carrier;
    }

    public static int[] getPeriods(String text) {
        //Split subject into words, remove empty strings
        String[] words = text.split(" ");
        words = Arrays.stream(words).filter(word -> !word.isEmpty()).toArray(String[]::new);
        if(words.length == 0) throw new IllegalArgumentException("Invalid subject");
        return Arrays.stream(words).mapToInt(TranscodeMessage::getPeriodBitSize).toArray();
    }
}
