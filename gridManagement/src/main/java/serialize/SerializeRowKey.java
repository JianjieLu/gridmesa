package serialize;

import constant.CommonConstants;
import constant.ResourceParseConsts;
import org.apache.hadoop.hbase.util.Bytes;
import util.ByteArrayUtils;

/**
 * 序列化rowKey：XZ2索引+ID.
 */
public class SerializeRowKey {

    // XZ2Index占用的byte;
    protected int XZ2Length;
    // id占用的byte；
    protected int idLength;

    protected int levelLength;
    protected byte[] XZ2Index;
    protected byte[] level = new byte[1];
    protected byte[] id;

    public static SerializeRowKey fromXZ2andId(String XZ2Index, String id) {
        return new Builder()
                .setXZ2Index(XZ2Index)
                .setId(id)
                .build();
    }

    public static SerializeRowKey fromByteArray(byte[] data) {
        return new Builder(data)
                .build();
    }

    /**
     * 序列化XZ2Index
     * @MBR 输入XZ2IndexString
    * */

    protected void setXZ2Index(String XZ2IndexStr) {
        int indexLevel = XZ2IndexStr.length()/2;
        level[0] = (byte) indexLevel;
        XZ2Index = ByteArrayUtils.binaryStringToIndex(XZ2IndexStr);
        setXZ2Length();
        setLevelLength();
    }


    /**
     * 序列化ID
     * @MBR 输入idString;
     * */
    protected void setId(String idStr) {
        id = Bytes.toBytes(ResourceParseConsts.TABLE_ROWKEY_ID_CONNECTOR + idStr);
        setIdLength();
    }


    protected void setIdLength() {
        this.idLength = id.length;
    }

    protected void setXZ2Length() {
        this.XZ2Length = XZ2Index.length;
    }

    protected void setLevelLength() {
        this.levelLength = 1;
    }

    protected void setData(final byte[] rowkey) {

        int length = rowkey.length;

        int i = length - 1;
        for (; i >= 0; i--) {
            if (rowkey[i] == (byte) 95) {
                break;
            }
        }
        XZ2Index = new byte[i-1];
        level = new byte[1];
        // 去掉分隔符;
        id = new byte[length-i-1];
        System.arraycopy(rowkey, 0, XZ2Index, 0, i-1);
        System.arraycopy(rowkey, i-1, level, 0, 1);
        System.arraycopy(rowkey, i+1, id, 0, length-i-1);
    }


    /**
     * Get the binary value of rowKey. Use to store in the database.
     *
     * @return byte[] binary value of rowKey.
     */
    public byte[] getData() {
        int len=XZ2Length+1+idLength;
        byte[] data = new byte[len];
        System.arraycopy(XZ2Index, 0, data, 0, XZ2Length);
        System.arraycopy(level, 0, data, XZ2Length, 1);
        System.arraycopy(id, 0, data, XZ2Length+1, idLength);
        return data;
    }

    public byte[] getXZ2Index() {
        return XZ2Index;
    }

    public byte[] getLevel() {
        return level;
    }

    public byte[] getId() {
        return id;
    }

    /**
     * Builder
     */
    public static class Builder {
        private SerializeRowKey rowkey = new SerializeRowKey();

        public Builder setXZ2Index(String XZ2IndexStr) {
            rowkey.setXZ2Index(XZ2IndexStr);
            return this;
        }

        public Builder setId(String idStr) {
            rowkey.setId(idStr);
            return this;
        }

        public Builder(byte[] data) {
            rowkey.setData(data);
        }
        public Builder() {
        }

        //public Builder
        public SerializeRowKey build() {
            return this.rowkey;
        }
    }
}
