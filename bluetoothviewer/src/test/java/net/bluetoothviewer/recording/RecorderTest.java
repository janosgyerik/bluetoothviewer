package net.bluetoothviewer.recording;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class RecorderTest {

    private Recorder recorder;

    @Before
    public void setUp() {
        recorder = new RecorderImpl();
    }

    @Test
    public void append_bytes_to_empty() {
        byte[] bytes = {1, 2, 3, 4, 5};
        recorder.append(bytes);
        assertThat(recorder.getBytes()).isEqualTo(bytes);
    }

    @Test
    public void clear_bytes_to_empty() {
        byte[] bytes = {1, 2, 3, 4, 5};
        recorder.append(bytes);
        assertThat(recorder.getBytes()).isEqualTo(bytes);
        recorder.clear();
        assertThat(recorder.getBytes()).isEqualTo(new byte[0]);
    }

    @Test
    public void append_variable_size_chunks() {
        byte[] chunk1 = {1, 2, 3};
        byte[] chunk2 = {4, 5};
        byte[] chunk3 = {6};
        byte[] chunk4 = {7, 8, 9, 10};
        byte[] complete = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        recorder.append(chunk1);
        recorder.append(chunk2);
        recorder.append(chunk3);
        recorder.append(chunk4);
        assertThat(recorder.getBytes()).isEqualTo(complete);
    }

    @Test
    public void getBytes_works_consistently() {
        byte[] chunk1 = {1, 2, 3};
        byte[] chunk2 = {3, 4};
        byte[] complete = {1, 2, 3, 3, 4};

        recorder.append(chunk1);
        recorder.append(chunk2);
        assertThat(recorder.getBytes()).isEqualTo(complete);
        assertThat(recorder.getBytes()).isEqualTo(complete);
    }

    @Test
    public void append_then_clear_then_append_more() {
        byte[] chunk1 = {1, 2, 3};
        recorder.append(chunk1);
        assertThat(recorder.getBytes()).isEqualTo(chunk1);

        recorder.clear();
        assertThat(recorder.getBytes()).isEqualTo(new byte[0]);

        byte[] chunk2 = {3, 4, 5};
        recorder.append(chunk2);
        assertThat(recorder.getBytes()).isEqualTo(chunk2);
    }

    @Test
    public void isEmpty_works_consistently() {
        byte[] chunk1 = {1, 2, 3};
        recorder.append(chunk1);
        assertThat(recorder.isEmpty()).isFalse();

        recorder.clear();
        assertThat(recorder.isEmpty()).isTrue();

        byte[] chunk2 = {3, 4, 5};
        recorder.append(chunk2);
        assertThat(recorder.isEmpty()).isFalse();
    }
}
