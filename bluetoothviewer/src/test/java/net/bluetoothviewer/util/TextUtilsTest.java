package net.bluetoothviewer.util;

import org.junit.Test;

import static net.bluetoothviewer.util.TextUtils.bytesToHexDump;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class TextUtilsTest {
    @Test
    public void bytesToHexDump_hello_returns_68_65_6c_6c_6f() {
        assertThat(bytesToHexDump("hello".getBytes())).isEqualTo("68 65 6c 6c 6f");
    }

    @Test
    public void bytesToHexDump_4nulls_returns_00_00_00_00() {
        assertThat(bytesToHexDump(new byte[]{0, 0, 0, 0})).isEqualTo("00 00 00 00");
    }

    @Test
    public void bytesToHexDump_ed_fe_07_returns_ed_fe_07() {
        assertThat(bytesToHexDump(new byte[]{(byte) 0xed, (byte) 0xfe, 7})).isEqualTo("ed fe 07");
    }
}
