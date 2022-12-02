package util;



import java.io.*;


/**
 * @Author zhangjianhao
 * @Date 2021/10/28
 */

public class FileUtil {

    /**
     * 将文本读取成字符串
     *
     * @param path 文本路径
     * @return 文本对应的字符串结果
     * @throws IOException
     */
    public static String readFile(String path) throws IOException {
        Reader reader = new InputStreamReader(new FileInputStream(new File(path)));
        BufferedReader br = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }




    public static String readStream(String path) {
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = new FileInputStream(new File(path));
            byte[] cbuf = new byte[1024 * 10 * 10];
            int len = -1;
            while ((len = fis.read(cbuf)) != -1) {
                sb.append(new String(cbuf));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static void writeFile(String data, String outPath) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(outPath));
            fos.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void copyFileByStream(String srcPath, String desPath) {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            //造流
            fis = new FileInputStream(new File(srcPath));
            fos = new FileOutputStream(new File(desPath));
            //读文件并写入到新文件，完成赋值操作
            byte[] cbuf = new byte[8192];
            int len = -1;
            while ((len = fis.read(cbuf)) != -1) {
                fos.write(cbuf, 0, len);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
