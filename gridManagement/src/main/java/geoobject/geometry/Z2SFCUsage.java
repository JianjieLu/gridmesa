package geoobject.geometry;

import org.locationtech.geomesa.curve.Z2SFC;
import org.locationtech.sfcurve.zorder.Z2;

public class Z2SFCUsage {

    public static void main1(String[] args) {
        double lon = 1.1;
        double lat = 1.1;

        Z2SFC z2SFC = new Z2SFC(1);//precision表示层级，每一层对空间四分


        long index = z2SFC.index(lon, lat, false);//x，y表示经纬度，lenient表示经纬度能否超过范【-180， 180】【-90，90】范围，
        //生成的index使用二进制字符串表示z-value，但是从其值自身无法知道其位于的层级
        // index使用最低层级，对于同一level的z-value，不影响使用，因为前缀都是0
        //其生成的内部实际上使用了Z2类

        long index2 = Z2.apply(z2SFC.lon().normalize(lon), z2SFC.lat().normalize(lat));//x、y表示标准化之后的经纬度
        //可以认为将平面空间划分为固定层级的网格，z2SFC.lon().normalize(lon), z2SFC.lat().normalize(lat)分别表示网格单元的横纵坐标
        //Z2.apply把网格当成最低层级进行处理，因为他只是将x，y的bit进行交叉组合



        //System.out.println(index + "  " + index2);
        /*System.out.println(index);
        System.out.println(Long.toBinaryString(index));
        System.out.println(z2.invert(index));
        System.out.println(z2.lon().no + "  " + z2.lat());*/
    }

}
