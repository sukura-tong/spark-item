package com.swust.bigdata.pojo;

import lombok.*;
import scala.Tuple2;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 日志数据
 * 按照每小时用户的访问量进行描述
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AccessLogByHour {
    private String fname;
    private String family;
    private Tuple2<Tuple2<String, String>, Long> value;
}
