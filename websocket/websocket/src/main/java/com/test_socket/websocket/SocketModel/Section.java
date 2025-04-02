package com.test_socket.websocket.SocketModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Section {
    public Object mid;
    public int sid;
    public int psid;
    public int sno;
    public int psrno;
    public String gstatus;
    public String nat;
    public int gscode;
    public int max;
    public int min;
    public String rem;
    public boolean br;
    public Object rname;
    public Object jname;
    public Object tname;
    public int hage;
    public Object himg;
    public int adfa;
    public Object rdt;
    public Object cno;
    public Object sdraw;
    public List<Datum> odds;
}
