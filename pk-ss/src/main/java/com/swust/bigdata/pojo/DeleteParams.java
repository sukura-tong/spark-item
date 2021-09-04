package com.swust.bigdata.pojo;


import lombok.*;

/**
 * @author 雪瞳
 * @Slogan 时钟尚且前行，人怎能就此止步！
 * @Function 删除参数
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DeleteParams {
    private String rowkey;
    private String column;
    private String qualifer;
    private String value;
}
