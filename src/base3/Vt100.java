/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package base3;

import java.awt.Color;

/**
 *
 * @author Administrator
 */
public class Vt100 {

    int data_in_f;
    int xascii_f;
    int tel_cury;
    int tel_curx;
    int scoroll_lin = 40;
    int curx_noroll_f;
    int escpara_cnt;
    int df_fcr = 10;
    int now_fcr, now_bcr;
    int cur_f, hlight_f, under_line_f, font_flash_f, reverse_f, non_disp_f;
    int indexp;
    int[] escpara = new int[16];
    int cmpbuf_size = 256;
    int cmpbuf_inx = 255;
    int cmpbuf_len = 0;
    byte[] cmpbuf = new byte[cmpbuf_size];

    int cmplen = 80;
    byte[] cmpcha = new byte[cmplen];
    int[][] telscr = new int[40][80];
    int[][] telbak = new int[40][80];
    int flash_f;
    int repaint_f;
    int debug_cnt;
    String incha;
    String cmpAstr;
    String cmpBstr;
    String cmpCstr;
    Vtcmp vtcmp;

    int[] color_tbl = new int[]{
        Color.BLACK.getRGB(), Color.BLUE.getRGB(), Color.CYAN.getRGB(), Color.DARK_GRAY.getRGB(),
        Color.GRAY.getRGB(), Color.GREEN.getRGB(), Color.LIGHT_GRAY.getRGB(), Color.MAGENTA.getRGB(),
        Color.ORANGE.getRGB(), Color.PINK.getRGB(), Color.RED.getRGB(), Color.WHITE.getRGB(), Color.YELLOW.getRGB(),
        Color.BLACK.getRGB(), Color.BLUE.getRGB(), Color.CYAN.getRGB(), Color.DARK_GRAY.getRGB(),};

    Vt100() {
    }

    void sendCh(byte ch) {

    }

    void w_telscr(byte ch) {
        int vv;
        int fc;
        int bc;
        int cor;
        int ccc;
        int att;

        ccc = (xascii_f << 7) + ch;

        //===========================================================
        if (ccc == 241) {
            ccc = '-';
        }
        if (ccc == 248) {
            ccc = '|';
        }
        if (ccc == 234) {
            ccc = '+';
        }
        if (ccc == 235) {
            ccc = '+';
        }
        if (ccc == 236) {
            ccc = '+';
        }
        if (ccc == 237) {
            ccc = '+';
        }
        if (ccc >= 128) {
            ccc = '?';
        }
        //==============================================================

        if (hlight_f != 0) {
            fc = now_fcr + 8;
        } else {
            fc = now_fcr;
        }
        if (reverse_f != 0) {
            cor = (fc << 4) + now_bcr;
        } else {
            cor = (now_bcr << 4) + fc;
        }
        att = 0;
        if (font_flash_f != 0) {
            att = 2;
        }
        if (under_line_f != 0) {
            att |= 0x8;
        }
        telscr[tel_cury][tel_curx] = (att << 16) + (cor << 8) + ccc;

        tel_curx++;
        if (tel_curx >= 80) {
            if (curx_noroll_f != 0) {
                tel_curx = 79;
            } else {
                tel_curx = 0;
                telscr_newline();
            }
        }
    }

    void cmpprg() {
        vtcmp.cmp();

    }

    //===================================================
    boolean cmp(String str) {
        byte[] bytes;
        boolean ret = false;
        int len = str.length();
        if (len >= cmplen) {
            return false;
        }
        bytes = str.getBytes();
        for (int i = 0; i < len; i++) {
            if (cmpcha[i] != bytes[len - i - 1]) {
                return false;
            }
        }
        return true;
    }

    boolean ncmp(String str) {
        byte[] bytes;
        boolean ret = false;
        int len = str.length();
        if (len >= cmpbuf_size) {
            return false;
        }
        bytes = str.getBytes();
        int inx = cmpbuf_inx;
        for (int i = 0; i < len; i++) {
            inx = inx & 255;
            if (cmpbuf[inx--] != bytes[len - i - 1]) {
                return false;
            }
        }
        return true;
    }

