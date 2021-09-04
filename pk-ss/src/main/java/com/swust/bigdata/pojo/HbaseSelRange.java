package com.swust.bigdata.pojo;

import lombok.*;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function Hbase 查询范围
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class HbaseSelRange {
    private String start;
    private String stop;
    private String family;
    private String coulmn;
}
