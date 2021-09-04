package com.swust.bigdata.pojo;

import lombok.*;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function Hbase params
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HbaseParams {
    private String rowKey;
    private String featureFamilly;
    private String featureName;
    private String featureVlaue;
}