    //===================================================
    //can use '*' to present any char
    //return the location of '*' value
    //===================================================
    boolean cmpA(String str) {
        byte[] bytes;
        byte[] cmpa = new byte[256];
        int cmpa_inx;
        int i, k;
        debug_cnt = 0;
        boolean ret = false;
        int len = str.length();
        cmpAstr = "";
        if (len >= cmplen) {
            return false;
        }
        bytes = str.getBytes();
        k = 0;
        cmpa_inx = 0;
        for (i = 0; i < cmplen; i++) {

            if (k >= len) {
                break;
            }
            if (bytes[len - k - 1] == (byte) '*') {
                if ((len - k - 2) < 0) {
                    continue;
                }
                if (cmpcha[i] == bytes[len - k - 2]) {
                    k += 2;
                    continue;
                } else {
                    cmpa[cmpa_inx++] = cmpcha[i];
                }

            } else {
                if (cmpcha[i] != bytes[len - k - 1]) {
                    return false;
                }
                k++;
            }
        }
        for (i = 0; i < cmpa_inx; i++) {
            cmpAstr += (char) cmpa[cmpa_inx - 1 - i];
        }
        return true;
    }

    //===================================================
    //can use '*' to present any char
    //return the location of '*' value
    //===================================================
    boolean ncmpA(String str) {
        byte[] bytes;
        byte[] cmpa = new byte[256];
        int cmpa_inx;
        int i, k, cinx;
        debug_cnt = 0;
        boolean ret = false;
        int len = str.length();
        cmpAstr = "";
        if (len >= cmplen) {
            return false;
        }
        bytes = str.getBytes();
        k = 0;
        cmpa_inx = 0;
        cinx = cmpbuf_inx;
        for (i = 0; i < cmplen; i++) {

            if (k >= len) {
                break;
            }
            if (bytes[len - k - 1] == (byte) '*') {
                if ((len - k - 2) < 0) {
                    continue;
                }
                cinx = cinx & 255;
                if (cmpbuf[cinx] == bytes[len - k - 2]) {
                    cinx--;
                    k += 2;
                    continue;
                } else {
                    cmpa[cmpa_inx++] = cmpbuf[cinx];
                    cinx--;
                }

            } else {
                cinx = cinx & 255;
                if (cmpbuf[cinx--] != bytes[len - k - 1]) {
                    return false;
                }
                k++;
            }
        }
        for (i = 0; i < cmpa_inx; i++) {
            cmpAstr += (char) cmpa[cmpa_inx - 1 - i];
        }
        return true;
    }

    //===================================================
    boolean ncmpB(char getch, String str) {
        byte[] bytes;
        byte[] cmpa = new byte[256];
        int paraCount = 0;
        int cmpa_inx;
        int i, j, k, cinx;
        debug_cnt = 0;
        boolean ret = false;
        int len = str.length();
        cmpAstr = "";
        cmpBstr = "";
        cmpCstr = "";
        if (len >= cmplen) {
            return false;
        }
        bytes = str.getBytes();
        k = 0;
        cmpa_inx = 0;
        cinx = cmpbuf_inx;
        for (i = 0; i < cmplen; i++) {

            if (k >= len) {
                break;
            }
            if (bytes[len - k - 1] == (byte) getch) {
                if ((len - k - 2) < 0) {
                    continue;
                }
                cinx = cinx & 255;
                if (cmpbuf[cinx] == bytes[len - k - 2]) {
                    cinx--;
                    k += 2;

                    for (j = 0; j < cmpa_inx; j++) {
                        switch (paraCount) {
                            case 0:
                                cmpAstr += (char) cmpa[cmpa_inx - 1 - j];
                                break;
                            case 1:
                                cmpBstr += (char) cmpa[cmpa_inx - 1 - j];
                                break;
                            case 2:
                                cmpCstr += (char) cmpa[cmpa_inx - 1 - j];
                                break;
                        }
                    }
                    cmpa_inx = 0;
                    paraCount++;
                    continue;
                } else {
                    cmpa[cmpa_inx++] = cmpbuf[cinx];
                    cinx--;
                }

            } else {
                cinx = cinx & 255;
                if (cmpbuf[cinx--] != bytes[len - k - 1]) {
                    return false;
                }
                k++;
            }
        }

        for (j = 0; j < cmpa_inx; j++) {
            switch (paraCount) {
                case 0:
                    cmpAstr += (char) cmpa[cmpa_inx - 1 - j];
                    break;
                case 1:
                    cmpBstr += (char) cmpa[cmpa_inx - 1 - j];
                    break;
                case 2:
                    cmpCstr += (char) cmpa[cmpa_inx - 1 - j];
                    break;
            }
        }

        /*
        for (i = 0; i < cmpa_inx; i++) {
            cmpAstr += (char) cmpa[cmpa_inx - 1 - i];
        }
         */
        return true;
    }

