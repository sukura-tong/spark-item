package com.swust.bigdata.service;

import com.swust.bigdata.pojo.AccessLogByHour;
import org.apache.hadoop.hbase.client.Table;

import java.io.IOException;

public interface AccessByHourToHbaseService {
    void insertOrUpdateToHbase(Table table, AccessLogByHour params) throws IOException;
}
