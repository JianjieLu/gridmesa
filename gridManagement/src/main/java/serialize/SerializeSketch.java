package serialize;

import constant.CommonConstants;
import geoobject.BoundingBox;
import index.util.BitUtil;
import org.locationtech.jts.geom.Envelope;
import util.BitSetUtil;
import java.util.BitSet;

/**
 * 序列化数据概要：MBR+Signature.
 */
public class SerializeSketch {

    private static final int PRECISION = CommonConstants.PRECISION;
    // 四个坐标占用的byte；
    protected int MBRLength = 16;
    // 签名占用的byte；
    protected int signatureLength;
    protected byte[] MBRValue;
    protected byte[] signatureValue;

    public static SerializeSketch fromMBRandSignature(Envelope mbr, BitSet signature) {
        return new Builder()
                .setMBR(mbr)
                .setSignature(signature)
                .build();
    }

    public static SerializeSketch fromByteArray(byte[] data) {
        return new SerializeSketch.Builder(data)
                .build();
    }

    /**
     * 序列化MBR
     * @MBR 输入MBR
    * */

    protected void setMBR(Envelope MBR) {
        double minX = MBR.getMinX();
        double minY = MBR.getMinY();
        double maxX = MBR.getMaxX();
        double maxY = MBR.getMaxY();
        int minLng = (int) (minX * PRECISION);
        int minLat = (int) (minY * PRECISION);
        int maxLng = (int) (maxX * PRECISION);
        int maxLat = (int) (maxY * PRECISION);
        MBRValue = new byte[MBRLength];
        int offset = 0;
        BitUtil.int32ToBinary(minLng, MBRValue, offset);
        BitUtil.int32ToBinary(minLat, MBRValue, offset + 4);
        BitUtil.int32ToBinary(maxLng, MBRValue, offset + 8);
        BitUtil.int32ToBinary(maxLat,MBRValue,offset + 12);
    }
    /**
     * 序列化signature
     * @MBR 输入BitSet;
     * */
    protected void setSignature(BitSet signature) {
        signatureValue = BitSetUtil.bitSet2ByteArray(signature);
        setSignatureLength();
    }


    protected void setSignatureLength() {
        this.signatureLength = signatureValue.length;
    }

    public void setData(byte[] sketch) {
        int length = sketch.length;
        MBRValue = new byte[MBRLength];
        signatureLength = length-MBRLength;
        signatureValue = new byte[signatureLength];
        System.arraycopy(sketch, 0, MBRValue, 0, MBRLength);
        System.arraycopy(sketch, 4, signatureValue, 0, signatureLength);
    }

    public byte[] getData() {
        int len = MBRLength+signatureLength;
        byte[] data = new byte[len];
        System.arraycopy(MBRValue, 0, data, 0, MBRLength);
        System.arraycopy(signatureValue, 0, data, MBRLength, signatureLength);
        return data;
    }

    public BoundingBox getMBR() {
        int offset = 0;
        double minLng = ((double) BitUtil.binaryToInt32(MBRValue, offset)) / PRECISION;
        double minLat = ((double) BitUtil.binaryToInt32(MBRValue, offset + 4)) / PRECISION;
        double maxLng = ((double) BitUtil.binaryToInt32(MBRValue, offset + 8)) / PRECISION;
        double maxLat = ((double) BitUtil.binaryToInt32(MBRValue, offset + 12)) / PRECISION;
        return new BoundingBox(minLng, minLat, maxLng, maxLat);
    }

    public BitSet getSignature() {
        return BitSetUtil.byteArray2BitSet(signatureValue);
    }

    /**
     * Builder
     */
    public static class Builder {
        private SerializeSketch sketch = new SerializeSketch();

        public Builder() {
        }

        public Builder(byte[] data) {
            sketch.setData(data);
        }

        public Builder setMBR(Envelope MBR) {
            sketch.setMBR(MBR);
            return this;
        }

        public Builder setSignature(BitSet signature) {
            sketch.setSignature(signature);
            return this;
        }

        //public Builder
        public SerializeSketch build() {
            return this.sketch;
        }
    }
}