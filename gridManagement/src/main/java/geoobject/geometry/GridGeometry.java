package geoobject.geometry;


import org.locationtech.geomesa.curve.Z2SFC;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.sfcurve.zorder.Z2;
import scala.Tuple2;

import java.util.*;

public abstract class GridGeometry {

    protected List<SplitGeometry> splitGeoms;

    protected List<Grid> splitGrids;

    protected Map<String,Grid> XZ2Grids;

    protected String id;

    protected Geometry geometry;

    protected String XZ2Index;

    // signature
    protected BitSet signature;

    public GridGeometry(Geometry geometry) {
        this.id = UUID.randomUUID().toString().replace("-","");
        this.geometry = geometry;
        this.splitGeoms = new ArrayList<>();
    }

    public GridGeometry(String id,Geometry geometry) {
        this.id = id;
        this.geometry = geometry;
        this.splitGeoms = new ArrayList<>();
    }

    /**
     * 计算geometry的XZ2Index
     * @param geometry 空间对象;
     * @return
     */
    public void computeXZ2Index(Geometry geometry){

        XZ2Grids = new HashMap<>();
        Envelope e = geometry.getEnvelopeInternal();
        // 确定初始层级;
        int initialLevel = (int) Math.floor(Math.log(360 / Math.max(e.getWidth(), e.getHeight() * 2)) / Math.log(2));
        Z2SFC z2SFC = new Z2SFC(initialLevel);
        //MBR的左下角格网
        Grid leftBottom = new Grid(initialLevel, z2SFC.index(e.getMinX(), e.getMinY(), false));
        //MBR的右上角格网
        Grid rightTop = new Grid(initialLevel, z2SFC.index(e.getMaxX(), e.getMaxY(), false));
        // XZ2索引的左下角格网
        Grid XZ2LeftBottom = leftBottom;
        //如果只有一个网格, 继续划分一级;
        if (leftBottom.getIndex() == rightTop.getIndex()){
            z2SFC = new Z2SFC(initialLevel+1);
            XZ2LeftBottom = new Grid(initialLevel+1, z2SFC.index(e.getMinX(), e.getMinY(), false));
        }

        Z2 z2 = new Z2(XZ2LeftBottom.getIndex());
        Tuple2<Object, Object> XY = z2.decode();
        // XZ2索引的右上角格网；
        Grid XZ2RightTop = new Grid(XZ2LeftBottom.getLevel(), Z2.apply((Integer) XY._1() + 1, (Integer) XY._2() + 1));
        Grid XZ2LeftTop = new Grid(XZ2LeftBottom.getLevel(), Z2.apply((Integer) XY._1(), (Integer) XY._2() + 1));
        Grid XZ2RightBottom = new Grid(XZ2LeftBottom.getLevel(), Z2.apply((Integer) XY._1() + 1, (Integer) XY._2()));

        // 左下角即为XZ2索引
        XZ2Index = XZ2LeftBottom.getBinaryStrIndex();
        // 返回XZ2格网信息用于计算signature
        XZ2Grids.put("leftBottom",XZ2LeftBottom);
        XZ2Grids.put("rightTop",XZ2RightTop);
        XZ2Grids.put("leftTop",XZ2LeftTop);
        XZ2Grids.put("rightBottom",XZ2RightBottom);
    }
    /**
     * 计算geometry的签名;
     * @param geometry 空间对象;
     * @param recursiveTimes 下分次数,不小于1;
     * @return
     */
    public void computeSignature(Geometry geometry,int recursiveTimes){
        // 确定初始层级;
        int indexLevel = XZ2Index.length()/2;
        splitGrids = new ArrayList<>();
        // XZ2索引默认划分一次，所以recursiveTimes不小于1;
        int signatureLevel = indexLevel + recursiveTimes-1;
        // 太小就丢弃;
//        System.out.println("signatureLevel:"+signatureLevel);
        if (signatureLevel>31) return;
        int signatureSize = (int) Math.pow(2, recursiveTimes);
        signature = new BitSet(signatureSize*signatureSize);
        Grid leftBottom = XZ2Grids.get("leftBottom");
        Grid rightTop = XZ2Grids.get("rightTop");
        long leftBottomIndex = leftBottom.getIndex();
        long rightTopIndex = rightTop.getIndex();
        // 取最小、最大格网;
        for (int i=0;i<(recursiveTimes-1);i++){
            leftBottomIndex = leftBottomIndex << 2;
            rightTopIndex = rightTopIndex << 2 | 3;
        }
        // 转为行列号;
        Z2 leftBottomZ2 = new Z2(leftBottomIndex);
        Z2 rightTopZ2 = new Z2(rightTopIndex);
        Tuple2<Object, Object> leftBottomXY = leftBottomZ2.decode();
        Tuple2<Object, Object> rightTopXY = rightTopZ2.decode();
        // X、Y分别为经纬度的行列号;
        int leftBottomX = (Integer) leftBottomXY._1();
        int leftBottomY = (Integer) leftBottomXY._2();
        int rightTopX = (Integer) rightTopXY._1();
        int rightTopY = (Integer) rightTopXY._2();

//        Envelope startEnvelope = leftBottom.getEnvelope();
//        Envelope endEnvelope = rightTop.getEnvelope();
//        double MinX = startEnvelope.getMinX();
//        double MinY = startEnvelope.getMinY();
//        double MaxX = endEnvelope.getMaxX();
//        double MaxY = endEnvelope.getMaxY();
//        double detaX = (MaxX - MinX) / signatureSize;
//        double detaY = (MaxY - MinY) / signatureSize;

        // 判断signature与grids是否匹配
        if ((rightTopX-leftBottomX+1)!=signatureSize || (rightTopY-leftBottomY+1)!=signatureSize)
            throw new RuntimeException("signatureSize is not match!");

        for (int i=0; i<signatureSize; i++){
            for (int j=0; j<signatureSize;j++){
                Grid tempGrid = new Grid(signatureLevel, Z2.apply(leftBottomX + i, leftBottomY + j));
//                System.out.println("tempGrid:"+tempGrid.getLevel());
                if (geometry.intersects(tempGrid.getPolygon())){
                    splitGrids.add(tempGrid);

                    signature.set((i*signatureSize)+j);
//                    double gridMinX = MinX + detaX * i;
//                    double gridMaxX = MinX + detaX * (i+1);
//                    double gridMinY = MinY + detaY*j;
//                    double gridMaxY = MinY + detaY*(j+1);
//                    System.out.println(tempGrid.getPolygon());
//                    System.out.println("test:"+gridMinX+" "+gridMinY+" "+gridMaxX+" "+gridMaxY);
//                    System.out.println("------------------");
                }
            }
        }
    }


