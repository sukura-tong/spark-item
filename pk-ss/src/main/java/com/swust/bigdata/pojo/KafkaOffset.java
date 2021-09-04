package com.swust.bigdata.pojo;

import lombok.*;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function offset实体类
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KafkaOffset {
    private String topic;
    private String groupid;
    private int partitions;
    private long offset;
}
