package com.test_socket.websocket.SocketModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

//@Data
//@RedisHash("Java_Odds")
@AllArgsConstructor
@Getter
@Setter
public class Java_Odds {

    public String event_id;
    public String iplay;
    public String mid;


    public String sid;
    //public int? psid;
    public String sno;
    //public int? psrno;
    public String gstatus;
    public String nat;


    public double odds_back_1;
    public double tno_back_1;
    public double size_back_1;

    public double odds_back_2;
    public double tno_back_2;
    public double size_back_2;


    public double odds_back_3;
    public double tno_back_3;
    public double size_back_3;

    public double odds_lay_1;
    public double tno_lay_1;
    public double size_lay_1;

    public double odds_lay_2;
    public double tno_lay_2;
    public double size_lay_2;


    public double odds_lay_3;
    public double tno_lay_3;
    public double size_lay_3;

    public String marketType;
    public String vendor;
    public String eid;


    public Java_Odds() {

    }
}