    // 将Geometry 分解为固定等级格网;
    public void computeGrids(Geometry geometry,int level){
        splitGrids = new ArrayList<>();
        Envelope e = geometry.getEnvelopeInternal();
        Z2SFC z2SFC = new Z2SFC(level);
        //MBR的左下角格网
        Grid leftBottom = new Grid(level, z2SFC.index(e.getMinX(), e.getMinY(), false));
        //MBR的右上角格网
        Grid rightTop = new Grid(level, z2SFC.index(e.getMaxX(), e.getMaxY(), false));
        //如果只有一个网格;
        if (leftBottom.getIndex() == rightTop.getIndex()){
            splitGrids.add(leftBottom);
        } else{
            // 转为行列号;
            Z2 leftBottomZ2 = new Z2(leftBottom.getIndex());
            Z2 rightTopZ2 = new Z2(rightTop.getIndex());
            Tuple2<Object, Object> leftBottomXY = leftBottomZ2.decode();
            Tuple2<Object, Object> rightTopXY = rightTopZ2.decode();
            // X、Y分别为经纬度的行列号;
            int minX = (Integer) leftBottomXY._1();
            int minY = (Integer) leftBottomXY._2();
            int maxX = (Integer) rightTopXY._1();
            int maxY = (Integer) rightTopXY._2();
            for (int i=minX;i<=maxX;i++){
                for (int j=minY;j<=maxY;j++){
                    Grid grid;
                    if (i==minX & j==minY) {grid = leftBottom;}
                    else if (i==maxX & j==maxY) {grid = rightTop;}
                    else { grid = new Grid(level, Z2.apply(i,j));}
                    if (geometry.intersects(grid.getPolygon())) {
                        splitGrids.add(grid);
                    }
                }
            }
        }
    }


    public List<SplitGeometry> getSplitGeoms(){
        return splitGeoms;
    }

    public List<Grid> getSplitGrids(){
        return splitGrids;
    }


    public String getId(){
        return id;
    }

    public String getXZ2Index(){
        return XZ2Index;
    }

    public Map<String, Grid> getXZ2Grids(){
        return XZ2Grids;
    }

    public BitSet getSignature(){
        return signature;
    }

    public Geometry getGeometry() {return geometry;}

    public void geomDecompose(Geometry geometry, int recursiveTimes){

        computeXZ2Index(geometry);
        computeSignature(geometry,recursiveTimes);
    }

    public int computeInitialLevel(Geometry geometry) {
        Envelope e = geometry.getEnvelopeInternal();
        // 确定初始层级;
        int initialLevel = (int) Math.floor(Math.log(360 / Math.max(e.getWidth(), e.getHeight() * 2)) / Math.log(2));
        return initialLevel;
    }

    public void computeInitialGrids(Geometry geometry){
        int initialLevel = computeInitialLevel(geometry);
        computeGrids(geometry,initialLevel);
    }
}