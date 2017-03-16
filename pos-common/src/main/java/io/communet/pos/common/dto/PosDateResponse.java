package io.communet.pos.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by leejohn on 2017/2/16.
 */
@Data
public class PosDateResponse
        implements Serializable {

    private int code;

    private List<OrderInfo> msg;

//    {"code":0,"msg":
//        [{"orderTime":"2017-02-14 11:45:27","orderAmount":240.000},{"orderTime":"2017-02-14 11:48:33","orderAmount":161.500}]}
}
