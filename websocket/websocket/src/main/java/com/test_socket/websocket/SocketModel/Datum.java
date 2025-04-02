package com.test_socket.websocket.SocketModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class Datum {
    public int gmid;
    public Object mid;
    public Object pmid;
    public String mname;
    public String rem;
    public String gtype;
    public String status;
    public int rc;
    public boolean visible;
    public int pid;
    public int gscode;
    public int maxb;
    public double sno;
    public int dtype;
    public int ocnt;
    public int m;
    public int max;
    public int min;
    public boolean biplay;
    public int umaxbof;
    public boolean boplay;
    public boolean iplay;
    public int btcnt;
    public Object company;
    public List<Section> section;
    public int sid;

    public int psid;
    public double odds;
    public String otype;
    public String oname;
    public int tno;
    public double size;
}

@AllArgsConstructor
@NoArgsConstructor
class Odd{
    public int sid;
    public int psid;
    public double odds;
    public String otype;
    public String oname;
    public int tno;
    public double size;
}


