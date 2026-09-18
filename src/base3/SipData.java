/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package base3;

/**
 *
 * @author Kevin
 */
class SipData {

    int ioBuf;
    byte phoneSta;
    byte connectSta;
    byte handStatus;
    byte earSpeakerVol;
    byte phsetSpeakerVol;
    byte earMicSens;
    byte phsetMicSens;
    int sipFlag;
    String sipStatus;
    String sipAction;
    String callto;
    String callfrom;
    int connectTime=0;

    SipData() {
        ioBuf = 0;
        phoneSta = 0x00;
        connectSta = 0x00;
        handStatus = 0x00;
        earSpeakerVol = 0x00;
        phsetSpeakerVol = 0x00;
        earMicSens = 0x00;
        phsetMicSens = 0x00;
        sipFlag = 0x00;
        sipStatus = "中山科學研究院";
        sipAction = "軟體電話 (未連線)";
        callto = "callto";
        callfrom = "callfrom";
    }
}
