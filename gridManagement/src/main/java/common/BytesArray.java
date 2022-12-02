package common;

import util.ByteArrayUtils;

import javax.validation.constraints.NotNull;

public class BytesArray implements Comparable<BytesArray> {

    private byte[][] bytesArr;

    public BytesArray(byte[][] arr){
        this.bytesArr = arr;
    }

    @Override
    public int compareTo(@NotNull BytesArray o) {
        for(int i = 0 ; i < bytesArr.length && i < o.bytesArr.length; i++){
            int result;
            if (i==0) {
                result = ByteArrayUtils.compareRowKey(bytesArr[i], o.bytesArr[i]);
            } else {
                result = ByteArrayUtils.compare(bytesArr[i], o.bytesArr[i]);
            }
            if(result != 0)
                return result;
        }
        return bytesArr.length - o.bytesArr.length;
    }
}


