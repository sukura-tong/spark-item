package com.swust.bigdata.item;

import java.io.*;

/**
 * @author 雪瞳
 * @Slogan 忘时，忘物，忘我。
 * @Function 针对大型流处理作业项目，如果中途服务器宕机，选择启动offset恢复模式
 */
public class GetOffsetUtils {
    public static int getOffsetFromCheckPointFile() throws IOException {
        String checkPath = "./pk-sss/check/java/points/offsets/0";
        File file = new File(checkPath);

        FileReader reader = null;

        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            return 0;
        }
        BufferedReader buffer = new BufferedReader(reader);

        String metaOffset;
        String topic = "access-log-nfy";
        while ((metaOffset = buffer.readLine()) != null) {
            if (metaOffset.contains(topic)) {
                break;
            }
        }

        String[] splits = metaOffset.split(":");
        String offset = splits[splits.length - 1].substring(0, splits[splits.length - 1].toCharArray().length - 2);

        return Integer.parseInt(offset);

    }

    public static void main(String[] args) {
        try {
            int offset = getOffsetFromCheckPointFile();
            System.out.println(offset);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
