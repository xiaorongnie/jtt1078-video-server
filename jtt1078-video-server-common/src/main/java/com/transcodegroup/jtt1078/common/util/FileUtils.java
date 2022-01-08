package com.transcodegroup.jtt1078.common.util;

import java.io.*;

/**
 * 文件操作单元
 * 
 * @author eason
 * @date 2022/01/08
 */
public final class FileUtils {

    public static void writeFile(File file, byte[] data) {
        writeFile(file, data, false);
    }

    public static void writeFile(File file, byte[] data, boolean append) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file, append);
            fos.write(data);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    public static byte[] read(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return read(fis);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }

    public static byte[] read(InputStream fis) {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream(1024);

            int len = -1;
            byte[] block = new byte[1024];
            while ((len = fis.read(block)) > -1) {
                baos.write(block, 0, len);
            }

            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void readInto(File file, OutputStream os) {
        FileInputStream fis = null;
        try {
            int len = -1;
            byte[] block = new byte[1024];
            fis = new FileInputStream(file);
            while ((len = fis.read(block)) > -1) {
                os.write(block, 0, len);
                os.flush();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }

    public static byte[] readFileToByteArray(String src) {
        // 文件输入流（需要关闭）
        InputStream is = null;
        try {
            is = new FileInputStream(new File(src));
            // 字节数组输出流（不需要关闭）
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024 * 1];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            baos.flush();
            return baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void writeByteArrayToFile(byte[] datas, String destFileName) {
        // 文件输出流（需要关闭）
        OutputStream os = null;
        try {
            // 字节数组输入流（不需要关闭）
            InputStream is = new ByteArrayInputStream(datas);
            os = new FileOutputStream(new File(destFileName));

            byte[] buf = new byte[1024];
            int len;
            while (((len = is.read(buf)) != -1)) {
                os.write(buf, 0, len);
            }
            os.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
