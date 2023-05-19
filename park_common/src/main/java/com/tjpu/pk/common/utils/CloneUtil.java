package com.tjpu.pk.common.utils;

import java.io.*;

/**
 * @author: chengzq
 * @date: 2020/3/26 0026 11:22
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public class CloneUtil {

    /**
     * @author: chengzq
     * @date: 2020/3/26 0026 上午 11:33
     * @Description: 深度复制对象
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [obj]
     * @throws:
     */
    public static <T extends Serializable> T clone(T obj) {
        T clonedObj = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            clonedObj = (T) ois.readObject();
            ois.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return clonedObj;
    }
}
