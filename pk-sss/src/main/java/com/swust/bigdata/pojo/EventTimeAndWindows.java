package com.swust.bigdata.pojo;


import lombok.*;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function eventtime 映射为DataSet
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EventTimeAndWindows {
    private String time;
    private String word;
}