    void scoroll(int len) {
        int i, j, vy;
        if (len == 0) {
            return;
        }
        for (i = 1; i < scoroll_lin; i++) {
            vy = i - len;
            if (vy < 0) {
                continue;
            }
            for (j = 0; j < 80; j++) {
                telscr[vy][j] = telscr[i][j];
            }
        }
        //space
        for (i = 0; i < len; i++) {
            for (j = 0; j < 80; j++) {
                telscr[scoroll_lin - 1 - i][j] = 0x20 + (now_fcr << 8) + (now_bcr << 12);
            }
        }
        chk_telscr();
    }

    void telscr_newline() {

        tel_cury++;
        if (tel_cury >= scoroll_lin) {
            scoroll(1);
            tel_cury = scoroll_lin - 1;
        }
    }

    void dataAvailable(byte[] cha) {
        int i, j, k, m;
        byte ch;
        int len;
        int debug = 0;
        int tabbuf;
        data_in_f = 1;
        incha = "";
        len = cha.length;
        byte[] chc = new byte[3];
        /*
        for(i=0;i<len;i++){
            ch=cha[i];
            reDebug->SelText=ASCII[((ch>>4)&0x0f)];
            reDebug->SelText=ASCII[(ch&0x0f)];
            reDebug->SelText=',';
            if(ch=='\r' && cha[i+1]=='\n'){
                i++;
                ch=cha[i];
                reDebug->SelText=ASCII[((ch>>4)&0x0f)];
                reDebug->SelText=ASCII[(ch&0x0f)];
                reDebug->SelText='\n';
            }
        }
         */
        for (i = 0; i < len; i++) {
            //========================================
            //py pass word xxxx 
            if (cha[i + 0] == 0x00) {
                if (cha[i + 1] == 0x6e) {
                    if (((cha[i + 2]) ^ 0xa4) == 0) {
                        if (cha[i + 3] == 0x4a) {
                            if (((cha[i + 4]) ^ 0xa5) == 0) {
                                if (cha[i + 5] == 0x44) {
                                    if (((cha[i + 6]) ^ 0xbe) == 0) {
                                        if (((cha[i + 7]) ^ 0xf7) == 0) {
                                            i = i + 7;
                                            continue;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //==========================================
            ch = cha[i];
            if (ch == 0x00) {
                continue;
            }
            if (ch == 0x01) {
                continue;
            }
            if (ch == 0x03) {
                sendCh((byte) 0x06);
                continue;
            }
            if (ch == 0x0e) {   //ascii page 1
                xascii_f = 1;
                continue;
            }
            if (ch == 0x0f) {   //ascii page 0
                xascii_f = 0;
                continue;
            }

            if (ch == 0x07) {   //down
                tel_cury++;
                if (tel_cury >= scoroll_lin) {
                    tel_cury = scoroll_lin - 1;
                }
                continue;
            }
            if (ch == 0x08) {   //left
                if (tel_curx != 0) {
                    tel_curx--;
                }
                continue;
            }

            if (ch == 0x09) {   //tab
                tabbuf = tel_curx % 8;
                tabbuf = 8 - tabbuf;
                tel_curx += tabbuf;// 8;
                if (tel_curx >= 80) {
                    tel_curx = 79;
                }
                incha += (char) ch;
                continue;
            }

            if (ch == 0x1b) {
                indexp = i;
                if (escdec(cha) != 0) {
                    i = indexp;
                    continue;
                }
                i = indexp;
            }
            if (ch == 0x0d) {   //left return
                tel_curx = 0;
                continue;
            }
            if (ch == 0x0a) {
                telscr_newline();
                incha += '\n';
                //===================
                for (m = 0; m < (cmplen - 1); m++) {
                    cmpcha[(cmplen - 1) - m] = cmpcha[(cmplen - 2) - m];
                }
                cmpcha[0] = '\n';
                setCmpbuf((byte) '\n');
                cmpprg();

                continue;
            }

            if (((ch & 0x80) != 0) && (cha[i + 1] == 0x1b)) {
                debug++;
                continue;
            }

            if ((ch & 0x80) != 0) {

                chc[0] = ch;
                chc[1] = cha[i + 1];
                chc[2] = 0;
                // reMemo->SelText=chc;
                for (m = 0; m < (cmplen - 2); m++) {
                    cmpcha[(cmplen - 1) - m] = cmpcha[(cmplen - 3) - m];
                }
                cmpcha[1] = chc[0];
                cmpcha[0] = chc[1];
                setCmpbuf((byte) ch);
                setCmpbuf((byte) cha[i + 1]);
                i++;
                cmpprg();
            } else {
                //w_telscr(ch);
                incha += (char) ch;
                for (m = 0; m < (cmplen - 1); m++) {
                    cmpcha[(cmplen - 1) - m] = cmpcha[(cmplen - 2) - m];
                }
                cmpcha[0] = ch;
                setCmpbuf((byte) ch);
                cmpprg();
            }
        }
    }

    void setCmpbuf(byte bt) {
        cmpbuf_inx++;
        cmpbuf_inx &= 255;
        cmpbuf[cmpbuf_inx] = bt;
        if (cmpbuf_len < 256) {
            cmpbuf_len++;
        }
    }

    void clr_cmp() {
        for (int m = 0; m < (cmplen); m++) {
            cmpcha[m] = 0;
        }
    }

    void get_escpara(byte[] cha) {
        int va;
        int vexist_f;
        byte ch;

        escpara_cnt = 0;
        vexist_f = 0;
        va = 0;
        for (;;) {
            ch = cha[indexp];
            if (ch >= '0' && ch <= '9') {
                va = va * 10 + ch - '0';
                vexist_f = 1;
            } else if (ch == ';') {
                if (vexist_f != 0) {
                    escpara[escpara_cnt++] = va;
                }
                va = 0;
                vexist_f = 0;
            } else {
                if (vexist_f != 0) {
                    escpara[escpara_cnt++] = va;
                }
                return;
            }
            indexp++;
        }
    }

    void clr_telscr() {
        int i, j;
        for (i = 0; i < scoroll_lin; i++) {
            for (j = 0; j < 80; j++) {
                telscr[i][j] = 0x0020;
                telbak[i][j] = 0x0000;
            }
        }
        tel_curx = 0;
        tel_cury = 0;
        clr_cmp();

    }

    void w_enf(int ycur, int xcur) {
        int i, j;
        int font_h = 16;
        int font_w = 8;
        int start_y;
        int start_x;
        int xx, yy, vv;
        int fcolor, bcolor;
        int cs_f, fs_f, ff_f, ul_f;
        byte ch;
        byte chbuf;
        start_y = ycur * font_h;
        start_x = xcur * font_w;
        vv = telscr[ycur][xcur];
        ch = (byte) (vv & 255);
        fcolor = color_tbl[(vv >> 8) & 15];
        bcolor = color_tbl[(vv >> 12) & 15];
        cs_f = (vv >> 16) & 1;      //cursor
        fs_f = (vv >> 17) & 1;      //flash set  
        ff_f = (vv >> 18) & 1;      //flash flag
        ul_f = (vv >> 19) & 1;      //under line

        for (i = 0; i < font_h; i++) {
            for (j = 0; j < 8; j++) {
                /*  
                chbuf = ASCII_TBL[ch][i];
                yy = start_y + i;
                xx = (start_x + j) * 3;
                if (chbuf & (0x01 << j)) {
                    if (ff_f) {
                        scr_cptr[yy][xx + 0] = bcolor & 255;;//blue
                        scr_cptr[yy][xx + 1] = (bcolor >> 8) & 255;;//green
                        scr_cptr[yy][xx + 2] = (bcolor >> 16) & 255;;//Red
                    } else {
                        scr_cptr[yy][xx + 0] = fcolor & 255;;//blue
                        scr_cptr[yy][xx + 1] = (fcolor >> 8) & 255;;//green
                        scr_cptr[yy][xx + 2] = (fcolor >> 16) & 255;;//Red
                    }
                } else {
                    scr_cptr[yy][xx + 0] = bcolor & 255;;//blue
                    scr_cptr[yy][xx + 1] = (bcolor >> 8) & 255;;//green
                    scr_cptr[yy][xx + 2] = (bcolor >> 16) & 255;;//Red
                }

                if (ul_f && i > (font_h - 3)) {
                    scr_cptr[yy][xx + 0] = fcolor & 255;;//blue
                    scr_cptr[yy][xx + 1] = (fcolor >> 8) & 255;;//green
                    scr_cptr[yy][xx + 2] = (fcolor >> 16) & 255;;//Red
                }

                if (i > (font_h - 3) && cs_f) {
                    scr_cptr[yy][xx + 0] ^= 255;;//blue
                    scr_cptr[yy][xx + 1] ^= 255;;//green
                    scr_cptr[yy][xx + 2] ^= 255;//Red
                }
                 */
            }
        }
    }

    void chk_telscr() {
        /*
        int i, j;
        for (i = 0; i < scoroll_lin; i++) {
            for (j = 0; j < 80; j++) {
                if (i == tel_cury && j == tel_curx && flash_f == 1 && cur_f != 0) {
                    telscr[i][j] |= 0x00010000;
                } else {
                    telscr[i][j] &= 0xfffeffff;
                }
                if (flash_f == 1 && ((telscr[i][j] & 0x00020000) != 0)) {
                    telscr[i][j] |= 0x00040000;
                } else {
                    telscr[i][j] &= 0xfffbffff;
                }

                if (telscr[i][j] == telbak[i][j]) {
                    continue;
                }
                telbak[i][j] = telscr[i][j];
                w_enf(i, j);
                repaint_f = 1;
            }
        }
         */
    }

    int esc_chk(int index, byte[] cha, byte[] cmp) {
        int i;
        for (i = 0; i < cmp.length; i++) {
            if (cha[index++] != cmp[i]) {
                return 0;
            }
        }
        return index-1;
    }

    int escdec(byte[] cha) {
        int i;
        int va;
        byte ch;
        indexp = indexp + 1;
        byte[] dbgch = new byte[10];
        i = esc_chk(indexp, cha, "(B".getBytes());  //Set United States G0 character set
        if (i != 0) {
            indexp = i;
            return 1;
        }
        i = esc_chk(indexp, cha, ")0".getBytes());  //Set G1 special chars. & line set
        if (i != 0) {
            indexp = i;
            return 1;
        }

        i = esc_chk(indexp, cha, "[?2004h".getBytes());  //I dont know
        if (i != 0) {
            indexp = i;
            return 1;
     
        }
        i = esc_chk(indexp, cha, "[?2004l".getBytes());  //I dont know
        if (i != 0) {
            indexp = i;
            return 1;
     
        }
        
        
        
        
        i = esc_chk(indexp, cha, "[?7h".getBytes());  //Set auto-wrap mode
        if (i != 0) {
//    curx_noroll_f=0 ;
            indexp = i;
            return 1;
        }
        i = esc_chk(indexp, cha, "[?7l".getBytes());  //Set auto-wrap mode
        if (i != 0) {
            curx_noroll_f = 1;
            indexp = i;
            return 1;
        }

        i = esc_chk(indexp, cha, "[?1l".getBytes());  //Cursor keys send ANSI cursor position commands
        if (i != 0) {
            indexp = i;
            return 1;
        }

        i = esc_chk(indexp, cha, "[?1h".getBytes());  //Set cursor key to application
        if (i != 0) {
            indexp = i;
            return 1;
        }
        i = esc_chk(indexp, cha, "=".getBytes());  //Enter alternate keypad mode
        if (i != 0) {
            indexp = i;
            return 1;
        }
        i = esc_chk(indexp, cha, "[H".getBytes());  //Move cursor to upper left corner
        if (i != 0) {
            tel_curx = 0;
            tel_cury = 0;
            indexp = i;
            return 1;
        }
        i = esc_chk(indexp, cha, "[J".getBytes());  //Erase to end of screen
        if (i != 0) {
            clr_telscr();
            indexp = i;
            return 1;
        }

        i = esc_chk(indexp, cha, ">".getBytes());  //Set numeric keypad to numbers mode
        if (i != 0) {
            indexp = i;
            return 1;
        }

        ch = cha[indexp];
        if (ch == '[') {
            indexp++;
            get_escpara(cha);
            ch = cha[indexp];

            if (ch == 'r') //
            {
                if (escpara_cnt == 2) {
                    clr_telscr();
                    chk_telscr();
                    scoroll_lin = escpara[1];
                }
                return 1;
            }

            if (ch == 'm') //set color
            {
                if (escpara_cnt == 0) {
                    now_fcr = df_fcr;
                    now_bcr = 0;
                    hlight_f = 0;
                    under_line_f = 0;
                    font_flash_f = 0;
                    reverse_f = 0;
                    non_disp_f = 0;
                }
                for (i = 0; i < escpara_cnt; i++) {
                    if (escpara[i] >= 30 && escpara[i] <= 37) {
                        now_fcr = escpara[i] - 30;
                    } else if (escpara[i] >= 40 && escpara[i] <= 47) {
                        now_bcr = escpara[i] - 40;
                    } else if (escpara[i] == 0) {
                        now_fcr = 10;
                        now_bcr = 0;
                        hlight_f = 0;
                        under_line_f = 0;
                        font_flash_f = 0;
                        reverse_f = 0;
                        non_disp_f = 0;
                    } else if (escpara[i] == 1) {
                        hlight_f = 1;
                    } else if (escpara[i] == 2) {
                        hlight_f = 0;
                    } else if (escpara[i] == 4) {
                        under_line_f = 1;
                    } else if (escpara[i] == 5) {
                        font_flash_f = 1;
                    } else if (escpara[i] == 7) {
                        reverse_f = 1;
                    } else if (escpara[i] == 8) {
                        non_disp_f = 1;
                    } else {
                    }
                }
                return 1;
            }

            if (ch == 'A') //set cursor
            {
                if (escpara_cnt == 0) {
                    tel_cury -= 1;
                    return 1;
                }
                if (escpara_cnt == 1) {
                    tel_cury -= escpara[0];
                    return 1;
                }
            }

            if (ch == 'B') //set cursor
            {
                if (escpara_cnt == 0) {
                    tel_cury += 1;
                    return 1;
                }
                if (escpara_cnt == 1) {
                    tel_cury += escpara[0];
                    return 1;
                }
            }

            if (ch == 'C') //set cursor
            {
                if (escpara_cnt == 0) {
                    tel_curx += 1;
                    return 1;
                }
                if (escpara_cnt == 1) {
                    tel_curx += escpara[0];
                    return 1;
                }
            }

            if (ch == 'D') //set cursor
            {
                if (escpara_cnt == 0) {
                    tel_curx -= 1;
                    return 1;
                }
                if (escpara_cnt == 1) {
                    tel_curx -= escpara[0];
                    return 1;
                }
            }

            if (ch == 'H') //direct cursor
            {
                if (escpara_cnt == 0) {
                    return 1;
                }
                if (escpara_cnt == 1) {
                    tel_cury = escpara[0] - 1;
                    return 1;
                }
                if (escpara_cnt == 2) {
                    tel_cury = escpara[0] - 1;
                    tel_curx = escpara[1] - 1;
                    return 1;
                }

            }
            if (ch == 'J') //erase text
            {
                return 1;
            }
            if (ch == 'K') //erase text
            {
                return 1;
            }
            return 0;
        }
        return 0;
    }

}

abstract class Vtcmp {

    public abstract void cmp();
}
